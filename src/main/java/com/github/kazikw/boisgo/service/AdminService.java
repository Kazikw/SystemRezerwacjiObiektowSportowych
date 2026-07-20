package com.github.kazikw.boisgo.service;

import com.github.kazikw.boisgo.domain.*;
import com.github.kazikw.boisgo.repository.FacilityAdminAssignmentRepository;
import com.github.kazikw.boisgo.repository.FacilityBlockadeRepository;
import com.github.kazikw.boisgo.repository.ReservationRepository;
import com.github.kazikw.boisgo.repository.ReservationSpecifications;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final ReservationRepository reservationRepository;
    private final FacilityBlockadeRepository facilityBlockadeRepository;
    private final FacilityAdminAssignmentRepository facilityAdminAssignmentRepository;
    private final UserService userService;

    public List<Long> getManagedFacilityIds(Long adminId) {
        return facilityAdminAssignmentRepository.findFacilityIdsByAdminId(adminId);
    }

    public List<Reservation> getPendingReservations(Long adminId) {
        return reservationRepository.findByFacility_IdInAndStatus(getManagedFacilityIds(adminId), ReservationStatus.PENDING);
    }

    @Transactional
    public void setStatus(Long reservationId, ReservationStatus status) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new EntityNotFoundException("Nie znaleziono rezerwacji o ID: " + reservationId));
        reservation.setStatus(status);
        reservationRepository.save(reservation);
    }

    @Transactional
    public void assignFacilityAdmin(Long userId, Long facilityId) {
        if (facilityAdminAssignmentRepository.existsByAdmin_IdAndFacility_Id(userId, facilityId)) {
            throw new IllegalStateException("Użytkownik jest już administratorem tego obiektu.");
        }
        User user = userService.getById(userId);
        Facility facility = new Facility();
        facility.setId(facilityId);

        facilityAdminAssignmentRepository.save(
                FacilityAdminAssignment.builder().admin(user).facility(facility).build());
        userService.promoteToAdmin(user);
    }

    @Transactional
    public void createBlockade(Long facilityId, LocalDate startDate, LocalDate endDate, String reason, User admin) {
        Facility facility = new Facility();
        facility.setId(facilityId);

        FacilityBlockade blockade = FacilityBlockade.builder()
                .facility(facility)
                .startDate(startDate)
                .endDate(endDate)
                .reason(reason)
                .createdByAdmin(admin)
                .active(true)
                .build();
        facilityBlockadeRepository.save(blockade);

        LocalDate current = startDate;
        while (!current.isAfter(endDate)) {
            List<Reservation> affected = reservationRepository.findByFacility_IdAndDateOrderByStartTimeAsc(facilityId, current);
            affected.forEach(r -> r.setStatus(ReservationStatus.CANCELLED));
            reservationRepository.saveAll(affected);
            current = current.plusDays(1);
        }
    }

    @Transactional
    public void deactivateBlockade(Long blockadeId) {
        FacilityBlockade blockade = facilityBlockadeRepository.findById(blockadeId)
                .orElseThrow(() -> new EntityNotFoundException("Nie znaleziono blokady o ID: " + blockadeId));
        blockade.setActive(false);
        facilityBlockadeRepository.save(blockade);
    }

    public List<FacilityBlockade> getActiveBlockades(Long adminId) {
        return facilityBlockadeRepository.findByCreatedByAdmin_Id(adminId).stream()
                .filter(b -> b.isActive() && b.getEndDate().isAfter(LocalDate.now()))
                .toList();
    }

    public List<FacilityBlockade> getPastBlockades(Long adminId) {
        return facilityBlockadeRepository.findByCreatedByAdmin_Id(adminId).stream()
                .filter(b -> !b.isActive() || !b.getEndDate().isAfter(LocalDate.now()))
                .toList();
    }

    public List<Reservation> filterReservations(Long adminId, List<ReservationStatus> statuses,
                                                LocalDate dateFrom, LocalDate dateTo) {
        Specification<Reservation> spec = Specification.where(
                ReservationSpecifications.forFacilities(getManagedFacilityIds(adminId)));

        Specification<Reservation> statusSpec = ReservationSpecifications.withStatuses(statuses);
        if (statusSpec != null) spec = spec.and(statusSpec);

        Specification<Reservation> fromSpec = ReservationSpecifications.fromDate(dateFrom);
        if (fromSpec != null) spec = spec.and(fromSpec);

        Specification<Reservation> toSpec = ReservationSpecifications.toDate(dateTo);
        if (toSpec != null) spec = spec.and(toSpec);

        return reservationRepository.findAll(spec);
    }
}
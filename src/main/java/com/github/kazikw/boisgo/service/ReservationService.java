package com.github.kazikw.boisgo.service;

import com.github.kazikw.boisgo.domain.*;
import com.github.kazikw.boisgo.dto.ReservationCreateRequest;
import com.github.kazikw.boisgo.repository.FacilityBlockadeRepository;
import com.github.kazikw.boisgo.repository.ReservationRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReservationService {

    private static final List<ReservationStatus> BLOCKING_STATUSES =
            List.of(ReservationStatus.PENDING, ReservationStatus.CONFIRMED);
    private static final int UPCOMING_LIMIT = 5;

    private final ReservationRepository reservationRepository;
    private final FacilityBlockadeRepository facilityBlockadeRepository;
    private final FacilityService facilityService;

    @Transactional
    public Reservation createReservation(ReservationCreateRequest request, User currentUser) {
        Facility facility = facilityService.getById(request.facilityId());

        LocalTime startTime = request.startTime();
        LocalTime endTime = startTime.plusHours(1);

        List<FacilityBlockade> blockades = facilityBlockadeRepository
                .findActiveForFacilityOnDate(facility.getId(), request.date());
        if (!blockades.isEmpty()) {
            FacilityBlockade blockade = blockades.get(0);
            throw new IllegalStateException("Obiekt jest niedostępny w tym terminie (blokada od "
                    + blockade.getStartDate() + " do " + blockade.getEndDate()
                    + ", powód: " + blockade.getReason() + ").");
        }

        if (!isFacilityAvailable(facility.getId(), request.date(), startTime, endTime)) {
            throw new IllegalStateException("Wybrany termin jest już zajęty.");
        }

        Reservation reservation = Reservation.builder()
                .facility(facility)
                .reserver(currentUser)
                .date(request.date())
                .startTime(startTime)
                .endTime(endTime)
                .status(ReservationStatus.PENDING)
                .requiredParticipants(request.groupReservation() ? validateGroupSize(request.requiredParticipants()) : 1)
                .allowJoin(request.groupReservation() && request.allowJoin())
                .build();

        reservation.getParticipants().add(currentUser);

        return reservationRepository.save(reservation);
    }

    private int validateGroupSize(Integer requiredParticipants) {
        if (requiredParticipants == null || requiredParticipants < 2) {
            throw new IllegalStateException("Dla rezerwacji grupowej wymagana liczba uczestników to co najmniej 2.");
        }
        return requiredParticipants;
    }

    public boolean isFacilityAvailable(Long facilityId, LocalDate date, LocalTime startTime, LocalTime endTime) {
        return reservationRepository.findOverlapping(facilityId, date, startTime, endTime, BLOCKING_STATUSES).isEmpty();
    }

    @Transactional
    public void cancelReservation(Long reservationId, User currentUser) {
        Reservation reservation = getById(reservationId);
        if (!reservation.getReserver().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("Tylko organizator może anulować rezerwację.");
        }
        reservationRepository.delete(reservation);
    }

    @Transactional
    public void joinReservation(Long reservationId, User currentUser) {
        Reservation reservation = getById(reservationId);
        if (!canUserJoin(reservation, currentUser)) {
            throw new IllegalStateException("Nie możesz dołączyć do tej rezerwacji.");
        }
        reservation.getParticipants().add(currentUser);
        reservationRepository.save(reservation);
    }

    @Transactional
    public void leaveReservation(Long reservationId, User currentUser) {
        Reservation reservation = getById(reservationId);
        if (reservation.getReserver().getId().equals(currentUser.getId())) {
            throw new IllegalStateException("Organizator nie może opuścić własnej rezerwacji — może ją anulować.");
        }
        reservation.getParticipants().remove(currentUser);
        reservationRepository.save(reservation);
    }

    @Transactional
    public void removeParticipant(Long reservationId, Long participantId, User currentUser) {
        Reservation reservation = getById(reservationId);
        if (!reservation.getReserver().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("Tylko organizator może usuwać uczestników.");
        }
        if (reservation.getReserver().getId().equals(participantId)) {
            throw new IllegalStateException("Nie można usunąć organizatora z listy uczestników.");
        }
        boolean removed = reservation.getParticipants().removeIf(u -> u.getId().equals(participantId));
        if (!removed) {
            throw new IllegalStateException("Ten użytkownik nie jest uczestnikiem rezerwacji.");
        }
        reservationRepository.save(reservation);
    }

    public boolean canUserJoin(Reservation reservation, User user) {
        if (reservation.getStatus() != ReservationStatus.CONFIRMED) {
            return false;
        }
        if (reservation.getReserver().getId().equals(user.getId())) {
            return false;
        }
        if (reservation.getParticipants().stream().anyMatch(p -> p.getId().equals(user.getId()))) {
            return false;
        }
        if (reservation.getParticipants().size() >= reservation.getRequiredParticipants()) {
            return false;
        }
        LocalDate today = LocalDate.now();
        if (reservation.getDate().isBefore(today)
                || (reservation.getDate().isEqual(today) && reservation.getEndTime().isBefore(LocalTime.now()))) {
            return false;
        }
        return reservation.isAllowJoin();
    }

    public Reservation getById(Long id) {
        return reservationRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Nie znaleziono rezerwacji o ID: " + id));
    }

    public Reservation getDetails(Long id) {
        return reservationRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new EntityNotFoundException("Nie znaleziono rezerwacji o ID: " + id));
    }

    public List<Reservation> searchByFacilityAndDate(Long facilityId, LocalDate date) {
        return reservationRepository.findByFacility_IdAndDateOrderByStartTimeAsc(facilityId, date);
    }

    public List<Reservation> getUpcomingAsOrganizer(Long userId) {
        return reservationRepository.findTop5ByReserver_IdAndDateGreaterThanEqualOrderByDateAscStartTimeAsc(userId, LocalDate.now());
    }

    public List<Reservation> getUpcomingAsParticipant(Long userId) {
        return reservationRepository.findUpcomingWhereUserIsParticipant(
                userId, LocalDate.now(), org.springframework.data.domain.PageRequest.of(0, UPCOMING_LIMIT));
    }

    public List<Reservation> getJoinable(Long userId) {
        return reservationRepository.findJoinableForUser(
                userId, LocalDate.now(), org.springframework.data.domain.PageRequest.of(0, UPCOMING_LIMIT));
    }

    public long countParticipants(Long reservationId) {
        return reservationRepository.countParticipants(reservationId);
    }

    public Page<Reservation> getMyReservationsPaginated(User user, Pageable pageable) {
        List<Reservation> combined = new ArrayList<>();
        combined.addAll(reservationRepository.findFutureAndTodayForUser(user));
        combined.addAll(reservationRepository.findPastForUser(user));

        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), combined.size());
        List<Reservation> pageContent = start < end ? combined.subList(start, end) : List.of();

        return new PageImpl<>(pageContent, pageable, combined.size());
    }
}
package com.github.kazikw.boisgo.repository;

import com.github.kazikw.boisgo.domain.Reservation;
import com.github.kazikw.boisgo.domain.ReservationStatus;
import com.github.kazikw.boisgo.domain.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

public interface ReservationRepository extends JpaRepository<Reservation, Long>, JpaSpecificationExecutor<Reservation> {

    List<Reservation> findByFacility_IdIn(List<Long> facilityIds);

    List<Reservation> findByFacility_IdInAndStatus(List<Long> facilityIds, ReservationStatus status);

    List<Reservation> findByFacility_IdAndDateOrderByStartTimeAsc(Long facilityId, LocalDate date);

    List<Reservation> findByFacility_IdInAndDateOrderByStartTimeAsc(List<Long> facilityIds, LocalDate date);

    List<Reservation> findByReserver_Id(Long reserverId);

    List<Reservation> findTop5ByReserver_IdAndDateGreaterThanEqualOrderByDateAscStartTimeAsc(Long reserverId, LocalDate date);

    @Query("""
        SELECT r FROM Reservation r
        WHERE r.facility.id = :facilityId
        AND r.date = :date
        AND r.status IN :statuses
        AND r.startTime < :endTime AND r.endTime > :startTime
        """)
    List<Reservation> findOverlapping(@Param("facilityId") Long facilityId,
                                      @Param("date") LocalDate date,
                                      @Param("startTime") LocalTime startTime,
                                      @Param("endTime") LocalTime endTime,
                                      @Param("statuses") List<ReservationStatus> statuses);

    @Query("""
        SELECT r FROM Reservation r JOIN r.participants p
        WHERE p.id = :userId AND r.date >= :date
        ORDER BY r.date ASC, r.startTime ASC
        """)
    List<Reservation> findUpcomingWhereUserIsParticipant(@Param("userId") Long userId,
                                                         @Param("date") LocalDate date,
                                                         Pageable pageable);

    @Query("""
        SELECT r FROM Reservation r
        WHERE r.allowJoin = true
        AND r.status = ReservationStatus.CONFIRMED
        AND r.date >= :date
        AND r.reserver.id <> :userId
        AND :userId NOT IN (SELECT p.id FROM r.participants p)
        AND SIZE(r.participants) < r.requiredParticipants
        ORDER BY r.date ASC, r.startTime ASC
        """)
    List<Reservation> findJoinableForUser(@Param("userId") Long userId,
                                          @Param("date") LocalDate date,
                                          Pageable pageable);

    @Query("SELECT COUNT(p) FROM Reservation r JOIN r.participants p WHERE r.id = :reservationId")
    long countParticipants(@Param("reservationId") Long reservationId);

    @Query("""
        SELECT r FROM Reservation r
        JOIN FETCH r.facility f
        JOIN FETCH r.reserver res
        LEFT JOIN FETCH r.participants p
        WHERE r.id = :id
        """)
    Optional<Reservation> findByIdWithDetails(@Param("id") Long id);

    @Query("""
        SELECT DISTINCT r FROM Reservation r
        JOIN FETCH r.facility f
        JOIN FETCH r.reserver res
        LEFT JOIN FETCH r.participants p
        WHERE r.date >= CURRENT_DATE AND (r.reserver = :user OR :user MEMBER OF r.participants)
        ORDER BY r.date ASC, r.startTime ASC
        """)
    List<Reservation> findFutureAndTodayForUser(@Param("user") User user);

    @Query("""
        SELECT DISTINCT r FROM Reservation r
        JOIN FETCH r.facility f
        JOIN FETCH r.reserver res
        LEFT JOIN FETCH r.participants p
        WHERE r.date < CURRENT_DATE AND (r.reserver = :user OR :user MEMBER OF r.participants)
        ORDER BY r.date DESC, r.startTime DESC
        """)
    List<Reservation> findPastForUser(@Param("user") User user);
}
package com.github.kazikw.boisgo.repository;

import com.github.kazikw.boisgo.domain.FacilityBlockade;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface FacilityBlockadeRepository extends JpaRepository<FacilityBlockade, Long> {

    List<FacilityBlockade> findByCreatedByAdmin_Id(Long adminId);

    @Query("""
        SELECT b FROM FacilityBlockade b
        WHERE b.active = true
        AND b.facility.id = :facilityId
        AND b.startDate <= :date
        AND b.endDate >= :date
        """)
    List<FacilityBlockade> findActiveForFacilityOnDate(@Param("facilityId") Long facilityId, @Param("date") LocalDate date);
}
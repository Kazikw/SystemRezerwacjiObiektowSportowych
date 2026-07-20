package com.github.kazikw.boisgo.repository;


import com.github.kazikw.boisgo.domain.FacilityAdminAssignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface FacilityAdminAssignmentRepository extends JpaRepository<FacilityAdminAssignment, Long> {

    @Query("SELECT a.facility.id FROM FacilityAdminAssignment a WHERE a.admin.id = :adminId")
    List<Long> findFacilityIdsByAdminId(@Param("adminId") Long adminId);

    boolean existsByAdmin_IdAndFacility_Id(Long adminId, Long facilityId);
}
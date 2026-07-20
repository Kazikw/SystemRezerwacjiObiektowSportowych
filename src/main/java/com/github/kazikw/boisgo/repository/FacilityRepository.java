package com.github.kazikw.boisgo.repository;

import com.github.kazikw.boisgo.domain.Facility;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FacilityRepository extends JpaRepository<Facility, Long> {
    List<Facility> findByCity_NameOrderByName(String cityName);
    List<Facility> findByIdIn(List<Long> ids);
    Optional<Facility> findByNameAndCity_Name(String name, String cityName);
}
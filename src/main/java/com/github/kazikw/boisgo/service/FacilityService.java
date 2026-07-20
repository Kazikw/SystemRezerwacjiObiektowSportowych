package com.github.kazikw.boisgo.service;

import com.github.kazikw.boisgo.domain.City;
import com.github.kazikw.boisgo.domain.Facility;
import com.github.kazikw.boisgo.repository.CityRepository;
import com.github.kazikw.boisgo.repository.FacilityRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FacilityService {

    private final CityRepository cityRepository;
    private final FacilityRepository facilityRepository;

    public List<City> getCities() {
        return cityRepository.findAll();
    }

    public List<Facility> getFacilitiesByCity(String cityName) {
        return facilityRepository.findByCity_NameOrderByName(cityName);
    }

    public Facility getById(Long id) {
        return facilityRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Nie znaleziono obiektu o ID: " + id));
    }

    public List<Facility> getByIds(List<Long> ids) {
        return facilityRepository.findByIdIn(ids);
    }
}
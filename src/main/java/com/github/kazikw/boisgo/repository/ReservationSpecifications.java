package com.github.kazikw.boisgo.repository;


import com.github.kazikw.boisgo.domain.Reservation;
import com.github.kazikw.boisgo.domain.ReservationStatus;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.util.List;

public final class ReservationSpecifications {

    private ReservationSpecifications() {
    }

    public static Specification<Reservation> forFacilities(List<Long> facilityIds) {
        return (root, query, cb) -> root.get("facility").get("id").in(facilityIds);
    }

    public static Specification<Reservation> withStatuses(List<ReservationStatus> statuses) {
        if (statuses == null || statuses.isEmpty()) {
            return null;
        }
        return (root, query, cb) -> root.get("status").in(statuses);
    }

    public static Specification<Reservation> fromDate(LocalDate dateFrom) {
        if (dateFrom == null) {
            return null;
        }
        return (root, query, cb) -> cb.greaterThanOrEqualTo(root.get("date"), dateFrom);
    }

    public static Specification<Reservation> toDate(LocalDate dateTo) {
        if (dateTo == null) {
            return null;
        }
        return (root, query, cb) -> cb.lessThanOrEqualTo(root.get("date"), dateTo);
    }
}
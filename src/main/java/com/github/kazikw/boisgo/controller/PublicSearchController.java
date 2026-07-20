package com.github.kazikw.boisgo.controller;


import com.github.kazikw.boisgo.domain.Reservation;
import com.github.kazikw.boisgo.domain.User;
import com.github.kazikw.boisgo.dto.ReservationView;
import com.github.kazikw.boisgo.service.FacilityService;
import com.github.kazikw.boisgo.service.ReservationService;
import com.github.kazikw.boisgo.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Controller
@RequiredArgsConstructor
public class PublicSearchController {

    private final FacilityService facilityService;
    private final ReservationService reservationService;
    private final UserService userService;

//    @GetMapping("/index/getFacilities")
//    @ResponseBody
//    public List<String> getFacilities(@RequestParam String city) {
//        return facilityService.getFacilitiesByCity(city).stream().map(f -> f.getName()).toList();
//    }
@GetMapping("/index/getFacilities")
@ResponseBody
public List<Map<String, Object>> getFacilities(@RequestParam String city) {
    return facilityService.getFacilitiesByCity(city).stream()
            .map(f -> Map.<String, Object>of("id", f.getId(), "name", f.getName()))
            .toList();
}
    @GetMapping("/public/search")
    public String search(@RequestParam Long facilityId,
                         @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
                         Model model) {
        List<Reservation> reservations = reservationService.searchByFacilityAndDate(facilityId, date);

        boolean loggedIn = SecurityContextHolder.getContext().getAuthentication() != null
                && SecurityContextHolder.getContext().getAuthentication().isAuthenticated()
                && !"anonymousUser".equals(SecurityContextHolder.getContext().getAuthentication().getPrincipal());

        User currentUser = loggedIn ? userService.getCurrentUser() : null;

        List<ReservationView> views = reservations.stream()
                .map(r -> new ReservationView(
                        r,
                        reservationService.countParticipants(r.getId()),
                        currentUser != null && reservationService.canUserJoin(r, currentUser)))
                .toList();

        model.addAttribute("reservationViews", views);
        return loggedIn ? "user/reservations-list" : "unloged/reservations-list";
    }
}
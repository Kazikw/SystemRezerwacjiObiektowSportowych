package com.github.kazikw.boisgo.controller.user;

import com.github.kazikw.boisgo.domain.User;
import com.github.kazikw.boisgo.service.ReservationService;
import com.github.kazikw.boisgo.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/user")
@RequiredArgsConstructor
class DashboardController {

    private final UserService userService;
    private final ReservationService reservationService;

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        User currentUser = userService.getCurrentUser();

        model.addAttribute("userName", currentUser.getFirstName());
        model.addAttribute("upcomingReservations", reservationService.getUpcomingAsParticipant(currentUser.getId()));
        model.addAttribute("myActiveGroupReservations", reservationService.getUpcomingAsOrganizer(currentUser.getId()));
        model.addAttribute("joinableReservations", reservationService.getJoinable(currentUser.getId()));

        return "user/dashboard";
    }
}
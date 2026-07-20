package com.github.kazikw.boisgo.controller.user;

import com.github.kazikw.boisgo.domain.Reservation;
import com.github.kazikw.boisgo.domain.User;
import com.github.kazikw.boisgo.dto.ReservationCreateRequest;
import com.github.kazikw.boisgo.dto.UserReservationView;
import com.github.kazikw.boisgo.service.FacilityService;
import com.github.kazikw.boisgo.service.ReservationService;
import com.github.kazikw.boisgo.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/reservations")
@RequiredArgsConstructor
class ReservationController {

    private final ReservationService reservationService;
    private final FacilityService facilityService;
    private final UserService userService;

    @GetMapping("/create")
    public String createForm(Model model) {
        model.addAttribute("reservationRequest",
                new ReservationCreateRequest(null, null, null, false, 1, false));
        model.addAttribute("facilities", facilityService.getCities().stream()
                .flatMap(c -> facilityService.getFacilitiesByCity(c.getName()).stream())
                .toList());
        return "user/create-reservation";
    }

    @PostMapping("/create")
    public String create(@ModelAttribute("reservationRequest") @Valid ReservationCreateRequest request,
                         BindingResult bindingResult,
                         RedirectAttributes redirectAttributes,
                         Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("facilities", facilityService.getCities().stream()
                    .flatMap(c -> facilityService.getFacilitiesByCity(c.getName()).stream())
                    .toList());
            return "user/create-reservation";
        }
        try {
            reservationService.createReservation(request, userService.getCurrentUser());
        } catch (IllegalStateException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/reservations/create";
        }
        redirectAttributes.addFlashAttribute("successMessage", "Rezerwacja została utworzona!");
        return "redirect:/reservations/create";
    }

    @PostMapping("/{id}/cancel")
    public String cancel(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        reservationService.cancelReservation(id, userService.getCurrentUser());
        redirectAttributes.addFlashAttribute("successMessage", "Rezerwacja anulowana.");
        return "redirect:/user/my-reservations";
    }

    @PostMapping("/{id}/join")
    public String join(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        reservationService.joinReservation(id, userService.getCurrentUser());
        redirectAttributes.addFlashAttribute("successMessage", "Dołączono do rezerwacji!");
        return "redirect:/user/my-reservations";
    }

    @PostMapping("/{id}/leave")
    public String leave(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        reservationService.leaveReservation(id, userService.getCurrentUser());
        redirectAttributes.addFlashAttribute("successMessage", "Opuszczono rezerwację.");
        return "redirect:/user/my-reservations";
    }

    @PostMapping("/{id}/remove-participant")
    public String removeParticipant(@PathVariable Long id, @RequestParam Long participantId,
                                    RedirectAttributes redirectAttributes) {
        reservationService.removeParticipant(id, participantId, userService.getCurrentUser());
        redirectAttributes.addFlashAttribute("successMessage", "Uczestnik został usunięty.");
        return "redirect:/reservations/" + id;
    }

    @GetMapping("/{id}")
    public String details(@PathVariable Long id, Model model) {
        User currentUser = userService.getCurrentUser();
        Reservation reservation = reservationService.getDetails(id);

        boolean isReserver = reservation.getReserver().getId().equals(currentUser.getId());
        boolean isParticipant = reservation.getParticipants().stream()
                .anyMatch(p -> p.getId().equals(currentUser.getId()));

        if (!isReserver && !isParticipant) {
            throw new org.springframework.security.access.AccessDeniedException(
                    "Nie masz uprawnień do przeglądania tej rezerwacji.");
        }

        model.addAttribute("reservation", reservation);
        model.addAttribute("currentUser", currentUser);
        model.addAttribute("isReserver", isReserver);
        return "user/reservation-details";
    }

    @GetMapping("/user/my-reservations")
    public String myReservations(Model model,
                                 @PageableDefault(size = 12, sort = "date", direction = Sort.Direction.ASC) Pageable pageable) {
        User currentUser = userService.getCurrentUser();
        Page<Reservation> page = reservationService.getMyReservationsPaginated(currentUser, pageable);

        List<UserReservationView> views = page.getContent().stream()
                .map(r -> new UserReservationView(r, r.getReserver().getId().equals(currentUser.getId())))
                .collect(Collectors.toList());

        model.addAttribute("reservations", views);
        model.addAttribute("currentPage", page.getNumber());
        model.addAttribute("totalPages", page.getTotalPages());
        model.addAttribute("pageSize", pageable.getPageSize());
        return "user/my-reservations";
    }
}
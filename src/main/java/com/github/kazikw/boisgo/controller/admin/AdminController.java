package com.github.kazikw.boisgo.controller.admin;

import com.github.kazikw.boisgo.domain.ReservationStatus;
import com.github.kazikw.boisgo.domain.User;
import com.github.kazikw.boisgo.dto.BlockadeCreateRequest;
import com.github.kazikw.boisgo.service.AdminService;
import com.github.kazikw.boisgo.service.FacilityService;
import com.github.kazikw.boisgo.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;
    private final FacilityService facilityService;
    private final UserService userService;

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        User admin = userService.getCurrentUser();
        model.addAttribute("pendingReservations", adminService.getPendingReservations(admin.getId()));
        return "admin/dashboard";
    }

    @PostMapping("/reservations/{id}/confirm")
    public String confirm(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        adminService.setStatus(id, ReservationStatus.CONFIRMED);
        redirectAttributes.addFlashAttribute("successMessage", "Rezerwacja potwierdzona.");
        return "redirect:/admin/dashboard";
    }

    @PostMapping("/reservations/{id}/cancel")
    public String cancel(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        adminService.setStatus(id, ReservationStatus.CANCELLED);
        redirectAttributes.addFlashAttribute("successMessage", "Rezerwacja odwołana.");
        return "redirect:/admin/dashboard";
    }

    @PostMapping("/reservations/{id}/status")
    public String changeStatus(@PathVariable Long id, @RequestParam ReservationStatus status,
                               RedirectAttributes redirectAttributes) {
        adminService.setStatus(id, status);
        redirectAttributes.addFlashAttribute("successMessage", "Status zmieniony.");
        return "redirect:/admin/reservations";
    }

    @GetMapping("/reservations")
    public String reservations(@RequestParam(required = false) List<ReservationStatus> status,
                               @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
                               @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo,
                               Model model) {
        User admin = userService.getCurrentUser();
        model.addAttribute("reservations", adminService.filterReservations(admin.getId(), status, dateFrom, dateTo));
        return "admin/reservations";
    }

    @GetMapping("/calendar")
    public String calendar(@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
                           @RequestParam(required = false) Integer selectedDay,
                           Model model) {
        User admin = userService.getCurrentUser();
        LocalDate today = (date != null) ? date : LocalDate.now();
        LocalDate monday = today.with(DayOfWeek.MONDAY);

        List<LocalDate> weekDates = new ArrayList<>();
        for (int i = 0; i < 7; i++) weekDates.add(monday.plusDays(i));

        int selectedIndex = (selectedDay != null) ? selectedDay : 0;
        LocalDate selectedDate = weekDates.get(Math.min(selectedIndex, 6));

        model.addAttribute("weekDates", weekDates);
        model.addAttribute("selectedDate", selectedDate);
        model.addAttribute("mondayDate", monday);
        model.addAttribute("selectedDayIndex", selectedIndex);
        model.addAttribute("reservations", adminService.filterReservations(admin.getId(), null, selectedDate, selectedDate));
        return "admin/calendar";
    }

    @GetMapping("/blockades")
    public String blockades(Model model) {
        User admin = userService.getCurrentUser();
        model.addAttribute("facilities", facilityService.getByIds(adminService.getManagedFacilityIds(admin.getId())));
        model.addAttribute("activeBlockades", adminService.getActiveBlockades(admin.getId()));
        model.addAttribute("pastBlockades", adminService.getPastBlockades(admin.getId()));
        model.addAttribute("blockadeRequest", new BlockadeCreateRequest(null, null, null, ""));
        return "admin/blockades";
    }

    @PostMapping("/blockades")
    public String createBlockade(@ModelAttribute("blockadeRequest") @Valid BlockadeCreateRequest request,
                                 BindingResult bindingResult,
                                 RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Błędne dane formularza blokady.");
            return "redirect:/admin/blockades";
        }
        adminService.createBlockade(request.facilityId(), request.startDate(), request.endDate(),
                request.reason(), userService.getCurrentUser());
        redirectAttributes.addFlashAttribute("successMessage", "Blokada dodana.");
        return "redirect:/admin/blockades";
    }

    @PostMapping("/blockades/{id}/deactivate")
    public String deactivateBlockade(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        adminService.deactivateBlockade(id);
        redirectAttributes.addFlashAttribute("successMessage", "Blokada odwołana.");
        return "redirect:/admin/blockades";
    }

    @PostMapping("/facility-admins")
    public String assignFacilityAdmin(@RequestParam Long userId, @RequestParam Long facilityId,
                                      RedirectAttributes redirectAttributes) {
        adminService.assignFacilityAdmin(userId, facilityId);
        redirectAttributes.addFlashAttribute("successMessage", "Uprawnienia administratora nadane.");
        return "redirect:/admin/dashboard";
    }
}
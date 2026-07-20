package com.github.kazikw.boisgo.controller;

import com.github.kazikw.boisgo.dto.UserRegisterRequest;
import com.github.kazikw.boisgo.service.FacilityService;
import com.github.kazikw.boisgo.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
public class PublicController {

    private final FacilityService facilityService;
    private final UserService userService;

//    @GetMapping("/")
//    public String home(Model model) {
//        model.addAttribute("cities", facilityService.getCities());
//        return "unloged/home";
//    }
    @GetMapping("/")
    public String home(Model model) {
        boolean loggedIn = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication() != null
            && org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication().isAuthenticated()
            && !"anonymousUser".equals(org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication().getPrincipal());

        model.addAttribute("cities", facilityService.getCities());
        model.addAttribute("isLoggedIn", loggedIn);
    return "unloged/home";
}

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/register")
    public String registerForm(Model model) {
        model.addAttribute("registerRequest", new UserRegisterRequest("", "", ""));
        return "register";
    }

    @PostMapping("/register")
    public String register(@ModelAttribute("registerRequest") @Valid UserRegisterRequest request,
                           BindingResult bindingResult,
                           RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            return "register";
        }
        try {
            userService.register(request.firstName(), request.email(), request.password());
        } catch (IllegalStateException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/register";
        }
        redirectAttributes.addFlashAttribute("successMessage", "Rejestracja zakończona sukcesem. Możesz się zalogować.");
        return "redirect:/login";
    }
}
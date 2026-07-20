package com.github.kazikw.boisgo.controller;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(EntityNotFoundException.class)
    public String handleNotFound(EntityNotFoundException ex, Model model) {
        model.addAttribute("errorMessage", ex.getMessage());
        return "error/not-found";
    }

    @ExceptionHandler(AccessDeniedException.class)
    public String handleAccessDenied(AccessDeniedException ex, Model model) {
        model.addAttribute("errorMessage", ex.getMessage());
        return "error/access-denied";
    }

    @ExceptionHandler(IllegalStateException.class)
    public String handleIllegalState(IllegalStateException ex, RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        return "redirect:/user/dashboard";
    }
}
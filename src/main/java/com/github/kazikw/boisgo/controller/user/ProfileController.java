package com.github.kazikw.boisgo.controller.user;

import com.github.kazikw.boisgo.domain.User;
import com.github.kazikw.boisgo.service.FriendService;
import com.github.kazikw.boisgo.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/user")
@RequiredArgsConstructor
class ProfileController {

    private final UserService userService;
    private final FriendService friendService;

    @GetMapping("/my-profile")
    public String profile(Model model) {
        User currentUser = userService.getCurrentUser();
        model.addAttribute("user", currentUser);
        model.addAttribute("friends", friendService.getFriends(currentUser.getId()));
        return "user/my-profile";
    }

    @PostMapping("/friends/add")
    public String addFriend(@RequestParam String friendEmail, RedirectAttributes redirectAttributes) {
        try {
            friendService.addFriend(userService.getCurrentUser(), friendEmail);
            redirectAttributes.addFlashAttribute("successMessage", "Znajomy dodany pomyślnie!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/user/my-profile";
    }

    @PostMapping("/friends/remove")
    public String removeFriend(@RequestParam Long friendId, RedirectAttributes redirectAttributes) {
        friendService.removeFriend(userService.getCurrentUser(), friendId);
        redirectAttributes.addFlashAttribute("successMessage", "Znajomy usunięty.");
        return "redirect:/user/my-profile";
    }
}
package com.hadiid.erp.identity;

import com.hadiid.erp.identity.dto.UserCreateRequest;
import com.hadiid.erp.security.AppUserPrincipal;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/** §G/§M: Admin-only user management screens. */
@Controller
@RequestMapping("/settings/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("users", userService.listAll());
        return "settings/users";
    }

    @GetMapping("/new")
    @PreAuthorize("hasAuthority('USER_CREATE')")
    public String newForm(Model model) {
        model.addAttribute("user", new UserCreateRequest());
        return "settings/user-form";
    }

    @PostMapping
    @PreAuthorize("hasAuthority('USER_CREATE')")
    public String create(@Valid @ModelAttribute("user") UserCreateRequest req, BindingResult binding,
                          @AuthenticationPrincipal AppUserPrincipal principal, RedirectAttributes redirectAttributes) {
        if (binding.hasErrors()) {
            return "settings/user-form";
        }
        userService.create(req, principal.getUserId());
        redirectAttributes.addFlashAttribute("successMessage", "User created.");
        return "redirect:/settings/users";
    }

    @PostMapping("/{id}/disable")
    @PreAuthorize("hasAuthority('USER_DISABLE')")
    public String disable(@PathVariable Long id, @AuthenticationPrincipal AppUserPrincipal principal,
                           RedirectAttributes redirectAttributes) {
        userService.setStatus(id, "DISABLED", principal.getUserId());
        redirectAttributes.addFlashAttribute("successMessage", "User disabled.");
        return "redirect:/settings/users";
    }

    @PostMapping("/{id}/enable")
    @PreAuthorize("hasAuthority('USER_DISABLE')")
    public String enable(@PathVariable Long id, @AuthenticationPrincipal AppUserPrincipal principal,
                          RedirectAttributes redirectAttributes) {
        userService.setStatus(id, "ACTIVE", principal.getUserId());
        redirectAttributes.addFlashAttribute("successMessage", "User enabled.");
        return "redirect:/settings/users";
    }
}

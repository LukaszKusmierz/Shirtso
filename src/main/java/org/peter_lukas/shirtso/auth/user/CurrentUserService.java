package org.peter_lukas.shirtso.auth.user;

import org.peter_lukas.shirtso.auth.config.CachedUserDetailsService;
import org.peter_lukas.shirtso.auth.validation.UserNotFoundException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import static org.peter_lukas.shirtso.messages.Alerts.USER_NOT_FOUND;

@Service
public class CurrentUserService {

    public User getCurrentUser() throws UserNotFoundException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new UserNotFoundException(USER_NOT_FOUND);
        }

        Object principal = authentication.getPrincipal();

        if (principal instanceof CachedUserDetailsService.CustomUserDetails customUserDetails) {
            return customUserDetails.user();
        }

        throw new UserNotFoundException(USER_NOT_FOUND);
    }

    public String getCurrentUserEmail() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null ? authentication.getName() : null;
    }
}
package org.peter_lukas.shirtso.auth.config;

import org.peter_lukas.shirtso.auth.user.User;
import org.peter_lukas.shirtso.auth.user.UserRepository;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collection;

@Service
public class CachedUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public CachedUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    @Cacheable(value = "usersByEmail", key = "#usernameOrEmail")
    public UserDetails loadUserByUsername(String usernameOrEmail) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(usernameOrEmail)
                .orElseThrow(() -> new UsernameNotFoundException("User with " + usernameOrEmail + " not found"));

        return new CustomUserDetails(user);
    }

    public record CustomUserDetails(User user) implements UserDetails {

        @Override
            public Collection<? extends GrantedAuthority> getAuthorities() {
                return user.getRoles().stream()
                        .map(r -> new SimpleGrantedAuthority("ROLE_" + r.getName()))
                        .toList();
            }

            @Override
            public String getPassword() {
                return user.getPassword();
            }

            @Override
            public String getUsername() {
                return user.getUserName();
            }

            @Override
            public boolean isAccountNonExpired() {
                return true;
            }

            @Override
            public boolean isAccountNonLocked() {
                return true;
            }

            @Override
            public boolean isCredentialsNonExpired() {
                return true;
            }

            @Override
            public boolean isEnabled() {
                return true;
            }
    }
}

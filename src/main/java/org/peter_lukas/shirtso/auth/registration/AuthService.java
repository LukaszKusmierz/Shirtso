package org.peter_lukas.shirtso.auth.registration;

import org.peter_lukas.shirtso.auth.config.SpringSecurityConfig;
import org.peter_lukas.shirtso.auth.user.Role;
import org.peter_lukas.shirtso.auth.user.RoleRepository;
import org.peter_lukas.shirtso.auth.user.User;
import org.peter_lukas.shirtso.auth.user.UserRepository;
import org.peter_lukas.shirtso.auth.validation.UserAlreadyExistsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class AuthService {

    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;


    public AuthService(PasswordEncoder passwordEncoder, UserRepository userRepository, RoleRepository roleRepository) {
        this.passwordEncoder = passwordEncoder;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
    }

    @Transactional
    public RegisterUserDataDto registerNewUser(NewUserRegistrationDto registrationDto) {
        verifyEmail(registrationDto);

        User newUser = new User(
                registrationDto.username(),
                registrationDto.email(),
                passwordEncoder.encode(registrationDto.password())
        );

        Role userRole = roleRepository.findByName(SpringSecurityConfig.USER_READ)
                .orElseThrow(() -> new IllegalStateException("User Role not found")
        );

        newUser.addRole(userRole);
        userRole.assignToUser(newUser);
        User saved = userRepository.save(newUser);

        return new RegisterUserDataDto(saved.getUserId(), saved.getUserName(), saved.getEmail());
    }

    private void verifyEmail(NewUserRegistrationDto registrationDto) {
        userRepository.findByEmail(registrationDto.email()).ifPresent(
                user -> {throw new UserAlreadyExistsException(registrationDto.username());}
        );

        userRepository.findByEmail(registrationDto.username()).ifPresent(
                user -> {throw new UserAlreadyExistsException("User with username " + registrationDto.username() + " already exists");}
        );
    }

    public RegisterUserDataDto getCurrentUser() throws UserNotFoundException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        return new RegisterUserDataDto(
                user.getUserId(),
                user.getUserName(),
                user.getEmail(),
                user.getRoles().stream()
                        .map(Role::getName)
                        .collect(Collectors.toSet())
        );
    }
}

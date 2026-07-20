package com.github.kazikw.boisgo.security;

import com.github.kazikw.boisgo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AppUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findByFirstName(username)
                .map(AppUserDetails::new)
                .orElseThrow(() -> new UsernameNotFoundException("Nie znaleziono użytkownika: " + username));
    }
}
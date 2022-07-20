package com.ceos.bankids.service;

import com.ceos.bankids.exception.NotFoundException;
import com.ceos.bankids.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class CustomUserDetailServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return (UserDetails) userRepository.findById(Long.parseLong(username))
            .orElseThrow(() -> new NotFoundException("사용자를 찾을 수 없습니다."));
    }
}

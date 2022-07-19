package com.ceos.bankids.unit.service;

import com.ceos.bankids.domain.User;
import com.ceos.bankids.exception.NotFoundException;
import com.ceos.bankids.repository.UserRepository;
import com.ceos.bankids.service.CustomUserDetailServiceImpl;
import java.util.Optional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.security.core.userdetails.UserDetails;

public class CustomUserDetailServiceTest {

    @Test
    @DisplayName("유저 아이디로 유저 조회 성공 시, 결과 반환하는지 확인")
    public void testIfLoadByUsernameSucceedThenReturnResult() {
        // given
        User user = User.builder()
            .username("ozzing")
            .isFemale(null)
            .authenticationCode("1234")
            .provider("kakao")
            .isKid(null)
            .refreshToken("rT")
            .build();
        UserRepository mockUserRepository = Mockito.mock(UserRepository.class);
        Mockito.when(mockUserRepository.findById(5L)).thenReturn(Optional.ofNullable(user));

        // when
        CustomUserDetailServiceImpl mockCustomUserDetailServiceImpl = new CustomUserDetailServiceImpl(
            mockUserRepository
        );
        UserDetails result = mockCustomUserDetailServiceImpl.loadUserByUsername("5");

        // then
        Assertions.assertEquals(user, result);
    }

    @Test
    @DisplayName("유저 아이디로 유저 조회 실패 시, 에러 처리하는지 확인")
    public void testIfLoadByUsernameFailWithoutUserThenThrowNotFoundException() {
        // given
        UserRepository mockUserRepository = Mockito.mock(UserRepository.class);
        Mockito.when(mockUserRepository.findById(5L)).thenReturn(Optional.ofNullable(null));

        // when
        CustomUserDetailServiceImpl mockCustomUserDetailServiceImpl = new CustomUserDetailServiceImpl(
            mockUserRepository
        );

        // then
        Assertions.assertThrows(NotFoundException.class, () -> {
            mockCustomUserDetailServiceImpl.loadUserByUsername("5");
        });
    }
}

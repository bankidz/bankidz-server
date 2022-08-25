package com.ceos.bankids.service;

import com.ceos.bankids.constant.ErrorCode;
import com.ceos.bankids.dto.oauth.AppleKeyDTO;
import com.ceos.bankids.exception.BadRequestException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = false)

public class AppleServiceImpl implements AppleService {

    private final WebClient webClient;

    @Override
    public AppleKeyDTO getAppleIdentityToken() {
        String getKeyURL = "https://appleid.apple.com/auth/keys";

        WebClient.ResponseSpec responseSpec = webClient.get().uri(getKeyURL).retrieve();

        try {
            AppleKeyDTO appleKeyDTO = responseSpec.bodyToMono(AppleKeyDTO.class).block();
            return appleKeyDTO;
        } catch (Exception e) {
            e.printStackTrace();
            throw new BadRequestException(ErrorCode.APPLE_BAD_REQUEST.getErrorCode());
        }
    }
}

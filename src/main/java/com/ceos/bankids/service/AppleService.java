package com.ceos.bankids.service;

import com.ceos.bankids.dto.oauth.AppleKeyDTO;
import org.springframework.stereotype.Service;

@Service
public interface AppleService {

    public AppleKeyDTO getAppleIdentityToken();
}

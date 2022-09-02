package com.ceos.bankids.service;

import com.ceos.bankids.controller.request.AppleRequest;
import com.ceos.bankids.dto.oauth.AppleKeyListDTO;
import com.ceos.bankids.dto.oauth.AppleSubjectDTO;
import com.ceos.bankids.dto.oauth.AppleTokenDTO;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMap;

@Service
public interface AppleService {

    public AppleKeyListDTO getAppleIdentityToken();

    public AppleSubjectDTO verifyIdentityToken(AppleRequest appleRequest,
        AppleKeyListDTO appleKeyListDTO);

    public AppleTokenDTO getAppleAccessToken(AppleRequest appleRequest);

    public Object revokeAppleAccount(AppleTokenDTO appleTokenDTO);

    public AppleRequest getAppleRequest(MultiValueMap<String, String> formData);
}

package com.ceos.bankids.service;

import com.ceos.bankids.controller.request.AppleRequest;
import com.ceos.bankids.dto.oauth.AppleKeyDTO;
import io.jsonwebtoken.Claims;
import org.springframework.stereotype.Service;

@Service
public interface AppleService {

    public AppleKeyDTO getAppleIdentityToken();

    public Claims verifyIdentityToken(AppleRequest appleRequest,
        AppleKeyDTO appleKeyDTO);

//    public JSONObject decodeIdToken(AppleRequest appleRequest);
//
//    public AppleTokenDTO getAppleAccessToken(AppleRequest appleRequest);
//

}

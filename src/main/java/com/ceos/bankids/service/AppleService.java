package com.ceos.bankids.service;

import com.ceos.bankids.controller.request.AppleRequest;
import com.ceos.bankids.dto.oauth.AppleKeyListDTO;
import io.jsonwebtoken.Claims;
import org.springframework.stereotype.Service;

@Service
public interface AppleService {

    public AppleKeyListDTO getAppleIdentityToken();

    public Claims verifyIdentityToken(AppleRequest appleRequest,
        AppleKeyListDTO appleKeyListDTO);

//    public JSONObject decodeIdToken(AppleRequest appleRequest);
//
//    public AppleTokenDTO getAppleAccessToken(AppleRequest appleRequest);
//

}

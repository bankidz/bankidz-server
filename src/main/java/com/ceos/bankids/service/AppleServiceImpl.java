package com.ceos.bankids.service;

import com.ceos.bankids.constant.ErrorCode;
import com.ceos.bankids.controller.request.AppleRequest;
import com.ceos.bankids.dto.oauth.AppleHeaderDTO;
import com.ceos.bankids.dto.oauth.AppleKeyDTO;
import com.ceos.bankids.dto.oauth.AppleKeyListDTO;
import com.ceos.bankids.exception.BadRequestException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.RSAPublicKeySpec;
import java.text.ParseException;
import java.util.Base64;
import java.util.Date;
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
    public AppleKeyListDTO getAppleIdentityToken() {
        String getKeyURL = "https://appleid.apple.com/auth/keys";

        WebClient.ResponseSpec responseSpec = webClient.get().uri(getKeyURL).retrieve();

        try {
            AppleKeyListDTO appleKeyListDTO = responseSpec.bodyToMono(AppleKeyListDTO.class)
                .block();
            return appleKeyListDTO;
        } catch (Exception e) {
            e.printStackTrace();
            throw new BadRequestException(ErrorCode.APPLE_BAD_REQUEST.getErrorCode());
        }
    }

    @Override
    public Claims verifyIdentityToken(AppleRequest appleRequest, AppleKeyListDTO appleKeyListDTO) {
        SignedJWT signedJWT = null;
        JWTClaimsSet payload = null;
        PublicKey publicKey = null;
        try {
            signedJWT = SignedJWT.parse(appleRequest.getIdToken());
            payload = signedJWT.getJWTClaimsSet();

            Date currentTime = new Date(System.currentTimeMillis());
            if (!currentTime.before(payload.getExpirationTime())) {
                throw new BadRequestException(ErrorCode.APPLE_TOKEN_EXPIRED.getErrorCode());
            }

            String headerOfIdentityToken = appleRequest.getIdToken()
                .substring(0, appleRequest.getIdToken().indexOf("."));

            AppleHeaderDTO appleHeaderDTO = new ObjectMapper().readValue(
                new String(Base64.getDecoder().decode(headerOfIdentityToken), "UTF-8"),
                AppleHeaderDTO.class);
            AppleKeyDTO appleKeyDTO = appleKeyListDTO.getMatchedKeyBy(
                    appleHeaderDTO.getKid(),
                    appleHeaderDTO.getAlg())
                .orElseThrow(
                    () -> new BadRequestException(ErrorCode.APPLE_KEY_UNAVAILABLE.getErrorCode()));

            byte[] nBytes = Base64.getUrlDecoder().decode(appleKeyDTO.getN());
            byte[] eBytes = Base64.getUrlDecoder().decode(appleKeyDTO.getE());

            BigInteger n = new BigInteger(1, nBytes);
            BigInteger e = new BigInteger(1, eBytes);

            RSAPublicKeySpec publicKeySpec = new RSAPublicKeySpec(n, e);
            KeyFactory keyFactory = null;
            keyFactory = KeyFactory.getInstance(appleKeyDTO.getKty());
            publicKey = keyFactory.generatePublic(publicKeySpec);

        } catch (ParseException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException ex) {
            ex.printStackTrace();
        } catch (InvalidKeySpecException ex) {
            ex.printStackTrace();
        } catch (JsonMappingException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return Jwts.parser().setSigningKey(publicKey).parseClaimsJws(appleRequest.getIdToken())
            .getBody();
    }
}

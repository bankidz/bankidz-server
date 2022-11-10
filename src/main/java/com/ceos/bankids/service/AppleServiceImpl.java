package com.ceos.bankids.service;

import com.ceos.bankids.constant.ErrorCode;
import com.ceos.bankids.controller.request.AppleRequest;
import com.ceos.bankids.dto.oauth.AppleHeaderDTO;
import com.ceos.bankids.dto.oauth.AppleKeyDTO;
import com.ceos.bankids.dto.oauth.AppleKeyListDTO;
import com.ceos.bankids.dto.oauth.AppleSubjectDTO;
import com.ceos.bankids.dto.oauth.AppleTokenDTO;
import com.ceos.bankids.exception.BadRequestException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.RSAPublicKeySpec;
import java.text.ParseException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Base64;
import java.util.Date;
import java.util.LinkedHashMap;
import lombok.RequiredArgsConstructor;
import org.apache.tomcat.util.json.JSONParser;
import org.bouncycastle.util.io.pem.PemObject;
import org.bouncycastle.util.io.pem.PemReader;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

@Service
@RequiredArgsConstructor
public class AppleServiceImpl implements AppleService {

    private final WebClient webClient;

    @Value("${apple.team.id}")
    private String APPLE_TEAM_ID;
    @Value("${apple.client.id}")
    private String APPLE_CLIENT_ID;
    @Value("${apple.redirect.uri}")
    private String APPLE_URI;
    @Value("${apple.key.id}")
    private String APPLE_KEY;
    @Value("${apple.key.path}")
    private String APPLE_KEY_PATH;
    @Value("${apple.nonce}")
    private String APPLE_NONCE;
    private String APPLE_SECRET;

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
    public AppleSubjectDTO verifyIdentityToken(AppleRequest appleRequest,
        AppleKeyListDTO appleKeyListDTO) {
        PublicKey publicKey = null;
        try {
            SignedJWT signedJWT = SignedJWT.parse(appleRequest.getIdToken());
            JWTClaimsSet payload = signedJWT.getJWTClaimsSet();

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
            KeyFactory keyFactory = KeyFactory.getInstance(appleKeyDTO.getKty());
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
        Claims claims = Jwts.parser().setSigningKey(publicKey)
            .parseClaimsJws(appleRequest.getIdToken())
            .getBody();
        if (!claims.get("nonce").equals(APPLE_NONCE)) {
            throw new BadRequestException(ErrorCode.APPLE_NONCE_INCORRECT.getErrorCode());
        }
        return new AppleSubjectDTO(claims.getSubject());
    }

    @Override
    public AppleTokenDTO getAppleAccessToken(AppleRequest appleRequest, String option) {
        APPLE_SECRET = makeClientSecret();

        String getTokenURL =
            "https://appleid.apple.com/auth/token?client_id=" + APPLE_CLIENT_ID + "&client_secret="
                + APPLE_SECRET + "&grant_type=authorization_code&code=" + appleRequest.getCode()
                + "&redirect_uri=" + APPLE_URI + option;

        WebClient.ResponseSpec responseSpec = webClient.post().uri(getTokenURL).retrieve();

        try {
            AppleTokenDTO appleTokenDTO = responseSpec.bodyToMono(AppleTokenDTO.class).block();
            return appleTokenDTO;
        } catch (Exception e) {
            e.printStackTrace();
            throw new BadRequestException(ErrorCode.APPLE_ACCESS_TOKEN_ERROR.getErrorCode());
        }
    }

    private String makeClientSecret() {
        Date expirationDate = Date.from(
            LocalDateTime.now().plusDays(30).atZone(ZoneId.systemDefault()).toInstant());
        return Jwts.builder()
            .setHeaderParam("kid", APPLE_KEY)
            .setHeaderParam("alg", "ES256")
            .setIssuer(APPLE_TEAM_ID)
            .setIssuedAt(new Date(System.currentTimeMillis()))
            .setExpiration(expirationDate)
            .setAudience("https://appleid.apple.com")
            .setSubject(APPLE_CLIENT_ID)
            .signWith(SignatureAlgorithm.ES256, getPrivateKey())
            .compact();
    }

    private PrivateKey getPrivateKey() {

        byte[] content = null;
        KeyFactory factory = null;
        PKCS8EncodedKeySpec priKeySpec = null;

        try (InputStreamReader keyReader = new InputStreamReader(
            new ClassPathResource(APPLE_KEY_PATH).getInputStream());
            PemReader pemReader = new PemReader(keyReader)) {
            {
                factory = KeyFactory.getInstance("EC");

                PemObject pemObject = pemReader.readPemObject();
                content = pemObject.getContent();
                priKeySpec = new PKCS8EncodedKeySpec(content);
                return factory.generatePrivate(priKeySpec);
            }
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            throw new BadRequestException("NoSuchAlgorithmException");
        } catch (InvalidKeySpecException e) {
            e.printStackTrace();
            throw new BadRequestException("InvalidKeySpecException");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            throw new BadRequestException("FileNotFoundException");
        } catch (IOException e) {
            e.printStackTrace();
            throw new BadRequestException("IOException");
        }
    }

    @Override
    public Object revokeAppleAccount(AppleTokenDTO appleTokenDTO) {
        APPLE_SECRET = makeClientSecret();

        String revokeURL = "https://appleid.apple.com/auth/revoke";

        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("client_id", APPLE_CLIENT_ID);
        formData.add("client_secret", APPLE_SECRET);
        formData.add("token", appleTokenDTO.getRefreshToken());

        WebClient.ResponseSpec responseSpec = webClient.post().uri(revokeURL)
            .header("Content-Type", "application/x-www-form-urlencoded")
            .body(BodyInserters.fromFormData(formData))
            .retrieve();

        try {
            Object appleResponse = responseSpec.bodyToMono(Object.class).block();
            return appleResponse;
        } catch (Exception e) {
            e.printStackTrace();
            throw new BadRequestException(ErrorCode.APPLE_BAD_REQUEST.getErrorCode());
        }
    }

    @Override
    public AppleRequest getAppleRequest(MultiValueMap<String, String> formData) {
        AppleRequest appleRequest = new AppleRequest(formData.get("code").get(0),
            formData.get("id_token").get(0), null);
        if (formData.get("user") != null) {
            String userString = formData.get("user").get(0);
            JSONParser userParser = new JSONParser(userString);
            Object obj = null;
            try {
                obj = userParser.parse();
            } catch (org.apache.tomcat.util.json.ParseException e) {
                e.printStackTrace();
            }
            LinkedHashMap<String, LinkedHashMap<String, String>> userObject = (LinkedHashMap<String, LinkedHashMap<String, String>>) obj;
            appleRequest.setUsername(userObject.get("name").get("lastName") + userObject.get("name")
                .get("firstName"));
        }
        return appleRequest;
    }
}

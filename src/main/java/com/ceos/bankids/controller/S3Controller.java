package com.ceos.bankids.controller;

import com.ceos.bankids.config.CommonResponse;
import com.ceos.bankids.domain.User;
import com.ceos.bankids.dto.PreSignedDTO;
import com.ceos.bankids.service.S3ServiceImpl;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/s3")
@RequiredArgsConstructor
public class S3Controller {

    private final S3ServiceImpl s3Service;

    @ApiOperation(value = "preSignedUrl 받아오기")
    @GetMapping(value = "url", produces = "application/json; charset=utf-8")
    public CommonResponse<PreSignedDTO> getPreSignedUrl(@AuthenticationPrincipal User authUser) {

        log.info("api = preSignedUrl 받아오기 user = {}", authUser.getUsername());
        PreSignedDTO preSignedDTO = s3Service.readPreSignedUrl(authUser);

        return CommonResponse.onSuccess(preSignedDTO);
    }
}

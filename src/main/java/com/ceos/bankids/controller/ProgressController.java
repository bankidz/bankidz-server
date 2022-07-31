package com.ceos.bankids.controller;

import com.ceos.bankids.config.CommonResponse;
import com.ceos.bankids.domain.User;
import com.ceos.bankids.dto.ProgressDTO;
import com.ceos.bankids.service.ProgressServiceImpl;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/progress")
@RequiredArgsConstructor
public class ProgressController {

    private final ProgressServiceImpl progressService;

    @ApiOperation(value = "돈길 걷기")
    @PatchMapping(value = "/{challengeId}", produces = "application/json; charset=utf-8")
    public CommonResponse<ProgressDTO> patchProgress(@AuthenticationPrincipal User authUser,
        @PathVariable Long challengeId) {

        log.info("api = 돈길 걷기, user = {}, challengeId = {}", authUser, challengeId);
        ProgressDTO progressDTO = progressService.updateProgress(authUser, challengeId);

        return CommonResponse.onSuccess(progressDTO);
    }
}

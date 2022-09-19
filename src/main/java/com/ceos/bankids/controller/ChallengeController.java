package com.ceos.bankids.controller;

import com.ceos.bankids.config.CommonResponse;
import com.ceos.bankids.domain.User;
import com.ceos.bankids.dto.ChallengeDTO;
import com.ceos.bankids.mapper.ChallengeMapper;
import com.ceos.bankids.mapper.request.ChallengeRequest;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/challenge")
@RequiredArgsConstructor
public class ChallengeController {

    private final ChallengeMapper challengeMapper;

    @ApiOperation(value = "돈길 생성")
    @PostMapping(produces = "application/json; charset=utf-8")
    public CommonResponse<ChallengeDTO> postChallenge(@AuthenticationPrincipal User authUser,
        @RequestBody
            ChallengeRequest challengeRequest) {

        log.info("api = 돈길 생성, req = {}", challengeRequest);

        ChallengeDTO challengeDTO = challengeMapper.postChallenge(authUser, challengeRequest);

        return CommonResponse.onSuccess(challengeDTO);
    }

}

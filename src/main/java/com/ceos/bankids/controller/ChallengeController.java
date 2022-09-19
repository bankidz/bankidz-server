package com.ceos.bankids.controller;

import com.ceos.bankids.config.CommonResponse;
import com.ceos.bankids.domain.User;
import com.ceos.bankids.dto.ChallengeDTO;
import com.ceos.bankids.dto.KidChallengeListDTO;
import com.ceos.bankids.mapper.ChallengeMapper;
import com.ceos.bankids.mapper.request.ChallengeRequest;
import io.swagger.annotations.ApiOperation;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
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

    @ApiOperation(value = "돈길 포기하기")
    @DeleteMapping(value = "/{challengeId}", produces = "application/json; charset=utf-8")
    public CommonResponse<ChallengeDTO> deleteChallenge(@AuthenticationPrincipal User authUser,
        @PathVariable Long challengeId) {

        log.info("api = 돈길 포기하기, user = {} challengeId = {}", authUser.getUsername(), challengeId);

        ChallengeDTO challengeDTO = challengeMapper.deleteChallenge(authUser, challengeId);

        return CommonResponse.onSuccess(challengeDTO);
    }

    @ApiOperation(value = "돈길 리스트 가져오기")
    @GetMapping(produces = "application/json; charset=utf-8")
    public CommonResponse<List<ChallengeDTO>> getListChallenge(
        @AuthenticationPrincipal User authUser, @RequestParam String status) {

        log.info("api = 돈길 리스트 가져오기, user = {}, status = {}", authUser.getUsername(), status);

        List<ChallengeDTO> challengeDTOList = challengeMapper.getListChallenge(authUser, status);

        return CommonResponse.onSuccess(challengeDTOList);
    }

    @ApiOperation(value = "자녀의 돈길 리스트 가져오기")
    @GetMapping(value = "/kid/{kidId}", produces = "application/json; charset=utf-8")
    public CommonResponse<KidChallengeListDTO> getListKidChallenge(
        @AuthenticationPrincipal User authUser, @PathVariable Long kidId,
        @RequestParam String status) {

        log.info("api = 자녀의 돈길 리스트 가져오기, user = {}, kidId = {}, status = {}",
            authUser.getUsername(), kidId, status);

        KidChallengeListDTO kidChallengeListDTO = challengeMapper.getListKidChallenge(authUser,
            kidId,
            status);

        return CommonResponse.onSuccess(kidChallengeListDTO);
    }

}

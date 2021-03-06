package com.ceos.bankids.controller;

import com.ceos.bankids.config.CommonResponse;
import com.ceos.bankids.controller.request.ChallengeRequest;
import com.ceos.bankids.controller.request.KidChallengeRequest;
import com.ceos.bankids.domain.User;
import com.ceos.bankids.dto.ChallengeDTO;
import com.ceos.bankids.dto.KidChallengeListDTO;
import com.ceos.bankids.dto.WeekDTO;
import com.ceos.bankids.service.ChallengeServiceImpl;
import io.swagger.annotations.ApiOperation;
import java.util.List;
import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
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

    private final ChallengeServiceImpl challengeService;

    @ApiOperation(value = "돈길 생성")
    @PostMapping(produces = "application/json; charset=utf-8")
    public CommonResponse<ChallengeDTO> postChallenge(@AuthenticationPrincipal User authUser,
        @Valid @RequestBody ChallengeRequest challengeRequest, BindingResult bindingResult) {

        log.info("api = 돈길 생성, req = {}", challengeRequest);
        ChallengeDTO challengeDTO = challengeService.createChallenge(authUser, challengeRequest);
        return CommonResponse.onSuccess(challengeDTO);
    }

    @ApiOperation(value = "돈길 정보 가져오기")
    @GetMapping(value = "/{challengeId}", produces = "application/json; charset=utf-8")
    public CommonResponse<ChallengeDTO> getChallenge(@AuthenticationPrincipal User authUser,
        @PathVariable Long challengeId) {

        log.info("api = 돈길 정보 가져오기, user = {}", authUser.getUsername());
        ChallengeDTO challengeDTO = challengeService.detailChallenge(authUser, challengeId);

        return CommonResponse.onSuccess(challengeDTO);
    }

    @ApiOperation(value = "돈길 포기하기")
    @DeleteMapping(value = "/{challengeId}", produces = "application/json; charset=utf-8")
    public CommonResponse<ChallengeDTO> deleteChallenge(
        @AuthenticationPrincipal User authUser,
        @PathVariable Long challengeId) {

        log.info("api = 돈길 포기하기, user = {} challengeId = {}", authUser.getUsername(), challengeId);
        ChallengeDTO deleteChallengeDTO = challengeService.deleteChallenge(authUser,
            challengeId);

        return CommonResponse.onSuccess(deleteChallengeDTO);
    }

    @ApiOperation(value = "돈길 리스트 가져오기")
    @GetMapping(produces = "application/json; charset=utf-8")
    public CommonResponse<List<ChallengeDTO>> getListChallenge(
        @AuthenticationPrincipal User authUser, @RequestParam String status) {

        log.info("api = 돈길 리스트 가져오기, user = {}, status = {}", authUser.getUsername(), status);
        List<ChallengeDTO> challengeList = challengeService.readChallenge(authUser, status);

        return CommonResponse.onSuccess(challengeList);
    }

    @ApiOperation(value = "자녀의 돈길 리스트 가져오기")
    @GetMapping(value = "/kid", produces = "application/json; charset=utf-8")
    public CommonResponse<List<KidChallengeListDTO>> getListKidChallenge(
        @AuthenticationPrincipal User authUser) {

        log.info("api = 자녀의 돈길 리스트 가져오기, user = {}", authUser.getUsername());
        List<KidChallengeListDTO> kidChallengeList = challengeService.readKidChallenge(authUser);

        return CommonResponse.onSuccess(kidChallengeList);
    }

    @ApiOperation(value = "자녀의 돈길 수락 / 거절")
    @PatchMapping(value = "/{challengeId}", produces = "application/json; charset=utf-8")
    public CommonResponse<ChallengeDTO> patchChallengeStatus(@AuthenticationPrincipal User authUser,
        @PathVariable Long challengeId,
        @Valid @RequestBody KidChallengeRequest kidChallengeRequest) {

        log.info("api = 자녀의 돈길 수락 / 거절, user = {}, challengeId = {}, 수락여부 = {}",
            authUser.getUsername(), challengeId, kidChallengeRequest.getAccept());
        ChallengeDTO challengeDTO = challengeService.updateChallengeStatus(authUser, challengeId,
            kidChallengeRequest);

        return CommonResponse.onSuccess(challengeDTO);
    }

    @ApiOperation(value = "주차 정보 가져오기")
    @GetMapping(value = "/progress", produces = "application/json; charset=utf-8")
    public CommonResponse<WeekDTO> getWeekInfo(@AuthenticationPrincipal User authUser) {

        log.info("api = 주차 정보 가져오기, user = {}", authUser.getUsername());
        WeekDTO weekDTO = challengeService.readWeekInfo(authUser);

        return CommonResponse.onSuccess(weekDTO);
    }

    @ApiOperation(value = "자녀의 주차 정보 가져오기")
    @GetMapping(value = "/kid/progress", produces = "application/json; charset=utf-8")
    public CommonResponse<WeekDTO> getKidWeekInfo(@AuthenticationPrincipal User authUser,
        @RequestParam String kidName) {

        log.info("api = 자녀의 주차 정보 가져오기, user = {}, kid = {}", authUser.getUsername(), kidName);
        WeekDTO weekDTO = challengeService.readKidWeekInfo(authUser, kidName);

        return CommonResponse.onSuccess(weekDTO);
    }
}

package com.ceos.bankids.controller;

import com.ceos.bankids.config.CommonResponse;
import com.ceos.bankids.controller.request.ChallengeRequest;
import com.ceos.bankids.controller.request.KidChallengeRequest;
import com.ceos.bankids.domain.User;
import com.ceos.bankids.dto.AchievedChallengeDTO;
import com.ceos.bankids.dto.AchievedChallengeListDTO;
import com.ceos.bankids.dto.ChallengeDTO;
import com.ceos.bankids.dto.KidChallengeListDTO;
import com.ceos.bankids.dto.KidWeekDTO;
import com.ceos.bankids.dto.WeekDTO;
import com.ceos.bankids.service.ChallengeServiceImpl;
import io.swagger.annotations.ApiOperation;
import java.util.List;
import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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
        @Valid @RequestBody ChallengeRequest challengeRequest) {

        log.info("api = 돈길 생성, req = {}", challengeRequest);
        ChallengeDTO challengeDTO = challengeService.createChallenge(authUser, challengeRequest);
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
    @GetMapping(value = "/kid/{kidId}", produces = "application/json; charset=utf-8")
    public CommonResponse<KidChallengeListDTO> getListKidChallenge(
        @AuthenticationPrincipal User authUser, @PathVariable Long kidId,
        @RequestParam String status) {

        log.info("api = 자녀의 돈길 리스트 가져오기, user = {}, kidId = {}, status = {}",
            authUser.getUsername(), kidId, status);
        KidChallengeListDTO kidChallengeList = challengeService.readKidChallenge(authUser, kidId,
            status);

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
    @GetMapping(value = "/kid/progress/{kidId}", produces = "application/json; charset=utf-8")
    public CommonResponse<KidWeekDTO> getKidWeekInfo(@AuthenticationPrincipal User authUser,
        @PathVariable Long kidId) {

        log.info("api = 자녀의 주차 정보 가져오기, user = {}, kid = {}", authUser.getUsername(), kidId);
        KidWeekDTO kidWeekDTO = challengeService.readKidWeekInfo(authUser, kidId);

        return CommonResponse.onSuccess(kidWeekDTO);
    }

    @ApiOperation(value = "완주한 돈길 리스트 가져오기")
    @GetMapping(value = "/achieved", produces = "application/json; charset=utf-8")
    public CommonResponse<AchievedChallengeListDTO> getAchievedListChallenge(
        @AuthenticationPrincipal User authUser, @RequestParam String interestPayment) {

        log.info("api = 완주한 돈길 리스트 가져오기, user = {}", authUser.getUsername());
        AchievedChallengeListDTO achievedChallengeListDTO = challengeService.readAchievedChallenge(
            authUser,
            interestPayment);

        return CommonResponse.onSuccess(achievedChallengeListDTO);
    }

    @ApiOperation(value = "완주한 돈길에 이자 지급하기")
    @PatchMapping(value = "/interestPayment/{challengeId}", produces = "application/json; charset=utf-8")
    public CommonResponse<AchievedChallengeDTO> patchInterestPayment(
        @AuthenticationPrincipal User authUser,
        @PathVariable Long challengeId) {

        log.info("api = 완주한 돈길에 이자 지급, user = {}, challengeId = {}", authUser.getUsername(),
            challengeId);
        AchievedChallengeDTO achievedChallengeDTO = challengeService.updateChallengeInterestPayment(
            authUser,
            challengeId);

        return CommonResponse.onSuccess(achievedChallengeDTO);
    }
}

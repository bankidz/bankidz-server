package com.ceos.bankids.controller;

import com.ceos.bankids.config.CommonResponse;
import com.ceos.bankids.controller.request.ChallengeRequest;
import com.ceos.bankids.domain.Challenge;
import com.ceos.bankids.domain.ChallengeCategory;
import com.ceos.bankids.domain.User;
import com.ceos.bankids.dto.ChallengeDTO;
import com.ceos.bankids.exception.BadRequestException;
import com.ceos.bankids.repository.ChallengeCategoryRepository;
import com.ceos.bankids.repository.ChallengeRepository;
import com.ceos.bankids.service.ChallengeServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

@Slf4j
@RestController
@RequestMapping("/challenge")
@RequiredArgsConstructor
public class ChallengeController {

    private final ChallengeServiceImpl challengeService;

    @PostMapping(produces = "application/json; charset=utf-8")
    public CommonResponse postChallenge(@AuthenticationPrincipal User authUser,
            @Valid @RequestBody ChallengeRequest challengeRequest, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            throw new BadRequestException(bindingResult.getFieldError().getDefaultMessage());
        }
        ChallengeDTO challengeDTO = challengeService.createChallenge(challengeRequest);
        return CommonResponse.onSuccess(challengeDTO);
    }
}

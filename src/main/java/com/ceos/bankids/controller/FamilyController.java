package com.ceos.bankids.controller;

import com.ceos.bankids.config.CommonResponse;
import com.ceos.bankids.controller.request.FamilyRequest;
import com.ceos.bankids.domain.Family;
import com.ceos.bankids.domain.FamilyUser;
import com.ceos.bankids.domain.User;
import com.ceos.bankids.dto.FamilyDTO;
import com.ceos.bankids.dto.FamilyUserDTO;
import com.ceos.bankids.dto.KidListDTO;
import com.ceos.bankids.service.ChallengeServiceImpl;
import com.ceos.bankids.service.FamilyServiceImpl;
import com.ceos.bankids.service.FamilyUserServiceImpl;
import io.swagger.annotations.ApiOperation;
import java.util.List;
import java.util.stream.Collectors;
import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Slf4j
@Controller
@RequestMapping("/family")
@RequiredArgsConstructor
public class FamilyController {

    private final FamilyServiceImpl familyService;
    private final FamilyUserServiceImpl familyUserService;
    private final ChallengeServiceImpl challengeService;

    @ApiOperation(value = "가족 생성하기")
    @PostMapping(value = "", produces = "application/json; charset=utf-8")
    @ResponseBody
    public CommonResponse<FamilyDTO> postFamily(@AuthenticationPrincipal User authUser) {

        log.info("api = 가족 생성하기, user = {}", authUser.getUsername());

        familyUserService.checkIfFamilyExists(authUser);

        Family family = familyService.postNewFamily(authUser);
        familyUserService.postNewFamilyUser(family, authUser);

        return CommonResponse.onSuccess(new FamilyDTO(family, List.of()));
    }

    @ApiOperation(value = "가족 정보 조회하기")
    @GetMapping(value = "", produces = "application/json; charset=utf-8")
    @ResponseBody
    public CommonResponse<FamilyDTO> getFamily(@AuthenticationPrincipal User authUser) {

        log.info("api = 가족 정보 조회하기, user = {}", authUser.getUsername());

        FamilyDTO familyDTO = familyService.getFamily(authUser);

        return CommonResponse.onSuccess(familyDTO);
    }

    @ApiOperation(value = "아이들 목록 조회하기")
    @GetMapping(value = "/kid", produces = "application/json; charset=utf-8")
    @ResponseBody
    public CommonResponse<List<KidListDTO>> getFamilyKidList(
        @AuthenticationPrincipal User authUser) {

        log.info("api = 아이들 목록 조회하기, user = {}", authUser.getUsername());

        List<KidListDTO> kidListDTOList = familyService.getKidListFromFamily(authUser);

        return CommonResponse.onSuccess(kidListDTOList);
    }

    @ApiOperation(value = "가족 참여하기")
    @PostMapping(value = "/user", produces = "application/json; charset=utf-8")
    @ResponseBody
    public CommonResponse<FamilyDTO> postFamilyUser(@AuthenticationPrincipal User authUser,
        @Valid @RequestBody FamilyRequest familyRequest) {

        log.info("api = 가족 참여하기, user = {}", authUser.getUsername());

        FamilyDTO familyDTO = familyService.postNewFamilyUser(authUser, familyRequest.getCode());

        return CommonResponse.onSuccess(familyDTO);
    }

    @ApiOperation(value = "가족 나가기")
    @DeleteMapping(value = "/user", produces = "application/json; charset=utf-8")
    @ResponseBody
    public CommonResponse<FamilyDTO> deleteFamilyUser(@AuthenticationPrincipal User authUser,
        @Valid @RequestBody FamilyRequest familyRequest) {

        log.info("api = 가족 나가기, user = {}", authUser.getUsername());

        if (authUser.getIsKid()) {
            challengeService.challengeCompleteDeleteByKid(authUser, familyRequest);
        } else {
            challengeService.challengeCompleteDeleteByParent(authUser, familyRequest);
        }

        FamilyUser familyUser = familyUserService.findByUserAndCheckCode(authUser,
            familyRequest.getCode());
        Family family = familyUser.getFamily();
        List<FamilyUser> familyUserList = familyUserService.getFamilyUserListExclude(family,
            authUser);

        familyUserService.deleteFamilyUser(familyUser);
        if (familyUserList.size() == 0) {
            familyService.deleteFamily(family);
        }

        return CommonResponse.onSuccess(
            new FamilyDTO(family, familyUserList.stream()
                .map(FamilyUserDTO::new).collect(Collectors.toList())));
    }
}

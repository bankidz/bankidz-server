package com.ceos.bankids.controller;

import com.ceos.bankids.config.CommonResponse;
import com.ceos.bankids.domain.User;
import com.ceos.bankids.dto.FamilyDTO;
import com.ceos.bankids.service.FamilyServiceImpl;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Log
@Controller
@RequestMapping("/family")
@RequiredArgsConstructor
public class FamilyController {

    private final FamilyServiceImpl familyService;

    @ApiOperation(value = "가족 생성하기")
    @PostMapping(value = "", produces = "application/json; charset=utf-8")
    @ResponseBody
    public CommonResponse<FamilyDTO> postFamily(@AuthenticationPrincipal User authUser) {

        FamilyDTO familyDTO = familyService.postNewFamily(authUser);

        return CommonResponse.onSuccess(familyDTO);
    }

    @ApiOperation(value = "가족 정보 조회하기")
    @GetMapping(value = "", produces = "application/json; charset=utf-8")
    @ResponseBody
    public CommonResponse<FamilyDTO> getFamily(@AuthenticationPrincipal User authUser) {

        FamilyDTO familyDTO = familyService.getFamily(authUser);

        return CommonResponse.onSuccess(familyDTO);
    }
}

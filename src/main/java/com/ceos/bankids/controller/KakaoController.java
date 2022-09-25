package com.ceos.bankids.controller;

import com.ceos.bankids.config.CommonResponse;
import com.ceos.bankids.domain.User;
import com.ceos.bankids.dto.LoginDTO;
import com.ceos.bankids.mapper.KakaoMapper;
import com.ceos.bankids.mapper.UserMapper;
import com.ceos.bankids.controller.request.KakaoRequest;
import io.swagger.annotations.ApiOperation;
import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Slf4j
@Controller
@RequestMapping("/kakao")
@RequiredArgsConstructor
public class KakaoController {

    private final KakaoMapper kakaoMapper;
    private final UserMapper userMapper;

    @ApiOperation(value = "카카오 로그인")
    @PostMapping(value = "/login", produces = "application/json; charset=utf-8")
    @ResponseBody
    public CommonResponse<LoginDTO> postKakaoLogin(@Valid @RequestBody KakaoRequest kakaoRequest) {

        log.info("api = 카카오 로그인");

        User user = kakaoMapper.postKakaoLogin(kakaoRequest);

        LoginDTO loginDTO = userMapper.refreshUserToken(user);

        return CommonResponse.onSuccess(loginDTO);
    }
}

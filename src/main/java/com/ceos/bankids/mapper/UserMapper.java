package com.ceos.bankids.mapper;

import com.ceos.bankids.constant.ErrorCode;
import com.ceos.bankids.domain.FamilyUser;
import com.ceos.bankids.domain.User;
import com.ceos.bankids.dto.KidDTO;
import com.ceos.bankids.dto.LoginDTO;
import com.ceos.bankids.dto.MyPageDTO;
import com.ceos.bankids.dto.OptInDTO;
import com.ceos.bankids.dto.ParentDTO;
import com.ceos.bankids.dto.TokenDTO;
import com.ceos.bankids.dto.UserDTO;
import com.ceos.bankids.exception.BadRequestException;
import com.ceos.bankids.mapper.request.ExpoRequest;
import com.ceos.bankids.mapper.request.UserTypeRequest;
import com.ceos.bankids.mapper.request.WithdrawalRequest;
import com.ceos.bankids.service.ChallengeServiceImpl;
import com.ceos.bankids.service.ChallengeUserServiceImpl;
import com.ceos.bankids.service.ExpoNotificationServiceImpl;
import com.ceos.bankids.service.FamilyServiceImpl;
import com.ceos.bankids.service.FamilyUserServiceImpl;
import com.ceos.bankids.service.JwtTokenServiceImpl;
import com.ceos.bankids.service.KidBackupServiceImpl;
import com.ceos.bankids.service.KidServiceImpl;
import com.ceos.bankids.service.ParentBackupServiceImpl;
import com.ceos.bankids.service.ParentServiceImpl;
import com.ceos.bankids.service.SlackServiceImpl;
import com.ceos.bankids.service.UserServiceImpl;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Optional;
import kotlin.jvm.internal.SerializedIr;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@SerializedIr
@RequiredArgsConstructor
public class UserMapper {

    private final UserServiceImpl userService;
    private final FamilyServiceImpl familyService;
    private final FamilyUserServiceImpl familyUserService;
    private final ChallengeServiceImpl challengeService;
    private final KidBackupServiceImpl kidBackupService;
    private final ParentBackupServiceImpl parentBackupService;
    private final KidServiceImpl kidService;
    private final ParentServiceImpl parentService;
    private final SlackServiceImpl slackService;
    private final ExpoNotificationServiceImpl notificationService;
    private final JwtTokenServiceImpl jwtTokenService;
    private final ChallengeUserServiceImpl challengeUserService;

    @Transactional
    public UserDTO updateUserType(User user, UserTypeRequest userTypeRequest) {
        // 유저 타입이 이미 선택되었는지 검사
        if (user.getIsFemale() != null) {
            throw new BadRequestException(ErrorCode.USER_ALREADY_HAS_TYPE.getErrorCode());
        }

        // 날짜 자체가 유효한지 검사
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
        dateFormat.setLenient(false);
        try {
            dateFormat.parse(userTypeRequest.getBirthday());
        } catch (ParseException e) {
            throw new BadRequestException(ErrorCode.INVALID_BIRTHDAY.getErrorCode());
        }

        // 가입이 유효한 범위의 날짜인지 검사
        Calendar cal = Calendar.getInstance();
        Integer currYear = cal.get(Calendar.YEAR);
        Integer birthYear = Integer.parseInt(userTypeRequest.getBirthday()) / 10000;
        if (birthYear >= currYear || birthYear <= currYear - 100) {
            throw new BadRequestException(ErrorCode.INVALID_BIRTHDAY.getErrorCode());
        }

        UserDTO userDTO = userService.updateUserType(user, userTypeRequest);
        if (userDTO.getIsKid() == true) {
            kidService.createNewKid(user);
        } else {
            parentService.createNewParent(user);
        }

        return userDTO;
    }

    @Transactional
    public LoginDTO refreshUserToken(User user) {
        String newRefreshToken = jwtTokenService.encodeJwtRefreshToken(user.getId());
        String newAccessToken = jwtTokenService.encodeJwtToken(new TokenDTO(user));

        User updatedUser = userService.updateRefreshToken(user, newRefreshToken);

        LoginDTO loginDTO;
        if (updatedUser.getIsKid() == null || updatedUser.getIsKid() == false) {
            loginDTO = new LoginDTO(updatedUser.getIsKid(), newAccessToken,
                updatedUser.getProvider());
        } else {
            loginDTO = new LoginDTO(updatedUser.getIsKid(), newAccessToken,
                updatedUser.getKid().getLevel(),
                updatedUser.getProvider());
        }
        return loginDTO;
    }

    @Transactional(readOnly = true)
    public MyPageDTO readUserInformation(User user) {
        MyPageDTO myPageDTO;
        UserDTO userDTO = new UserDTO(user);
        if (user.getIsKid() == null) {
            throw new BadRequestException(ErrorCode.USER_TYPE_NOT_CHOSEN.getErrorCode());
        } else if (user.getIsKid() == true) {
            KidDTO kidDTO = new KidDTO(user.getKid());
            myPageDTO = new MyPageDTO(userDTO, kidDTO);
        } else {
            ParentDTO parentDTO = new ParentDTO(user.getParent());
            myPageDTO = new MyPageDTO(userDTO, parentDTO);
        }
        return myPageDTO;
    }

    @Transactional
    public UserDTO updateUserLogout(User user) {
        userService.updateUserLogout(user);

        return null;
    }

    @Transactional
    public UserDTO deleteUserAccount(User user, WithdrawalRequest withdrawalRequest) {
        Optional<FamilyUser> familyUser = familyUserService.findByUserNullable(user);
//        if (familyUser.isPresent()) {
//            Family family = familyUser.get().getFamily();
//            List<FamilyUser> familyUserList = familyUserService.getFamilyUserListExclude(family,
//                user);
//            FamilyRequest familyRequest = new FamilyRequest(family.getCode());

//            if (user.getIsKid()) {
//                List<Challenge> challengeList = challengeUserService.getAllChallengeUserList(
//                    user);
//                challengeUserService.deleteAllChallengeUser(user);
//                ChallengeCompleteDeleteByKidMapperDTO challengeCompleteDeleteByKidMapperDTO = challengeService.challengeCompleteDeleteByKid(
//                    challengeList);
//                kidService.updateInitKid(user);
//                parentService.updateParentForDeleteFamilyUserByKid(familyUserList,
//                    challengeCompleteDeleteByKidMapperDTO);
//            } else {
//                List<ChallengeUser> challengeUserList = challengeUserService.getChallengeUserListByContractUser(
//                    user);
//                kidService.updateKidForDeleteFamilyUserByParent(challengeUserList);
//                parentService.updateInitParent(user);
//                challengeService.challengeCompleteDeleteByParent(challengeUserList);
//            }
//
//            familyUserService.deleteFamilyUser(familyUser.get());
//            if (familyUserList.size() == 0) {
//                familyService.deleteFamily(family);
//            }
//        }
//
//        if (user.getIsKid()) {
//            KidBackupDTO kidBackupDTO = kidBackupService.backupKidUser(user);
//            slackService.sendWithdrawalMessage("KidBackup ", kidBackupDTO.getId(),
//                withdrawalRequest.getMessage());
//            kidService.deleteKid(user);
//        } else {
//            ParentBackupDTO parentBackupDTO = parentBackupService.backupParentUser(user);
//            slackService.sendWithdrawalMessage("ParentBackup ", parentBackupDTO.getId(),
//                withdrawalRequest.getMessage());
//            parentService.deleteParent(user);
//        }
//        notificationService.deleteAllNotification(user);
//        UserDTO userDTO = userService.deleteUser(user);

//        return userDTO;
        return null;
    }

    @Transactional
    public UserDTO updateUserExpoToken(User user, ExpoRequest expoRequest) {
        userService.updateUserExpoToken(user, expoRequest);

        return null;
    }

    @Transactional
    public OptInDTO updateNoticeOptIn(User user) {
        OptInDTO optInDTO = userService.updateNoticeOptIn(user);

        return optInDTO;
    }

    @Transactional
    public OptInDTO updateServiceOptIn(User user) {
        OptInDTO optInDTO = userService.updateServiceOptIn(user);

        return optInDTO;
    }

    @Transactional(readOnly = true)
    public OptInDTO readOptIn(User user) {
        return new OptInDTO(user);
    }
}

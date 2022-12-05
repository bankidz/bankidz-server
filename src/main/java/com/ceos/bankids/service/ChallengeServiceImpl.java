package com.ceos.bankids.service;

import com.ceos.bankids.constant.ChallengeStatus;
import com.ceos.bankids.constant.ErrorCode;
import com.ceos.bankids.controller.request.KidChallengeRequest;
import com.ceos.bankids.domain.Challenge;
import com.ceos.bankids.domain.ChallengeCategory;
import com.ceos.bankids.domain.ChallengeUser;
import com.ceos.bankids.domain.Comment;
import com.ceos.bankids.domain.Progress;
import com.ceos.bankids.domain.TargetItem;
import com.ceos.bankids.domain.User;
import com.ceos.bankids.dto.AchievedChallengeDTO;
import com.ceos.bankids.dto.AchievedChallengeListDTO;
import com.ceos.bankids.dto.ChallengeCompleteDeleteByKidMapperDTO;
import com.ceos.bankids.dto.ChallengeDTO;
import com.ceos.bankids.dto.ChallengeListMapperDTO;
import com.ceos.bankids.dto.ChallengePostDTO;
import com.ceos.bankids.dto.KidAchievedChallengeListDTO;
import com.ceos.bankids.dto.ProgressDTO;
import com.ceos.bankids.dto.WeekDTO;
import com.ceos.bankids.exception.BadRequestException;
import com.ceos.bankids.exception.ForbiddenException;
import com.ceos.bankids.repository.ChallengeCategoryRepository;
import com.ceos.bankids.repository.ChallengeRepository;
import com.ceos.bankids.repository.CommentRepository;
import com.ceos.bankids.repository.ProgressRepository;
import com.ceos.bankids.repository.TargetItemRepository;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class ChallengeServiceImpl implements ChallengeService {

    // Enum ChallengeStatus
    private static final ChallengeStatus pending = ChallengeStatus.PENDING;
    private static final ChallengeStatus walking = ChallengeStatus.WALKING;
    private static final ChallengeStatus achieved = ChallengeStatus.ACHIEVED;
    private static final ChallengeStatus failed = ChallengeStatus.FAILED;
    private static final ChallengeStatus rejected = ChallengeStatus.REJECTED;

    private final ChallengeRepository challengeRepository;
    private final ChallengeCategoryRepository challengeCategoryRepository;
    private final TargetItemRepository targetItemRepository;
    private final ProgressRepository progressRepository;
    private final CommentRepository commentRepository;

    static int getCurrentWeek(Calendar nowCal, Calendar createdAtCal, int currentWeek) {
        if (nowCal.get(Calendar.YEAR) != createdAtCal.get(Calendar.YEAR)) {
            int diffYears = nowCal.get(Calendar.YEAR) - createdAtCal.get(Calendar.YEAR);
            currentWeek =
                diffYears * createdAtCal.getActualMaximum(Calendar.WEEK_OF_YEAR) + currentWeek;
        }
        return currentWeek;
    }

    // 돈길 생성 API
    @Transactional
    @Override
    public Challenge createChallenge(User user, ChallengePostDTO challengeRequest) {

        String category = challengeRequest.getChallengeCategory();
        String name = challengeRequest.getItemName();
        ChallengeCategory challengeCategory = challengeCategoryRepository.findByCategory(category);
        TargetItem targetItem = targetItemRepository.findByName(name);

        if (targetItem == null) {
            throw new BadRequestException(ErrorCode.NOT_EXIST_CATEGORY.getErrorCode());
        }
        if (challengeCategory == null) {
            throw new BadRequestException(ErrorCode.NOT_EXIST_ITEM.getErrorCode());
        }

        Challenge newChallenge = Challenge.builder().title(challengeRequest.getTitle())
            .contractUser(challengeRequest.getContractUser())
            .totalPrice(challengeRequest.getTotalPrice())
            .weekPrice(challengeRequest.getWeekPrice()).weeks(challengeRequest.getWeeks())
            .challengeStatus(pending)
            .interestRate(challengeRequest.getInterestRate())
            .interestPrice(challengeRequest.getInterestPrice())
            .challengeCategory(challengeCategory).targetItem(targetItem)
            .filename(challengeRequest.getFileName()).build();
        challengeRepository.save(newChallenge);

        return newChallenge;
    }

    // 돈길 삭제 API (2주에 한번)
    @Transactional
    @Override
    public ChallengeDTO deleteWalkingChallenge(User user, ChallengeUser challengeUser) {

        Challenge deleteChallenge = challengeUser.getChallenge();
        List<Progress> deleteChallengeProgressList = deleteChallenge.getProgressList();
        progressRepository.deleteAll(deleteChallengeProgressList);
        challengeRepository.delete(deleteChallenge);

        return new ChallengeDTO(deleteChallenge, null, null);
    }

    @Transactional
    @Override
    public ChallengeDTO deleteRejectedChallenge(User user, ChallengeUser challengeUser) {
        Challenge deleteChallenge = challengeUser.getChallenge();
        Comment comment = deleteChallenge.getComment();
        commentRepository.delete(comment);
        challengeRepository.delete(deleteChallenge);

        return new ChallengeDTO(deleteChallenge, null, null);
    }

    @Transactional
    @Override
    public ChallengeDTO deletePendingChallenge(User user, ChallengeUser challengeUser) {
        Challenge deleteChallenge = challengeUser.getChallenge();
        challengeRepository.delete(deleteChallenge);

        return new ChallengeDTO(deleteChallenge, null, null);
    }

    @Transactional
    @Override
    public ChallengeListMapperDTO readWalkingChallenge(Challenge challenge) {

        List<ProgressDTO> progressDTOList = new ArrayList<>();
        List<Progress> progressList = challenge.getProgressList();
        Long diffWeeks =
            timeLogic(progressList) > challenge.getWeeks() ? challenge
                .getWeeks() + 1 : (long) timeLogic(progressList);
        Long interestRate = challenge.getInterestRate();
        Long risk = 0L;
        Long falseCnt = 0L;
        if (interestRate == 10L) {
            risk = 1000L;
        } else if (interestRate == 20L) {
            risk = 4L;
        } else if (interestRate == 30L) {
            risk = 2L;
        }
        for (Progress progress : progressList) {
            if (progress.getWeeks() <= diffWeeks) {
                if (!progress.getIsAchieved() && progress.getWeeks() < diffWeeks) {
                    falseCnt += 1;
                }
                progressDTOList.add(new ProgressDTO(progress, challenge));
            }
        }
        if (falseCnt >= risk) {
            if (challenge.getChallengeStatus() == failed) {
                return new ChallengeListMapperDTO(challenge, progressDTOList, false);
            }
            challenge.setChallengeStatus(failed);
            challengeRepository.save(challenge);
            return new ChallengeListMapperDTO(challenge, progressDTOList, true);
        } else if (diffWeeks > challenge.getWeeks()) {
            if (challenge.getChallengeStatus() == achieved) {
                return new ChallengeListMapperDTO(challenge, progressDTOList, false);
            }
            challenge.setChallengeStatus(achieved);
            challengeRepository.save(challenge);
            return new ChallengeListMapperDTO(challenge, progressDTOList, true);
        }
        return new ChallengeListMapperDTO(challenge, progressDTOList, false);
    }

    @Transactional
    @Override
    public ChallengeListMapperDTO readPendingChallenge(Challenge challenge) {
        return new ChallengeListMapperDTO(challenge, null, false);
    }

    @Transactional
    @Override
    public ChallengeDTO readChallengeDetail(Long challengeId) {
        Challenge challenge = challengeRepository.findById(challengeId).orElseThrow(
            () -> new BadRequestException(ErrorCode.NOT_EXIST_CHALLENGE.getErrorCode()));
        return new ChallengeDTO(challenge, challenge.getProgressList().stream()
            .map(progress -> new ProgressDTO(progress, challenge)).collect(
                Collectors.toList()), challenge.getComment());
    }

    // 돈길 수락 / 거절 API
    @Transactional
    @Override
    public ChallengeDTO updateChallengeStatusToWalking(Challenge challenge) {

        List<ProgressDTO> progressDTOList = new ArrayList<>();
        if (challenge.getChallengeStatus() != pending) {
            throw new BadRequestException(ErrorCode.ALREADY_APPROVED_CHALLENGE.getErrorCode());
        }
        challenge.setChallengeStatus(walking);
        challengeRepository.save(challenge);
        for (int i = 1; i <= challenge.getWeeks(); i++) {
            Progress newProgress = Progress.builder().weeks((long) i)
                .challenge(challenge)
                .isAchieved(false).build();
            progressDTOList.add(new ProgressDTO(newProgress, challenge));
            progressRepository.save(newProgress);
        }
        return new ChallengeDTO(challenge, progressDTOList, challenge.getComment());
    }

    @Transactional
    @Override
    public ChallengeDTO updateChallengeStatusToRejected(Challenge challenge,
        KidChallengeRequest kidChallengeRequest, User contractUser) {

        Comment newComment = Comment.builder().challenge(challenge).content(
            kidChallengeRequest.getComment()).user(contractUser).build();
        challenge.setChallengeStatus(rejected);
        challenge.setComment(newComment);
        commentRepository.save(newComment);
        challengeRepository.save(challenge);
        return new ChallengeDTO(challenge, null, challenge.getComment());
    }

    // 주차 정보 가져오기 API
    @Transactional
    @Override
    public WeekDTO readWeekInfo(List<Challenge> challengeList) {

        Long[] currentPrice = {0L};
        Long[] totalPrice = {0L};
        challengeList.forEach(challenge -> {
            List<Progress> progressList = challenge.getProgressList();
            int diffWeeks = timeLogic(progressList);
            progressList.forEach(progress -> {
                if (progress.getWeeks() == diffWeeks) {
                    totalPrice[0] += challenge.getWeekPrice();
                    if (progress.getIsAchieved()) {
                        currentPrice[0] += challenge.getWeekPrice();
                    }
                }
            });
        });

        return new WeekDTO(currentPrice[0], totalPrice[0]);
    }

    // 완주한 돈길만 가져오기 API
    @Transactional
    @Override
    public AchievedChallengeListDTO readAchievedChallenge(List<Challenge> achievedChallengeList,
        String interestPayment) {

        List<Challenge> challengeList = achievedChallengeList.stream().filter(challenge -> {
            if (Objects.equals(interestPayment, "paid")) {
                return challenge.getIsInterestPayment();
            } else if (Objects.equals(interestPayment, "unPaid")) {
                return !challenge.getIsInterestPayment();
            } else {
                throw new BadRequestException(ErrorCode.QUERY_PARAM_ERROR.getErrorCode());
            }
        }).collect(Collectors.toList());
        List<AchievedChallengeDTO> achievedChallengeDTOList = challengeList.stream()
            .map(AchievedChallengeDTO::new)
            .collect(Collectors.toList());
        return new AchievedChallengeListDTO(
            achievedChallengeDTOList);
    }

    //자녀의 완주한 돈길 리스트 가져오기 API
    @Transactional
    @Override
    public KidAchievedChallengeListDTO readKidAchievedChallenge(User user,
        List<Challenge> achievedChallengeList,
        String interestPayment, Long kidId) {

        if (!Objects.equals(interestPayment, "paid") && !Objects.equals(interestPayment,
            "unPaid")) {
            throw new BadRequestException(ErrorCode.QUERY_PARAM_ERROR.getErrorCode());
        }

        AchievedChallengeListDTO achievedChallengeListDTO = readAchievedChallenge(
            achievedChallengeList,
            interestPayment);
        List<AchievedChallengeDTO> challengeDTOList = achievedChallengeListDTO.getChallengeDTOList();
        List<AchievedChallengeDTO> contractUserChallengeDTOList = challengeDTOList.stream().filter(
            achievedChallengeDTO -> achievedChallengeDTO.getChallenge().getIsMom()
                == user.getIsFemale()).collect(
            Collectors.toList());
        achievedChallengeListDTO.setChallengeDTOList(contractUserChallengeDTOList);

        return new KidAchievedChallengeListDTO(kidId, achievedChallengeListDTO);
    }

    // 완주한 돈길에 이자 지급 API
    @Transactional
    @Override
    public AchievedChallengeDTO updateChallengeInterestPayment(User user, Long challengeId) {

        Challenge challenge = challengeRepository.findById(challengeId).orElseThrow(
            () -> new BadRequestException(ErrorCode.NOT_EXIST_CHALLENGE.getErrorCode()));
        if (!Objects.equals(challenge.getContractUser().getId(), user.getId())) {
            throw new ForbiddenException(ErrorCode.NOT_MATCH_CONTRACT_USER.getErrorCode());
        }
        if (challenge.getIsInterestPayment()) {
            throw new BadRequestException(ErrorCode.ALREADY_INTEREST_PAYMENT.getErrorCode());
        }
        if (challenge.getChallengeStatus() != achieved) {
            throw new BadRequestException(ErrorCode.NOT_ALREADY_ACHIEVED_CHALLENGE.getErrorCode());
        }
        challenge.setIsInterestPayment(true);
        challengeRepository.save(challenge);

        return new AchievedChallengeDTO(challenge);
    }

    @Transactional
    @Override
    public ProgressDTO updateProgress(Challenge challenge) {
        Long diffWeeks = (long) timeLogic(challenge.getProgressList());
        Progress progress = progressRepository.findByChallengeIdAndWeeks(challenge.getId(),
                diffWeeks)
            .orElseThrow(
                () -> new BadRequestException(ErrorCode.NOT_EXIST_PROGRESS.getErrorCode()));
        if (progress.getIsAchieved()) {
            throw new BadRequestException(ErrorCode.ALREADY_WALK_PROGRESS.getErrorCode());
        }
        if (diffWeeks.equals(challenge.getWeeks())) {
            challenge.setChallengeStatus(achieved);
        }
        progress.setIsAchieved(true);
        challenge.setSuccessWeeks(challenge.getSuccessWeeks() + 1);
        challengeRepository.save(challenge);
        progressRepository.save(progress);

        return new ProgressDTO(progress, challenge);
    }

    @Transactional(readOnly = true)
    @Override
    public Challenge readChallenge(Long challengeId) {
        return challengeRepository.findById(challengeId).orElseThrow(
            () -> new BadRequestException(ErrorCode.NOT_EXIST_CHALLENGE.getErrorCode()));
    }

    private int timeLogic(List<Progress> progressList) {
        LocalDateTime now = LocalDateTime.now();
        Timestamp nowTimestamp = Timestamp.valueOf(now);
        Calendar nowCal = Calendar.getInstance();
        nowCal.setTime(nowTimestamp);
        int dayOfWeek = nowCal.get(Calendar.DAY_OF_WEEK);
        Progress progress1 = progressList.stream().findFirst()
            .orElseThrow(() -> new ForbiddenException(ErrorCode.TIMELOGIC_ERROR.getErrorCode()));
        Timestamp createdAt1 = progress1.getCreatedAt();
        Calendar createdAtCal = Calendar.getInstance();
        createdAtCal.setTime(createdAt1);
        int createdWeek = createdAtCal.get(Calendar.WEEK_OF_YEAR);
        int currentWeek = nowCal.get(Calendar.WEEK_OF_YEAR);
        currentWeek = getCurrentWeek(nowCal, createdAtCal, currentWeek);
        return dayOfWeek == 1 ? currentWeek - createdWeek
            : currentWeek - createdWeek + 1;
    }

    @Transactional
    public ChallengeCompleteDeleteByKidMapperDTO challengeCompleteDeleteByKid(
        List<Challenge> challengeList) {

        long[] momRequest = new long[]{0L, 0L};
        long[] dadRequest = new long[]{0L, 0L};

        //challenge / progress / comment 한번에 삭제
        challengeList.forEach(challenge -> {
            boolean isMom = challenge.getContractUser().getIsFemale();
            if (isMom) {
                momRequest[0] = momRequest[0] + 1;
            } else {
                dadRequest[0] = dadRequest[0] + 1;
            }
            if (challenge.getChallengeStatus() == rejected) {
                commentRepository.delete(challenge.getComment());
            } else if (challenge.getChallengeStatus() == achieved
                || challenge.getChallengeStatus() == walking
                || challenge.getChallengeStatus() == failed) {
                if (isMom) {
                    momRequest[1] = momRequest[1] + 1;
                } else {
                    dadRequest[1] = dadRequest[1] + 1;
                }
                progressRepository.deleteAll(challenge.getProgressList());
            }
            challengeRepository.delete(challenge);
        });
        return new ChallengeCompleteDeleteByKidMapperDTO(momRequest[0], momRequest[1],
            dadRequest[0], dadRequest[1]);
    }

    @Transactional(readOnly = true)
    @Override
    public List<ChallengeDTO> readChallengeHistory(String status) {

        List<Challenge> challengeHistoryList =
            status == null ? challengeRepository.findAllByDeletedAtIsNotNullOrderByIdDesc()
                : challengeRepository.findByChallengeStatusAndDeletedAtIsNotNullOrderByIdDesc(
                    status);
        return challengeHistoryList.stream().map(challenge -> {
            if (challenge.getChallengeStatus() == ChallengeStatus.ACHIEVED
                || challenge.getChallengeStatus() == ChallengeStatus.FAILED) {
                List<ProgressDTO> progressDTOList = challenge.getProgressList().stream()
                    .map(progress -> new ProgressDTO(progress, challenge)).collect(
                        Collectors.toList());
                return new ChallengeDTO(challenge, progressDTOList, null);
            } else {
                return new ChallengeDTO(challenge, null, challenge.getComment());
            }
        }).collect(Collectors.toList());
    }

    @Transactional
    public void challengeCompleteDeleteByParent(List<ChallengeUser> challengeUserList) {

        challengeUserList.forEach(challengeUser -> {
            Challenge challenge = challengeUser.getChallenge();
            if (challenge.getChallengeStatus() != pending
                && challenge.getChallengeStatus() != rejected) {
                progressRepository.deleteAll(challenge.getProgressList());
            }

            if (challenge.getChallengeStatus() == rejected) {
                commentRepository.delete(challenge.getComment());
            }
            challengeRepository.delete(challenge);
        });
    }
}


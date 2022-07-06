package com.ceos.bankids.service;

import com.ceos.bankids.controller.request.ChallengeRequest;
import com.ceos.bankids.domain.*;
import com.ceos.bankids.dto.ChallengeDTO;
import com.ceos.bankids.exception.BadRequestException;
import com.ceos.bankids.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = false)
public class ChallengeServiceImpl implements ChallengeService {

    private final ChallengeRepository challengeRepository;
    private final ChallengeCategoryRepository challengeCategoryRepository;
    private final TargetItemRepository targetItemRepository;
    private final ChallengeUserRepository challengeUserRepository;

    @Transactional
    @Override
    public ChallengeDTO createChallenge(User user, ChallengeRequest challengeRequest) {
        String category = challengeRequest.getCategory();
        String itemName = challengeRequest.getItemName();
        ChallengeCategory challengeCategory = challengeCategoryRepository.findByCategory(category);
        TargetItem targetItem = targetItemRepository.findByItemName(itemName);
        System.out.println("targetItem = " + targetItem + "challengeCategory = " + challengeCategory);
        if (targetItem == null) {
            throw new BadRequestException("목표 아이템 입력이 잘 못 되었습니다.");
        }
        if (challengeCategory == null) {
            throw new BadRequestException("카테고리 입력이 잘 못 되었습니다.");
        }
        if (challengeRequest.getWeeks() != challengeRequest.getTotalPrice() / challengeRequest.getWeekPrice()) {
            throw new BadRequestException("주차수가 목표 금액 / 주당 금액의 값과 다릅니다.");
        }
        Challenge newChallenge = Challenge.builder()
                .title(challengeRequest.getTitle())
                .isAchieved(false)
                .totalPrice(challengeRequest.getTotalPrice())
                .weekPrice(challengeRequest.getWeekPrice())
                .weeks(challengeRequest.getWeeks())
                .status(1L)
                .interestRate(challengeRequest.getInterestRate())
                .challengeCategory(challengeCategory)
                .targetItem(targetItem)
                .build();
        challengeRepository.save(newChallenge);
        //ChallengeUser에 등록
        //ToDo: 자식-부모 매핑되면 부모도 같이 등록시키기
        ChallengeUser newChallengeUser = ChallengeUser.builder()
                .challenge(newChallenge)
                .member("parent")
                .user(user)
                .build();
        challengeUserRepository.save(newChallengeUser);

        return new ChallengeDTO(newChallenge);
    }

    @Transactional
    @Override
    public ChallengeDTO detailChallenge(Long challengeId) {
        ChallengeUser challengeUserRow = challengeUserRepository.findByChallengeId(challengeId);
        Challenge findChallenge = challengeUserRow.getChallenge();

        return new ChallengeDTO(findChallenge);
    }
}


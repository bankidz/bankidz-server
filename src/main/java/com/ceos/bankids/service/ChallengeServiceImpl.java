package com.ceos.bankids.service;

import com.ceos.bankids.controller.request.ChallengeRequest;
import com.ceos.bankids.controller.request.KidChallengeRequest;
import com.ceos.bankids.domain.Challenge;
import com.ceos.bankids.domain.ChallengeCategory;
import com.ceos.bankids.domain.ChallengeUser;
import com.ceos.bankids.domain.Comment;
import com.ceos.bankids.domain.FamilyUser;
import com.ceos.bankids.domain.Progress;
import com.ceos.bankids.domain.TargetItem;
import com.ceos.bankids.domain.User;
import com.ceos.bankids.dto.ChallengeDTO;
import com.ceos.bankids.dto.KidChallengeListDTO;
import com.ceos.bankids.exception.BadRequestException;
import com.ceos.bankids.exception.ForbiddenException;
import com.ceos.bankids.exception.NotFoundException;
import com.ceos.bankids.repository.ChallengeCategoryRepository;
import com.ceos.bankids.repository.ChallengeRepository;
import com.ceos.bankids.repository.ChallengeUserRepository;
import com.ceos.bankids.repository.CommentRepository;
import com.ceos.bankids.repository.FamilyUserRepository;
import com.ceos.bankids.repository.ProgressRepository;
import com.ceos.bankids.repository.TargetItemRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = false)
public class ChallengeServiceImpl implements ChallengeService {

    private final ChallengeRepository challengeRepository;
    private final ChallengeCategoryRepository challengeCategoryRepository;
    private final TargetItemRepository targetItemRepository;
    private final ChallengeUserRepository challengeUserRepository;
    private final ProgressRepository progressRepository;
    private final FamilyUserRepository familyUserRepository;
    private final CommentRepository commentRepository;

    @Transactional
    @Override
    public ChallengeDTO createChallenge(User user, ChallengeRequest challengeRequest) {

        Boolean isMom = challengeRequest.getIsMom();
        FamilyUser familyUser = familyUserRepository.findByUserId(user.getId())
            .orElseThrow(() -> new ForbiddenException("가족이 없는 유저는 돈길을 생성 할 수 없습니다."));
        User contractUser = familyUserRepository.findByFamily(familyUser.getFamily())
            .stream()
            .filter(f -> !f.getUser().getIsKid() && f.getUser().getIsFemale() == isMom).findFirst()
            .orElseThrow(() -> new BadRequestException("해당 부모가 없습니다.")).getUser();

        String category = challengeRequest.getCategory();
        String name = challengeRequest.getItemName();
        ChallengeCategory challengeCategory = challengeCategoryRepository.findByCategory(category);
        TargetItem targetItem = targetItemRepository.findByName(name);

        if (targetItem == null) {
            throw new BadRequestException("목표 아이템 입력이 잘 못 되었습니다.");
        }
        if (challengeCategory == null) {
            throw new BadRequestException("카테고리 입력이 잘 못 되었습니다.");
        }
        if (challengeRequest.getWeeks()
            != challengeRequest.getTotalPrice() / challengeRequest.getWeekPrice()) {
            throw new BadRequestException("주차수가 목표 금액 / 주당 금액의 값과 다릅니다.");
        }
        Challenge newChallenge = Challenge.builder().title(challengeRequest.getTitle())
            .contractUser(contractUser)
            .isAchieved(false).totalPrice(challengeRequest.getTotalPrice())
            .weekPrice(challengeRequest.getWeekPrice()).weeks(challengeRequest.getWeeks())
            .status(1L).interestRate(challengeRequest.getInterestRate())
            .challengeCategory(challengeCategory).targetItem(targetItem).build();
        challengeRepository.save(newChallenge);

        ChallengeUser newChallengeUser = ChallengeUser.builder().challenge(newChallenge)
            .member("parent").user(user).build();
        challengeUserRepository.save(newChallengeUser);

        return new ChallengeDTO(newChallenge);
    }

    @Transactional
    @Override
    public ChallengeDTO detailChallenge(User user, Long challengeId) {
        Optional<ChallengeUser> challengeUserRow = challengeUserRepository.findByChallengeId(
            challengeId);
        System.out.println(user.getId());
        if (challengeUserRow.isPresent()) {
            ChallengeUser challengeUser = challengeUserRow.get();
            if (!Objects.equals(challengeUser.getUser().getId(), user.getId())) {
                throw new ForbiddenException("권한이 없습니다.");
            }
            Challenge findChallenge = challengeUser.getChallenge();
            return new ChallengeDTO((findChallenge));
        } else {
            throw new NotFoundException("챌린지가 없습니다.");
        }
    }

    @Transactional
    @Override
    public ChallengeDTO deleteChallenge(User user, Long challengeId) {
        Optional<ChallengeUser> deleteChallengeUserRow = challengeUserRepository.findByChallengeId(
            challengeId);
        if (deleteChallengeUserRow.isPresent()) {
            ChallengeUser deleteChallengeUser = deleteChallengeUserRow.get();
            Challenge deleteChallenge = deleteChallengeUser.getChallenge();
            Long deleteChallengeId = deleteChallenge.getId();
            if (!Objects.equals(deleteChallengeUser.getUser().getId(), user.getId())) {
                throw new ForbiddenException("권한이 없습니다.");
            }
            List<Progress> progressList = deleteChallenge.getProgressList();
            if ((long) progressList.size() == 1 || progressList.isEmpty()) {
                progressRepository.deleteAll(progressList);
                challengeUserRepository.delete(deleteChallengeUser);
                challengeRepository.delete(deleteChallenge);
            } else {
                throw new BadRequestException("생성한지 일주일이 지난 돈길은 포기가 불가능합니다.");
            }

            return null;
        } else {
            throw new NotFoundException("챌린지가 없습니다.");
        }
    }

    @Transactional
    @Override
    public List<ChallengeDTO> readChallenge(User user, String status) {

        List<ChallengeUser> challengeUserRow = challengeUserRepository.findByUserId(
            user.getId());
        List<ChallengeDTO> challengeDTOList = new ArrayList<>();
        for (ChallengeUser r : challengeUserRow) {
            if (status.equals("accept") && r.getChallenge().getStatus() == 2L) {
                challengeDTOList.add(new ChallengeDTO(r.getChallenge()));
            } else if ((status.equals("pending") || status.equals("reject"))
                && r.getChallenge().getStatus() != 2L) {
                challengeDTOList.add(new ChallengeDTO(r.getChallenge()));
            }

        }
        return challengeDTOList;
    }

    @Transactional
    @Override
    public List<KidChallengeListDTO> readKidChallenge(User user) {

        Optional<FamilyUser> familyUser = familyUserRepository.findByUserId(user.getId());
        List<KidChallengeListDTO> kidChallengeListDTOList = new ArrayList<>();
        familyUser.ifPresent(c -> {
            List<FamilyUser> familyUserList = familyUserRepository.findByFamily(c.getFamily());
            familyUserList.forEach(familyUser1 -> {
                List<ChallengeDTO> challengeList = new ArrayList<>();
                if (familyUser1.getUser().getIsKid()) {
                    List<ChallengeUser> challengeUserList = challengeUserRepository.findByUserId(
                        familyUser1.getUser().getId());
                    challengeUserList.forEach(challengeUser -> {
                        challengeList.add(new ChallengeDTO(challengeUser.getChallenge()));
                    });
                    kidChallengeListDTOList.add(
                        new KidChallengeListDTO(familyUser1.getUser(), challengeList));
                }
            });
        });

        return kidChallengeListDTOList;
    }

    @Transactional
    @Override
    public ChallengeDTO updateChallengeStatus(User user, Long challengeId,
        KidChallengeRequest kidChallengeRequest) {

        ChallengeUser findChallengeUser = challengeUserRepository.findByChallengeId(challengeId)
            .orElseThrow(() -> new BadRequestException("존재하지 않는 돈길입니다."));
        User cUser = findChallengeUser.getUser();
        Optional<FamilyUser> familyUser = familyUserRepository.findByUserId(cUser.getId());
        Optional<FamilyUser> familyUser1 = familyUserRepository.findByUserId(user.getId());
        familyUser.ifPresent(f -> {
            familyUser1.ifPresent(f1 -> {
                if (f.getFamily() != f1.getFamily() || user.getIsKid()) {
                    throw new ForbiddenException("권한이 없습니다.");
                }
            });
        });
        Challenge challenge = findChallengeUser.getChallenge();
        if (challenge.getStatus() != 1L) {
            throw new BadRequestException("이미 승인 혹은 거절된 돈길입니다.");
        }
        if (kidChallengeRequest.getAccept()) {
            challenge.setStatus(2L);
            challengeRepository.save(challenge);
            for (int i = 1; i <= challenge.getWeeks(); i++) {
                Progress newProgress = Progress.builder().weeks(Long.valueOf(i))
                    .challenge(challenge)
                    .isAchieved(false).build();
                progressRepository.save(newProgress);
            }
        } else {
            Comment newComment = Comment.builder().challenge(challenge).content(
                kidChallengeRequest.getComment()).user(user).build();
            challenge.setStatus(0L);
            commentRepository.save(newComment);
            challenge.setComment(newComment);
            challengeRepository.save(challenge);
        }

        return new ChallengeDTO(challengeRepository.findById(challengeId).get());
    }

}


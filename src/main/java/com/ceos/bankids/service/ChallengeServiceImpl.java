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
import com.ceos.bankids.dto.ProgressDTO;
import com.ceos.bankids.dto.WeekDTO;
import com.ceos.bankids.exception.BadRequestException;
import com.ceos.bankids.exception.ForbiddenException;
import com.ceos.bankids.exception.InternalServerException;
import com.ceos.bankids.exception.NotFoundException;
import com.ceos.bankids.repository.ChallengeCategoryRepository;
import com.ceos.bankids.repository.ChallengeRepository;
import com.ceos.bankids.repository.ChallengeUserRepository;
import com.ceos.bankids.repository.CommentRepository;
import com.ceos.bankids.repository.FamilyUserRepository;
import com.ceos.bankids.repository.ProgressRepository;
import com.ceos.bankids.repository.TargetItemRepository;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
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

        long count = challengeUserRepository.findByUserId(user.getId()).stream()
            .filter(challengeUser -> challengeUser.getChallenge().getStatus() == 2).count();
        if (count >= 5) {
            throw new ForbiddenException("돈길 생성 개수 제한에 도달했습니다.");
        }
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

        return new ChallengeDTO(newChallenge, null, null);
    }

    @Transactional
    @Override
    public ChallengeDTO detailChallenge(User user, Long challengeId) {
        Optional<ChallengeUser> challengeUserRow = challengeUserRepository.findByChallengeId(
            challengeId);
        if (challengeUserRow.isPresent()) {
            ChallengeUser challengeUser = challengeUserRow.get();
            if (!Objects.equals(challengeUser.getUser().getId(), user.getId())) {
                throw new ForbiddenException("권한이 없습니다.");
            }
            Challenge findChallenge = challengeUser.getChallenge();
            List<ProgressDTO> progressDTOList = new ArrayList<>();
            if (findChallenge.getStatus() == 2L) {
                List<Progress> progressList = findChallenge.getProgressList();
                progressList.forEach(
                    progress -> {
                        progressDTOList.add(new ProgressDTO(progress));
                    });
                return new ChallengeDTO(findChallenge, progressDTOList, null);
            } else if (findChallenge.getStatus() == 0L) {
                return new ChallengeDTO(findChallenge, null, findChallenge.getComment());
            } else {
                return new ChallengeDTO(findChallenge, null, null);
            }
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

        try {
            List<ChallengeUser> challengeUserRow = challengeUserRepository.findByUserId(
                user.getId());
            List<ChallengeDTO> challengeDTOList = new ArrayList<>();
            String nowDate = LocalDate.now().toString();
            SimpleDateFormat format = new SimpleDateFormat("yy-MM-dd");
            Date date = format.parse(nowDate);
            for (ChallengeUser r : challengeUserRow) {
                if (status.equals("accept") && r.getChallenge().getStatus() == 2L) {
                    List<ProgressDTO> progressDTOList = new ArrayList<>();
                    List<Progress> progressList = r.getChallenge().getProgressList();
                    Progress progress1 = progressList.stream().findFirst()
                        .orElseThrow(BadRequestException::new);
                    Date createdAt = format.parse(progress1.getCreatedAt().toString());
                    long diff = date.getTime() - createdAt.getTime();
                    long diffWeeks = (diff / (1000 * 60 * 60 * 24 * 7)) + 1;
                    progressList
                        .forEach(progress -> {
                            if (progress.getWeeks() <= diffWeeks) {
                                progressDTOList.add(new ProgressDTO(progress));
                            }
                        });
                    challengeDTOList.add(new ChallengeDTO(r.getChallenge(), progressDTOList,
                        r.getChallenge().getComment()));
                } else if ((status.equals("pending"))
                    && r.getChallenge().getStatus() != 2L) {
                    challengeDTOList.add(new ChallengeDTO(r.getChallenge(), null,
                        r.getChallenge().getComment()));
                }
            }
            return challengeDTOList;
        } catch (ParseException e) {
            throw new InternalServerException("Datetime parse 오류");
        }

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
                        List<ProgressDTO> progressDTOList = new ArrayList<>();
                        Long status = challengeUser.getChallenge().getStatus();
                        if (status == 2L) {
                            challengeUser.getChallenge().getProgressList()
                                .forEach(
                                    progress -> progressDTOList.add(new ProgressDTO(progress)));
                            challengeList.add(
                                new ChallengeDTO(challengeUser.getChallenge(), progressDTOList,
                                    challengeUser.getChallenge().getComment()));
                        } else {
                            challengeList.add(new ChallengeDTO(challengeUser.getChallenge(), null,
                                challengeUser.getChallenge()
                                    .getComment()));
                        }
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
        List<ProgressDTO> progressDTOList = new ArrayList<>();
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
                progressDTOList.add(new ProgressDTO(newProgress));
                progressRepository.save(newProgress);
            }
        } else {
            Comment newComment = Comment.builder().challenge(challenge).content(
                kidChallengeRequest.getComment()).user(user).build();
            challenge.setStatus(0L);
            commentRepository.save(newComment);
            challenge.setComment(newComment);
            challengeRepository.save(challenge);
            progressDTOList = null;
        }
        return new ChallengeDTO(challenge, progressDTOList, challenge.getComment());
    }

    @Transactional
    @Override
    public WeekDTO readWeekInfo(User user) {

        String now = LocalDate.now().toString();
        SimpleDateFormat format = new SimpleDateFormat("yy-MM-dd");

        Long[] currentPrice = {0L};
        Long[] totalPrice = {0L};
        try {
            Date nowDate = format.parse(now);
            List<ChallengeUser> challengeUserList = challengeUserRepository.findByUserId(
                user.getId());
            challengeUserList.forEach(challengeUser -> {
                Challenge challenge = challengeUser.getChallenge();
                if (challenge.getStatus() == 2 && !challenge.getIsAchieved()) {
                    List<Progress> progressList = challenge.getProgressList();
                    Progress progress1 = progressList.stream().findFirst()
                        .orElseThrow(BadRequestException::new);
                    String progressCreatedAtString = progress1.getCreatedAt().toString();
                    try {
                        Date progressCreatedAt = format.parse(progressCreatedAtString);
                        long diff = nowDate.getTime() - progressCreatedAt.getTime();
                        long diffWeeks = (diff / (1000 * 60 * 60 * 24 * 7)) + 1;
                        progressList.forEach(progress -> {
                            if (progress.getWeeks() == diffWeeks) {
                                totalPrice[0] += challenge.getWeekPrice();
                                if (progress.getIsAchieved()) {
                                    currentPrice[0] += challenge.getWeekPrice();
                                }
                            }
                        });
                    } catch (ParseException ex) {
                        throw new InternalServerException("Datetime Parse 오류");
                    }
                }
            });
        } catch (ParseException e) {
            throw new InternalServerException("Datetime parse 오류");
        }
        return new WeekDTO(currentPrice[0], totalPrice[0]);
    }
}


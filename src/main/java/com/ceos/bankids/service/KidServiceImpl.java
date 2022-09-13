package com.ceos.bankids.service;

import com.ceos.bankids.constant.ErrorCode;
import com.ceos.bankids.domain.Kid;
import com.ceos.bankids.domain.User;
import com.ceos.bankids.exception.BadRequestException;
import com.ceos.bankids.exception.ForbiddenException;
import com.ceos.bankids.repository.KidRepository;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Calendar;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class KidServiceImpl implements KidService {

    private final KidRepository kidRepository;

    @Override
    @Transactional
    public void createNewKid(User user) {
        Kid newKid = Kid.builder()
            .savings(0L)
            .achievedChallenge(0L)
            .totalChallenge(0L)
            .level(1L)
            .user(user)
            .build();
        kidRepository.save(newKid);
    }

    @Override
    @Transactional
    public void deleteKid(User user) {
        kidRepository.delete(user.getKid());
    }

    @Transactional(readOnly = true)
    @Override
    public void checkKidDeleteChallenge(User user) {

        if (user.getKid() == null) {
            throw new BadRequestException(ErrorCode.NOT_EXIST_KID.getErrorCode());
        }

        // 현재 시간 가져오기
        LocalDateTime now = LocalDateTime.now();
        Timestamp nowTimestamp = Timestamp.valueOf(now);
        Calendar nowCal = Calendar.getInstance();
        nowCal.setTime(nowTimestamp);

        Kid kid = user.getKid();
        if (kid.getDeleteChallenge() != null) {

            // 유저의 마지막 돈길 삭제시간 가져오기
            Timestamp deleteChallenge = kid.getDeleteChallenge();
            Calendar deleteCal = Calendar.getInstance();
            deleteCal.setTime(deleteChallenge);

            int lastDeleteWeek = deleteCal.get(Calendar.WEEK_OF_YEAR);
            int currentWeek = nowCal.get(Calendar.WEEK_OF_YEAR);
            int diffYears = nowCal.get(Calendar.YEAR) - deleteCal.get(Calendar.YEAR);
            int l = deleteCal.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY ? lastDeleteWeek - 1
                : lastDeleteWeek;
            int c = nowCal.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY ? currentWeek - 1
                : currentWeek;
            if (diffYears == 0 && l + 2 > c) {
                throw new ForbiddenException(ErrorCode.NOT_TWO_WEEKS_YET.getErrorCode());
            } else if (diffYears > 0) {
                int newC = diffYears * deleteCal.getActualMaximum(Calendar.WEEK_OF_YEAR) + c;
                if (l + 2 > newC) {
                    throw new ForbiddenException(ErrorCode.NOT_TWO_WEEKS_YET.getErrorCode());
                }
            }
        }
        long datetime = System.currentTimeMillis();
        Timestamp timestamp = new Timestamp(datetime);
        kid.setDeleteChallenge(timestamp);
        kidRepository.save(kid);
    }

}

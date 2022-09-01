package com.ceos.bankids.service;

import com.ceos.bankids.domain.KidBackup;
import com.ceos.bankids.domain.User;
import com.ceos.bankids.dto.KidBackupDTO;
import com.ceos.bankids.repository.KidBackupRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class KidBackupServiceImpl implements KidBackupService {

    private final KidBackupRepository kidBackupRepository;

    @Override
    @Transactional
    public KidBackupDTO backupKidUser(User user) {
        KidBackup kidBackup = KidBackup.builder()
            .birthYear(user.getBirthday().substring(0, 4))
            .isKid(user.getIsKid())
            .savings(user.getKid().getSavings())
            .achievedChallenge(user.getKid().getAchievedChallenge())
            .totalChallenge(user.getKid().getTotalChallenge())
            .level(user.getKid().getLevel())
            .build();
        kidBackupRepository.save(kidBackup);

        return new KidBackupDTO(kidBackup);
    }

}

package com.ceos.bankids.repository;

import com.ceos.bankids.domain.ChallengeNotification;
import com.ceos.bankids.domain.ChallengeUser;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChallengeNotificationRepository extends
    JpaRepository<ChallengeNotification, Long> {

    public List<ChallengeNotification> findByChallengeUser(ChallengeUser challengeUser);

}

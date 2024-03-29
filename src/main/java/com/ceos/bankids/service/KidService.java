package com.ceos.bankids.service;

import com.ceos.bankids.domain.Challenge;
import com.ceos.bankids.domain.Kid;
import com.ceos.bankids.domain.User;
import org.springframework.stereotype.Service;

@Service
public interface KidService {

    public void createKid(User user);

    public void deleteKid(User user);

    public void checkKidDeleteChallenge(User user, Challenge challenge);

    public void userLevelUp(User contractUser, User user);

    public Kid getKid(Long kidId);

    public void updateKidTotalChallenge(User user);

    public void updateInitKid(User user);

    public void updateKidByPatchProgress(User user, Challenge challenge);

    public void updateKidDecreaseTotalChallenge(User user);
}

package com.ceos.bankids.service;

import com.ceos.bankids.domain.FamilyUser;
import com.ceos.bankids.domain.User;
import com.ceos.bankids.dto.ChallengeCompleteDeleteByKidMapperDTO;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public interface ParentService {

    public void createParent(User user);

    public void deleteParent(User user);

    public void updateParentForCreateChallenge(User contractUser);

    public void updateParentAcceptedChallenge(User contractUser);

    public void updateParentForDeleteFamilyUserByKid(List<FamilyUser> familyUserList,
        ChallengeCompleteDeleteByKidMapperDTO challengeCompleteDeleteByKidMapperDTO);

    public void updateInitParent(User user);
}

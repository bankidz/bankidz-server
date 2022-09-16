package com.ceos.bankids.service;

import com.ceos.bankids.domain.Parent;
import com.ceos.bankids.domain.User;
import com.ceos.bankids.repository.ParentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ParentServiceImpl implements ParentService {

    private final ParentRepository parentRepository;

    @Override
    @Transactional
    public void createNewParent(User user) {
        Parent newParent = Parent.builder()
            .acceptedRequest(0L)
            .totalRequest(0L)
            .user(user)
            .build();
        parentRepository.save(newParent);
    }

    @Override
    @Transactional
    public void deleteParent(User user) {
        parentRepository.delete(user.getParent());
    }

    @Transactional
    @Override
    public void updateParentForCreateChallenge(User contractUser) {
        // 자녀가 제안한 총 돈길
        Parent parent = contractUser.getParent();
        parent.setTotalRequest(contractUser.getParent().getTotalRequest() + 1);
        parentRepository.save(parent);
    }

    @Transactional
    @Override
    public void updateParentAcceptedChallenge(User contractUser) {

        Parent parent = contractUser.getParent();
        Long acceptedRequest = parent.getAcceptedRequest();
        parent.setAcceptedRequest(acceptedRequest + 1L);
        parentRepository.save(parent);
    }

}

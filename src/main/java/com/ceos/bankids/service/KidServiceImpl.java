package com.ceos.bankids.service;

import com.ceos.bankids.domain.User;
import com.ceos.bankids.repository.KidRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class KidServiceImpl implements KidService {

    private final KidRepository kidRepository;

    @Override
    @Transactional
    public void deleteKid(User user) {
        kidRepository.delete(user.getKid());
    }

}

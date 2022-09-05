package com.ceos.bankids.service;

import com.ceos.bankids.domain.User;
import org.springframework.stereotype.Service;

@Service
public interface ParentService {

    public void deleteParent(User user);

}

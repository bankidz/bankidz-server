package com.ceos.bankids.service;

import com.ceos.bankids.domain.User;
import com.ceos.bankids.dto.PreSignedDTO;
import org.springframework.stereotype.Service;

@Service
public interface S3Service {

    PreSignedDTO readPreSignedUrl(User user);
}

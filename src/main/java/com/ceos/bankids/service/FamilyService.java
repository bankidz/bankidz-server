package com.ceos.bankids.service;

import com.ceos.bankids.domain.Family;
import com.ceos.bankids.domain.User;
import com.ceos.bankids.dto.FamilyDTO;
import com.ceos.bankids.dto.FamilyUserDTO;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public interface FamilyService {

    public FamilyDTO postNewFamily(User user);

    public List<FamilyUserDTO> getFamilyUserList(Family family);

}

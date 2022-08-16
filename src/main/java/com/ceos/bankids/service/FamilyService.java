package com.ceos.bankids.service;

import com.ceos.bankids.domain.Family;
import com.ceos.bankids.domain.User;
import com.ceos.bankids.dto.FamilyDTO;
import com.ceos.bankids.dto.FamilyUserDTO;
import com.ceos.bankids.dto.KidListDTO;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public interface FamilyService {

    public FamilyDTO postNewFamily(User user);

    public List<FamilyUserDTO> getFamilyUserList(Family family, User user);

    public FamilyDTO getFamily(User user);

    public List<KidListDTO> getKidListFromFamily(User user);

    public FamilyDTO postNewFamilyUser(User user, String code);
}

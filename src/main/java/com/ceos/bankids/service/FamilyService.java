package com.ceos.bankids.service;

import com.ceos.bankids.domain.Family;
import com.ceos.bankids.domain.User;
import org.springframework.stereotype.Service;

@Service
public interface FamilyService {

    public Family postNewFamily(User user);
    
    public Family getFamilyByCode(String code);

    public FamilyDTO getFamily(User user);

    public List<KidListDTO> getKidListFromFamily(User user);

    public FamilyDTO postNewFamilyUser(User user, String code);

    public FamilyDTO deleteFamilyUser(User user, String code);

    public User getContractUser(User user, Boolean isMom);

    public void checkSameFamily(User firstUser, User secondUser);

    public void deleteFamily(Family family);
}

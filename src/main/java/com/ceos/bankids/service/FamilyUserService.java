package com.ceos.bankids.service;

import com.ceos.bankids.domain.Family;
import com.ceos.bankids.domain.FamilyUser;
import com.ceos.bankids.domain.User;
import com.ceos.bankids.dto.KidListDTO;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public interface FamilyUserService {

    public void checkIfFamilyExists(User user);

    public void postNewFamilyUser(Family family, User user);

    public void leavePreviousFamily(User user);

    public FamilyUser findByUser(User user);

    public FamilyUser findByUserAndCheckCode(User user, String code);

    public List<FamilyUser> checkFamilyUserList(Family family, User user);


    public void deleteFamilyUser(FamilyUser familyUser);

    public List<FamilyUser> getFamilyUserListExclude(Family family, User user);

    public List<KidListDTO> getKidListFromFamily(FamilyUser familyUser);

}

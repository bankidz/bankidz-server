package com.ceos.bankids.service;

import com.ceos.bankids.domain.Family;
import com.ceos.bankids.domain.User;
import com.ceos.bankids.dto.FamilyDTO;
import com.ceos.bankids.dto.KidListDTO;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public interface FamilyService {

    public Family postNewFamily(User user);

    public FamilyDTO getFamily(User user);

    public List<KidListDTO> getKidListFromFamily(User user);

    public Family getFamilyByCode(String code);

    public void deleteFamily(Family family);
}

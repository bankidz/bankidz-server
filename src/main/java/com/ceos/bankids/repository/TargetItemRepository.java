package com.ceos.bankids.repository;

import com.ceos.bankids.domain.TargetItem;
import org.springframework.data.jpa.repository.JpaRepository;


public interface TargetItemRepository extends JpaRepository<TargetItem, Long> {

    public TargetItem findByName(String itemName);
}

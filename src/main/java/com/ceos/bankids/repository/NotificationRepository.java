package com.ceos.bankids.repository;

import com.ceos.bankids.domain.Notification;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationRepository extends
    JpaRepository<Notification, Long> {

    public List<Notification> findAllByUserId(Long userId);

    //    @Query("select n from Notification n where n.id < ")
    public Page<Notification> findByIdLessThanEqualAndUserIdOrderByIdDesc(Long id, Long userId,
        Pageable pageRequest);

    public Page<Notification> findByUserIdOrderByIdDesc(Long userId, Pageable pageRequest);

    public void deleteAllByUserId(Long userId);
}

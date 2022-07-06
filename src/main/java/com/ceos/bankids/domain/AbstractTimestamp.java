package com.ceos.bankids.domain;

import java.sql.Timestamp;
import javax.persistence.Column;
import javax.persistence.EntityListeners;
import javax.persistence.MappedSuperclass;
import lombok.Getter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Getter
@MappedSuperclass
@EntityListeners(value = {AuditingEntityListener.class})
public class AbstractTimestamp {

    @Column(nullable = false)
    @CreationTimestamp
    private Timestamp createdAt;

    @Column(nullable = false)
    @UpdateTimestamp
    private Timestamp updatedAt;
}
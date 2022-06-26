package com.ceos.bankids.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "Parent")
@NoArgsConstructor
@EqualsAndHashCode(of = "id")
public class Parent extends AbstractTimestamp {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 10)
    private Long educationLevel;

    @Column(nullable = false, length = 10)
    private Long lifeLevel;

    @Builder
    public Parent(
        Long id,
        Long educationLevel,
        Long lifeLevel
    ) {
        if (educationLevel == null) {
            throw new RuntimeException("주기는 필수값입니다.");
        }
        if (lifeLevel == null) {
            throw new RuntimeException("용돈은 필수값입니다.");
        }
        this.id = id;
        this.educationLevel = educationLevel;
        this.lifeLevel = lifeLevel;
    }
}

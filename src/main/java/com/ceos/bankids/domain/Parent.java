package com.ceos.bankids.domain;

import com.ceos.bankids.exception.BadRequestException;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
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

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Builder
    public Parent(
        Long id,
        Long educationLevel,
        Long lifeLevel,
        User user
    ) {
        if (educationLevel == null) {
            throw new BadRequestException("금융 교육 레벨은 필수값입니다.");
        }
        if (lifeLevel == null) {
            throw new BadRequestException("금융 생활 레벨은 필수값입니다.");
        }
        if (user == null) {
            throw new BadRequestException("유저는 필수값입니다.");
        }
        this.id = id;
        this.educationLevel = educationLevel;
        this.lifeLevel = lifeLevel;
        this.user = user;
    }
}

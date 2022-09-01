package com.ceos.bankids.domain;

import com.ceos.bankids.exception.BadRequestException;
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
@Table(name = "KidBackup")
@NoArgsConstructor
@EqualsAndHashCode(of = "id")
public class KidBackup extends AbstractTimestamp {

    // 공통
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 8)
    private String birthYear;

    @Column(nullable = false)
    private Boolean isKid;

    // 자녀
    @Column(nullable = false)
    private Long savings;

    @Column(nullable = false)
    private Long achievedChallenge;

    @Column(nullable = false)
    private Long totalChallenge;

    @Column(nullable = false)
    private Long level;

    @Builder
    public KidBackup(
        Long id,
        String birthYear,
        Boolean isKid,
        Long savings,
        Long achievedChallenge,
        Long totalChallenge,
        Long level
    ) {
        if (birthYear == null) {
            throw new BadRequestException("태어난 해는 필수값입니다.");
        }
        if (isKid == null) {
            throw new BadRequestException("부모/자녀 여부는 필수값입니다.");
        }
        if (savings == null) {
            throw new BadRequestException("총 저금액은 필수값입니다.");
        }
        if (achievedChallenge == null) {
            throw new BadRequestException("완주한 돈길 수는 필수값입니다.");
        }
        if (totalChallenge == null) {
            throw new BadRequestException("총 돈길 수는 필수값입니다.");
        }
        if (level == null) {
            throw new BadRequestException("레벨은 필수값입니다.");
        }

        this.id = id;
        this.birthYear = birthYear;
        this.isKid = isKid;
        this.savings = savings;
        this.achievedChallenge = achievedChallenge;
        this.totalChallenge = totalChallenge;
        this.level = level;
    }
}

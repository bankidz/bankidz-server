package com.ceos.bankids.domain;

import com.ceos.bankids.constant.ChallengeStatus;
import com.ceos.bankids.exception.BadRequestException;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.List;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

@Getter
@Setter
@Entity
@Table(name = "Challenge")
@NoArgsConstructor
@EqualsAndHashCode(of = "id")
@DynamicInsert
@DynamicUpdate
@ToString(exclude = {"progressList", "challengeUserList"})
public class Challenge extends AbstractTimestamp {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 20)
    private String title;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    @ColumnDefault("PENDING")
    private ChallengeStatus challengeStatus;

    @Column(nullable = false)
    private Long totalPrice;

    @Column(nullable = false)
    private Long weekPrice;

    @Column(nullable = false)
    private Long weeks;

    @Column(nullable = false)
    private Long interestRate;

    @Column(nullable = false)
    private Long interestPrice;

    @Column(nullable = false)
    @ColumnDefault("false")
    private Boolean isInterestPayment;

    @Column(nullable = false)
    @ColumnDefault("0")
    private Long successWeeks;

    @Column(nullable = false)
    private String fileName;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "targetItemId", nullable = false)
    private TargetItem targetItem;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "challengeCategoryId", nullable = false)
    private ChallengeCategory challengeCategory;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contractUserId", nullable = false)
    private User contractUser;

    @OneToMany(mappedBy = "challenge")
    private List<Progress> progressList;

    @JsonIgnore
    @OneToOne(mappedBy = "challenge", fetch = FetchType.LAZY)
    private ChallengeUser challengeUser;

    @OneToOne(mappedBy = "challenge", fetch = FetchType.LAZY)
    private Comment comment;

    @Builder
    public Challenge(
        Long id,
        String title,
        ChallengeStatus challengeStatus,
        Long totalPrice,
        Long weekPrice,
        Long weeks,
        Long interestRate,
        Long interestPrice,
        Boolean isInterestPayment,
        Long successWeeks,
        String filename,
        ChallengeCategory challengeCategory,
        User contractUser,
        TargetItem targetItem,
        Comment comment
    ) {
        if (title == null) {
            throw new BadRequestException("돈길 제목은 필수값입니다.");
        }
        if (totalPrice == null) {
            throw new BadRequestException("목표 금액은 필수값입니다.");
        }
        if (weekPrice == null) {
            throw new BadRequestException("주당 금액은 필수값입니다.");
        }
        if (weeks == null) {
            throw new BadRequestException("주차는 필수값입니다.");
        }
        if (weeks > 15) {
            throw new BadRequestException("주차는 15주를 넘을 수 없습니다.");
        }
        if (interestRate == null) {
            throw new BadRequestException("이자율은 필수값입니다.");
        }
        if (challengeCategory == null) {
            throw new BadRequestException("카테고리는 필수값입니다.");
        }
        if (contractUser == null) {
            throw new BadRequestException("계약 대상 유저는 필수값입니다.");
        }
        if (targetItem == null) {
            throw new BadRequestException("목표 아이템은 필수값입니다.");
        }

        this.id = id;
        this.challengeStatus = challengeStatus;
        this.title = title;
        this.totalPrice = totalPrice;
        this.weekPrice = weekPrice;
        this.weeks = weeks;
        this.interestRate = interestRate;
        this.interestPrice = interestPrice;
        this.isInterestPayment = isInterestPayment;
        this.successWeeks = successWeeks;
        this.fileName = filename;
        this.challengeCategory = challengeCategory;
        this.contractUser = contractUser;
        this.targetItem = targetItem;
        this.comment = comment;
    }
}

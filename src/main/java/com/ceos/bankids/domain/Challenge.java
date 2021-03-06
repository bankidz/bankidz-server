package com.ceos.bankids.domain;

import com.ceos.bankids.exception.BadRequestException;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.List;
import javax.persistence.Column;
import javax.persistence.Entity;
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
    @ColumnDefault("1")
    private Long isAchieved;

    @Column(nullable = false)
    private Long totalPrice;

    @Column(nullable = false)
    private Long weekPrice;

    @Column(nullable = false)
    private Long weeks;

    @Column(nullable = false)
    @ColumnDefault("1")
    private Long status;

    @Column(nullable = false)
    private Long interestRate;

    @Column(nullable = false)
    @ColumnDefault("0")
    private Long successWeeks;

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
        Long isAchieved,
        Long totalPrice,
        Long weekPrice,
        Long weeks,
        Long status,
        Long interestRate,
        Long successWeeks,
        ChallengeCategory challengeCategory,
        User contractUser,
        TargetItem targetItem,
        Comment comment
    ) {
        if (title == null) {
            throw new BadRequestException("?????? ????????? ??????????????????.");
        }
        if (totalPrice == null) {
            throw new BadRequestException("?????? ????????? ??????????????????.");
        }
        if (weekPrice == null) {
            throw new BadRequestException("?????? ????????? ??????????????????.");
        }
        if (weeks == null) {
            throw new BadRequestException("????????? ??????????????????.");
        }
        if (weeks > 15) {
            throw new BadRequestException("????????? 15?????? ?????? ??? ????????????.");
        }
        if (interestRate == null) {
            throw new BadRequestException("???????????? ??????????????????.");
        }
        if (challengeCategory == null) {
            throw new BadRequestException("??????????????? ??????????????????.");
        }
        if (contractUser == null) {
            throw new BadRequestException("?????? ?????? ????????? ??????????????????.");
        }
        if (targetItem == null) {
            throw new BadRequestException("?????? ???????????? ??????????????????.");
        }

        this.id = id;
        this.isAchieved = isAchieved;
        this.title = title;
        this.totalPrice = totalPrice;
        this.weekPrice = weekPrice;
        this.weeks = weeks;
        this.status = status;
        this.interestRate = interestRate;
        this.successWeeks = successWeeks;
        this.challengeCategory = challengeCategory;
        this.contractUser = contractUser;
        this.targetItem = targetItem;
        this.comment = comment;
    }
}

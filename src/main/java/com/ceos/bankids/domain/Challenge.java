package com.ceos.bankids.domain;

import com.ceos.bankids.exception.BadRequestException;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import java.util.List;
import javax.persistence.Column;
import javax.persistence.Entity;
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

@Getter
@Setter
@Entity
@Table(name = "Challenge")
@NoArgsConstructor
@EqualsAndHashCode(of = "id")
@DynamicInsert
@ToString(exclude = {"progressList", "challengeUserList"})
public class Challenge extends AbstractTimestamp {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 20)
    private String title;

    @Column(nullable = false)
    @ColumnDefault("false")
    private Boolean isAchieved;

    @Column(nullable = false)
    private Long totalPrice;

    @Column(nullable = false)
    private Long weekPrice;

    @Column(nullable = false)
    private Long weeks;

    @Column(nullable = false)
    private Long status;

    @Column(nullable = false)
    private Long interestRate;

    @JsonBackReference
    @ManyToOne
    @JoinColumn(name = "targetItemId", nullable = false)
    private TargetItem targetItem;

    @JsonBackReference
    @ManyToOne
    @JoinColumn(name = "challengeCategoryId", nullable = false)
    private ChallengeCategory challengeCategory;

    @JsonManagedReference
    @OneToMany(mappedBy = "challenge")
    private List<Progress> progressList;

    @JsonManagedReference
    @OneToMany(mappedBy = "challenge")
    private List<ChallengeUser> challengeUserList;

    @JsonManagedReference
    @OneToOne(mappedBy = "challenge")
    private Comment comment;

    @Builder
    public Challenge(
        Long id,
        String title,
        Boolean isAchieved,
        Long totalPrice,
        Long weekPrice,
        Long weeks,
        Long status,
        Long interestRate,
        ChallengeCategory challengeCategory,
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
        if (targetItem == null) {
            throw new BadRequestException("목표 아이템은 필수값입니다.");
        }

        this.id = id;
        this.isAchieved = isAchieved;
        this.title = title;
        this.totalPrice = totalPrice;
        this.weekPrice = weekPrice;
        this.weeks = weeks;
        this.status = status;
        this.interestRate = interestRate;
        this.challengeCategory = challengeCategory;
        this.targetItem = targetItem;
        this.comment = comment;
    }
}

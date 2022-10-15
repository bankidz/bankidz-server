package com.ceos.bankids.domain;

import com.ceos.bankids.exception.BadRequestException;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
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
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

@Entity
@Getter
@Setter
@Table(name = "Progress")
@NoArgsConstructor
@DynamicInsert
@EqualsAndHashCode(of = "id")
@Where(clause = "deleted_at is Null")
@SQLDelete(sql = "UPDATE progress SET deleted_at = CURRENT_TIMESTAMP where id = ?")
public class Progress extends AbstractTimestamp {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long weeks;

    @Column()
    @ColumnDefault("false") //default value: false
    private Boolean isAchieved;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "challengeId", nullable = false)
    private Challenge challenge;

    @Builder
    public Progress(
        Long id,
        Long weeks,
        Boolean isAchieved,
        Challenge challenge
    ) {
        if (weeks == null) {
            throw new BadRequestException("주차는 필수값입니다.");
        }
        if (weeks > 15) {
            throw new BadRequestException("주차는 15주를 넘을 수 없습니다.");
        }

        this.id = id;
        this.weeks = weeks;
        this.isAchieved = isAchieved;
        this.challenge = challenge;
    }
}

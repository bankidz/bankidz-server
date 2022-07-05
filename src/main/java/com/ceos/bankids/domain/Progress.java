package com.ceos.bankids.domain;

import com.ceos.bankids.exception.BadRequestException;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;

import javax.persistence.*;

@Entity
@Getter
@Setter
@Table(name = "progress")
@NoArgsConstructor
@EqualsAndHashCode(of = "id")
public class Progress extends AbstractTimestamp{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long weeks;

    @Column()
    @ColumnDefault("false") //default value: false
    private Boolean isAchieved;

    @ManyToOne
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

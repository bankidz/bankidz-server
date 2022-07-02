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
@Table(name = "Kid")
@NoArgsConstructor
@EqualsAndHashCode(of = "id")
public class Kid extends AbstractTimestamp {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 10)
    private String period;

    @Column(nullable = false, length = 10)
    private Long allowance;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Builder
    public Kid(
        Long id,
        String period,
        Long allowance,
        User user
    ) {
        if (period == null) {
            throw new BadRequestException("주기는 필수값입니다.");
        }
        if (allowance == null) {
            throw new BadRequestException("용돈은 필수값입니다.");
        }
        if (user == null) {
            throw new BadRequestException("유저는 필수값입니다.");
        }
        this.id = id;
        this.period = period;
        this.allowance = allowance;
        this.user = user;
    }
}

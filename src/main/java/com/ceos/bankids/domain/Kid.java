package com.ceos.bankids.domain;

import com.ceos.bankids.exception.BadRequestException;
import com.fasterxml.jackson.annotation.JsonFormat;
import java.sql.Timestamp;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

@Getter
@Setter
@Entity
@Table(name = "Kid")
@NoArgsConstructor
@DynamicUpdate
@DynamicInsert
@EqualsAndHashCode(of = "id")
public class Kid extends AbstractTimestamp {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    @ColumnDefault("0")
    private Long savings;

    @Column(nullable = false)
    @ColumnDefault("0")
    private Long achievedChallenge;

    @Column()
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd hh:mm:ss", timezone = "Asia/Seoul")
    private Timestamp deleteChallenge;

    @Column(nullable = false)
    @ColumnDefault("0")
    private Long totalChallenge;

    @Column(nullable = false)
    @ColumnDefault("1")
    private Long level;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "userId", nullable = false)
    private User user;

    @Builder
    public Kid(
        Long id,
        Long savings,
        Long achievedChallenge,
        Long totalChallenge,
        Timestamp deleteChallenge,
        Long level,
        User user
    ) {
        if (user == null) {
            throw new BadRequestException("유저는 필수값입니다.");
        }
        this.id = id;
        this.savings = savings;
        this.achievedChallenge = achievedChallenge;
        this.totalChallenge = totalChallenge;
        this.deleteChallenge = deleteChallenge;
        this.level = level;
        this.user = user;
    }
}

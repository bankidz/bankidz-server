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
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

@Entity
@Getter
@Setter
@Table(name = "ChallengeUser")
@NoArgsConstructor
@EqualsAndHashCode(of = "id")
@Where(clause = "deleted_at is Null")
@SQLDelete(sql = "UPDATE challenge_user SET deleted_at = CURRENT_TIMESTAMP where id = ?")
public class ChallengeUser extends AbstractTimestamp {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String member;

    @Column()
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd hh:mm:ss", timezone = "Asia/Seoul")
    private Timestamp deleted_at;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "challengeId", nullable = false)
    private Challenge challenge;

    @Builder
    public ChallengeUser(
        Long id,
        String member,
        User user,
        Challenge challenge
    ) {
        if (member == null) {
            throw new BadRequestException("돈길에 함께할 멤버 타입을 필수값입니다.");
        }
        if (user == null) {
            throw new BadRequestException("챌린지 참여 유저는 필수값입니다.");
        }
        if (challenge == null) {
            throw new BadRequestException("챌린지 정보는 필수값입니다.");
        }

        this.id = id;
        this.member = member;
        this.user = user;
        this.challenge = challenge;
    }
}

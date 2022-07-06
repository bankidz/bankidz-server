package com.ceos.bankids.domain;

import com.ceos.bankids.exception.BadRequestException;
import lombok.*;

import javax.persistence.*;

@Entity
@Getter
@Setter
@Table(name = "challengeUser")
@NoArgsConstructor
@EqualsAndHashCode(of = "id")
public class ChallengeUser extends AbstractTimestamp{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String member;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
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

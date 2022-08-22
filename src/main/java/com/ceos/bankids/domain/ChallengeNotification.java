package com.ceos.bankids.domain;

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

@Getter
@Setter
@Entity
@Table(name = "ChallengeNotification")
@NoArgsConstructor
@EqualsAndHashCode(of = "id")
public class ChallengeNotification extends AbstractTimestamp {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private String message;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "challengeUserId", nullable = false)
    private ChallengeUser challengeUser;

    @Builder
    public ChallengeNotification(
        Long id,
        String message,
        ChallengeUser challengeUser
    ) {
        this.id = id;
        this.message = message;
        this.challengeUser = challengeUser;
    }
}

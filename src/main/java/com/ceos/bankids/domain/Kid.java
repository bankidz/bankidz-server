package com.ceos.bankids.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

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

    @Column(nullable = false, unique = true, length = 10)
    private String username;

    @Column(columnDefinition = "TEXT")
    private String image;

    @Column(name = "authentication_code", nullable = false, unique = true)
    private String authenticationCode;

    @Column(nullable = false, length = 10)
    private String provider;

    @Column(nullable = false, length = 10)
    private String period;

    @Column(nullable = false, length = 10)
    private Long allowance;

    @Builder
    public Kid(
        Long id,
        String username,
        String image,
        String authenticationCode,
        String provider,
        String period,
        Long allowance
    ) {
        if (username == null) {
            throw new RuntimeException("이름은 필수값입니다.");
        }
        if (authenticationCode == null) {
            throw new RuntimeException("인증 코드는 필수값입니다.");
        }
        if (provider == null) {
            throw new RuntimeException("provider는 필수값입니다.");
        }
        if (period == null) {
            throw new RuntimeException("주기는 필수값입니다.");
        }
        if (allowance == null) {
            throw new RuntimeException("용돈은 필수값입니다.");
        }

        this.id = id;
        this.username = username;
        this.image = image;
        this.authenticationCode = authenticationCode;
        this.provider = provider;
        this.period = period;
        this.allowance = allowance;
    }
}

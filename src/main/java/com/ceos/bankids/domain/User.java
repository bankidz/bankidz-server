package com.ceos.bankids.domain;

import com.ceos.bankids.exception.BadRequestException;
import java.util.List;
import javax.persistence.*;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@Entity
@Table(name = "User")
@NoArgsConstructor
@EqualsAndHashCode(of = "id")
@ToString(exclude = {"kids", "parents"})
public class User extends AbstractTimestamp {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 10)
    private String username;

    @Column(columnDefinition = "tinyint(1) default 0")
    private Boolean isFemale;

    @Column(nullable = false)
    private String birthday;

    @Column(nullable = false, unique = true)
    private String authenticationCode;

    @Column(nullable = false, length = 10)
    private String provider;

    @Column(columnDefinition = "tinyint(1) default 0")
    private Boolean isKid;

    @Column(columnDefinition = "TEXT")
    private String refreshToken;

    @OneToMany(mappedBy = "user")
    private List<Kid> kids;

    @OneToMany(mappedBy = "user")
    private List<Parent> parents;

    @OneToMany(mappedBy = "user")
    private List<LinkChallenge> link_challengeList;

    @Builder
    public User(
        Long id,
        String username,
        Boolean isFemale,
        String birthday,
        String authenticationCode,
        String provider,
        Boolean isKid,
        String refreshToken
    ) {
        if (username == null) {
            throw new BadRequestException("이름은 필수값입니다.");
        }
        if (birthday == null) {
            throw new BadRequestException("생년월일은 필수값입니다.");
        }
        if (authenticationCode == null) {
            throw new BadRequestException("인증 코드는 필수값입니다.");
        }
        if (provider == null) {
            throw new BadRequestException("provider는 필수값입니다.");
        }
        if (refreshToken == null) {
            throw new BadRequestException("refreshToken은 필수값입니다.");
        }

        this.id = id;
        this.username = username;
        this.isFemale = isFemale;
        this.birthday = birthday;
        this.authenticationCode = authenticationCode;
        this.provider = provider;
        this.isKid = isKid;
        this.refreshToken = refreshToken;
    }
}

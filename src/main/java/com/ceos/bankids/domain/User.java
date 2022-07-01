package com.ceos.bankids.domain;

import com.ceos.bankids.exception.BadRequestException;
import java.util.List;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
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

    @Column(nullable = false)
    private Boolean isFemale;

    @Column(nullable = false)
    private String birthday;

    @Column(name = "authentication_code", nullable = false, unique = true)
    private String authenticationCode;

    @Column(nullable = false, length = 10)
    private String provider;

    @Column(nullable = false)
    private Boolean isKid;

    @OneToMany(mappedBy = "user")
    private List<Kid> kids;

    @OneToMany(mappedBy = "user")
    private List<Parent> parents;

    @Builder
    public User(
        Long id,
        String username,
        Boolean isFemale,
        String birthday,
        String authenticationCode,
        String provider,
        Boolean isKid
    ) {
        if (username == null) {
            throw new BadRequestException("이름은 필수값입니다.");
        }
        if (isFemale == null) {
            throw new BadRequestException("성별은 필수값입니다.");
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
        if (isKid == null) {
            throw new BadRequestException("부모 자녀 구분은 필수값입니다.");
        }

        this.id = id;
        this.username = username;
        this.isFemale = isFemale;
        this.birthday = birthday;
        this.authenticationCode = authenticationCode;
        this.provider = provider;
        this.isKid = isKid;
    }
}

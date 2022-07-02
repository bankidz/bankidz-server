package com.ceos.bankids.domain;

import com.ceos.bankids.exception.BadRequestException;
import java.util.Collection;
import java.util.Collections;
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
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

@Getter
@Setter
@Entity
@Table(name = "User")
@NoArgsConstructor
@EqualsAndHashCode(of = "id")
@ToString(exclude = {"kids", "parents"})
public class User extends AbstractTimestamp implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 10)
    private String username;

    @Column(columnDefinition = "tinyint(1) default 0")
    private Boolean isFemale;

    @Column(nullable = false)
    private String birthday;

    @Column(name = "authentication_code", nullable = false, unique = true)
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

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singleton(new SimpleGrantedAuthority("USER"));
    }

    @Override
    public String getPassword() {
        return null;
    }

    @Override
    public boolean isAccountNonExpired() {
        return false;
    }

    @Override
    public boolean isAccountNonLocked() {
        return false;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return false;
    }

    @Override
    public boolean isEnabled() {
        return false;
    }
}

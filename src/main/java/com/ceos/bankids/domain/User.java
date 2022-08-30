package com.ceos.bankids.domain;

import com.ceos.bankids.exception.BadRequestException;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.DynamicUpdate;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

@Getter
@Setter
@Entity
@Table(name = "User")
@NoArgsConstructor
@DynamicUpdate
@EqualsAndHashCode(of = "id")
public class User extends AbstractTimestamp implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 10)
    private String username;

    @Column(nullable = true)
    private Boolean isFemale;

    @Column(nullable = true, length = 8)
    private String birthday;

    @Column(nullable = true, length = 12, unique = true)
    private String phone;

    @Column(name = "authentication_code", nullable = false, unique = true)
    private String authenticationCode;

    @Column(nullable = false, length = 10)
    private String provider;

    @Column(nullable = true)
    private Boolean isKid;

    @Column(columnDefinition = "TEXT")
    private String refreshToken;

    @Column(columnDefinition = "TEXT")
    private String expoToken;

    @OneToOne(mappedBy = "user", fetch = FetchType.LAZY)
    private Kid kid;

    @OneToOne(mappedBy = "user", fetch = FetchType.LAZY)
    private Parent parent;

    @OneToMany(mappedBy = "user")
    private List<ChallengeUser> challengeUserList;

    @OneToMany(mappedBy = "user")
    private List<FamilyUser> familyUserList;

    @OneToMany(mappedBy = "user")
    private List<Comment> commentList;

    @Builder
    public User(
        Long id,
        String username,
        Boolean isFemale,
        String birthday,
        String phone,
        String authenticationCode,
        String provider,
        Boolean isKid,
        String refreshToken,
        String expoToken,
        Parent parent,
        Kid kid
    ) {
        if (username == null) {
            throw new BadRequestException("이름은 필수값입니다.");
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
        this.phone = phone;
        this.authenticationCode = authenticationCode;
        this.provider = provider;
        this.isKid = isKid;
        this.refreshToken = refreshToken;
        this.expoToken = expoToken;
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

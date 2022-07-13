package com.ceos.bankids.domain;

import com.ceos.bankids.exception.BadRequestException;
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
@Table(name = "FamilyUser")
@NoArgsConstructor
@EqualsAndHashCode(of = "id")
public class FamilyUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "userId", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "familyId", nullable = false)
    private Family family;

    @Builder
    public FamilyUser(
        Long id,
        User user,
        Family family
    ) {
        if (user == null) {
            throw new BadRequestException("챌린지 참여 유저는 필수값입니다.");
        }
        if (family == null) {
            throw new BadRequestException("가족 정보는 필수값입니다.");
        }

        this.id = id;
        this.user = user;
        this.family = family;
    }
}

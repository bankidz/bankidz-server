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
@Table(name = "Family")
@NoArgsConstructor
@EqualsAndHashCode(of = "id")
@ToString(exclude = "familyUserList")
public class Family {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String code;

    @OneToMany(mappedBy = "user")
    private List<FamilyUser> familyUserList;

    @Builder
    public Family(
        Long id,
        String code
    ) {
        if (code == null) {
            throw new BadRequestException("가족코드는 필수값입니다.");
        }
        this.id = id;
        this.code = code;
    }
}

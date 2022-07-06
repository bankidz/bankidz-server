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

@Entity
@Getter
@Setter
@Table(name = "ChallengeCategory")
@NoArgsConstructor
@EqualsAndHashCode(of = "id")
@ToString(exclude = "challengeList")
public class ChallengeCategory extends AbstractTimestamp {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String category;

    @OneToMany(mappedBy = "challengeCategory")
    private List<Challenge> challengeList;

    @Builder
    public ChallengeCategory(
        Long id,
        String category
    ) {
        if (category == null) {
            throw new BadRequestException("카테고리 이름은 필수값입니다.");
        }

        this.id = id;
        this.category = category;
    }

}

package com.ceos.bankids.domain;

import com.ceos.bankids.exception.BadRequestException;
import lombok.*;

import javax.persistence.*;
import java.util.List;

@Entity
@Getter
@Setter
@Table(name = "ChallengeCategory")
@NoArgsConstructor
@EqualsAndHashCode(of = "id")
public class ChallengeCategory extends AbstractTimestamp{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String category;

    @OneToMany(mappedBy = "challengeCategory")
    private List<Challenge> challenges;

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

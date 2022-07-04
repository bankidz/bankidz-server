package com.ceos.bankids.domain;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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

    @OneToMany (mappedBy = "challengeCategory")
    private List<Challenge> challenges;

}

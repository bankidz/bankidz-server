package com.ceos.bankids.domain;

import com.ceos.bankids.exception.BadRequestException;
import com.fasterxml.jackson.annotation.JsonManagedReference;
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
@Table(name = "TargetItem")
@NoArgsConstructor
@EqualsAndHashCode(of = "id")
@ToString(exclude = "challengeList")
public class TargetItem extends AbstractTimestamp {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @JsonManagedReference
    @OneToMany(mappedBy = "targetItem")
    private List<Challenge> challengeList;

    @Builder
    public TargetItem(
        Long id,
        String name
    ) {
        if (name == null) {
            throw new BadRequestException("아이템 이름은 필수값입니다.");
        }

        this.id = id;
        this.name = name;
    }
}

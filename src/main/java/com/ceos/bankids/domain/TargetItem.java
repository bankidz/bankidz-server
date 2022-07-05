package com.ceos.bankids.domain;

import com.ceos.bankids.exception.BadRequestException;
import lombok.*;

import javax.persistence.*;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(of = "id")
@Table(name = "TargetItem")
public class TargetItem extends AbstractTimestamp{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "targetItemId")
    private Long id;

    @Column(nullable = false)
    private String itemName;

    @OneToMany(mappedBy = "targetItem")
    private List<Challenge> challengeList;

    @Builder
    public TargetItem(
        Long id,
        String itemName
    ) {
        if (itemName == null) {
            throw new BadRequestException("아이템 이름은 필수값입니다.");
        }

        this.id = id;
        this.itemName = itemName;
    }
}

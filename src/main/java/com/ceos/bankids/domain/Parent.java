package com.ceos.bankids.domain;

import com.ceos.bankids.exception.BadRequestException;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.DynamicUpdate;

@Getter
@Setter
@Entity
@Table(name = "Parent")
@NoArgsConstructor
@DynamicUpdate
@EqualsAndHashCode(of = "id")
public class Parent extends AbstractTimestamp {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 10)
    @ColumnDefault("0")
    private Long savings;

    @OneToOne
    @JoinColumn(name = "userId", nullable = false)
    private User user;

    @Builder
    public Parent(
        Long id,
        Long savings,
        User user
    ) {
        if (user == null) {
            throw new BadRequestException("유저는 필수값입니다.");
        }
        this.id = id;
        this.savings = savings;
        this.user = user;
    }
}

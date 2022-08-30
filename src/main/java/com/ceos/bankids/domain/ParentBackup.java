package com.ceos.bankids.domain;

import com.ceos.bankids.exception.BadRequestException;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.DynamicUpdate;

@Getter
@Setter
@Entity
@Table(name = "ParentBackup")
@NoArgsConstructor
@DynamicUpdate
public class ParentBackup extends AbstractTimestamp {

    // 공통
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 8)
    private String birthYear;

    @Column(nullable = false)
    private Boolean isKid;

    // 부모
    @Column(nullable = false)
    private Long acceptedRequest;

    @Column(nullable = false)
    private Long totalRequest;

    @Builder
    public ParentBackup(
        Long id,
        String birthYear,
        Boolean isKid,
        Long acceptedRequest,
        Long totalRequest
    ) {
        if (birthYear == null) {
            throw new BadRequestException("태어난 해는 필수값입니다.");
        }
        if (isKid == null) {
            throw new BadRequestException("부모/자녀 여부는 필수값입니다.");
        }
        if (acceptedRequest == null) {
            throw new BadRequestException("수락한 요청 수는 필수값입니다.");
        }
        if (totalRequest == null) {
            throw new BadRequestException("총 요청 수는 필수값입니다.");
        }

        this.id = id;
        this.birthYear = birthYear;
        this.isKid = isKid;
        this.acceptedRequest = acceptedRequest;
        this.totalRequest = totalRequest;
    }
}

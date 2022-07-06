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

@Getter
@Setter
@Entity
@Table(name = "Comment")
@NoArgsConstructor
@EqualsAndHashCode(of = "id")
public class Comment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String content;

    @OneToOne
    @JoinColumn(name = "challengeId", nullable = false)
    private Challenge challenge;

    @Builder
    public Comment(
        Long id,
        String content,
        Challenge challenge
    ) {
        if (content == null) {
            throw new BadRequestException("내용은 필수값입니다.");
        }
        if (challenge == null) {
            throw new BadRequestException("챌린지는 필수값입니다.");
        }
        this.id = id;
        this.content = content;
        this.challenge = challenge;
    }
}

package com.ceos.bankids.domain;

import com.ceos.bankids.exception.BadRequestException;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.sql.Timestamp;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

@Getter
@Setter
@Entity
@Table(name = "Comment")
@NoArgsConstructor
@DynamicUpdate
@EqualsAndHashCode(of = "id")
@Where(clause = "deleted_at is Null")
@SQLDelete(sql = "UPDATE comment SET deleted_at = CURRENT_TIMESTAMP where id = ?")
public class Comment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String content;

    @Column()
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd hh:mm:ss", timezone = "Asia/Seoul")
    private Timestamp deleted_at;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "challengeId", nullable = false)
    @JsonIgnore
    private Challenge challenge;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "userId", nullable = false)
    @JsonIgnore
    private User user;

    @Builder
    public Comment(
        Long id,
        String content,
        Challenge challenge,
        User user
    ) {
        if (content == null) {
            throw new BadRequestException("내용은 필수값입니다.");
        }
        if (challenge == null) {
            throw new BadRequestException("챌린지는 필수값입니다.");
        }
        if (user == null) {
            throw new BadRequestException("유저는 필수값입니다.");
        }
        this.id = id;
        this.content = content;
        this.challenge = challenge;
        this.user = user;
    }
}

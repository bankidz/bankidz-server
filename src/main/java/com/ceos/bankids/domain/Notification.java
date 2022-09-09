package com.ceos.bankids.domain;

import com.ceos.bankids.constant.NotificationCategory;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.DynamicInsert;

@Getter
@Setter
@Entity
@Table(name = "Notification")
@NoArgsConstructor
@EqualsAndHashCode(of = "id")
@DynamicInsert
public class Notification extends AbstractTimestamp {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private String title;

    @Column
    private String message;

    @Column(nullable = false)
    @ColumnDefault(value = "false")
    private Boolean isRead;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private NotificationCategory notificationCategory;

    @Column(nullable = false)
    private String linkUrl;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "userId", nullable = false)
    private User user;

    @Builder
    public Notification(
        Long id,
        String title,
        String message,
        Boolean isRead,
        NotificationCategory notificationCategory,
        String linkUrl,
        User user
    ) {

        this.id = id;
        this.title = title;
        this.message = message;
        this.isRead = isRead;
        this.notificationCategory = notificationCategory;
        this.linkUrl = linkUrl;
        this.user = user;
    }
}

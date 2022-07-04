package com.ceos.bankids.dto;

import com.ceos.bankids.domain.Challenge;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.sql.Timestamp;


@Getter
@ToString
@EqualsAndHashCode
public class ChallengeDTO {

    private Long id;
    private String title;
    private Boolean isAchieved;
    private Long interestRate;
    private Long totalPrice;
    private Long weekPrice;
    private Long weeks;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd", timezone = "Asia/Seoul")
    private Timestamp createdAt;
    private Long status;

    public ChallengeDTO(Challenge challenge) {
        this.id = challenge.getId();
        this.title = challenge.getTitle();
        this.isAchieved = challenge.getIsAchieved();
        this.interestRate = challenge.getInterestRate();
        this.totalPrice = challenge.getTotalPrice();
        this.weekPrice = challenge.getWeekPrice();
        this.weeks = challenge.getWeeks();
        this.createdAt = challenge.getCreatedAt();
        this.status = challenge.getStatus();
    }
}

package com.ceos.bankids.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ErrorCode implements EnumMapperType {

    // Kakao
    KAKAO_BAD_REQUEST("E400-10001"),

    // User
    USER_NOT_EXISTS("E400-20001"),
    USER_ALREADY_HAS_TYPE("E400-20002"),
    INVALID_BIRTHDAY("E400-20003"),
    USER_TYPE_NOT_CHOSEN("E400-20004"),

    // Family
    FAMILY_NOT_EXISTS("E403-30001"),
    KID_FORBIDDEN("E403-30002"),
    FAMILY_TO_JOIN_NOT_EXISTS("E400-30003"),
    INVALID_USER_TYPE("E400-30004"),
    MOM_ALREADY_EXISTS("E403-30005"),
    DAD_ALREADY_EXISTS("E403-30006"),
    USER_ALREADY_IN_THIS_FAMILY("E403-30007"),
    USER_NOT_IN_THIS_FAMILY("E400-30008"),
    USER_NOT_IN_ANY_FAMILY("E400-30009"),

    // Challenge
    CHALLENGE_COUNT_OVER_FIVE("E403-40001"),
    NOT_EXIST_FAMILY("E403-40002"),
    NOT_EXIST_CONSTRUCT_USER("E400-40003"),
    NOT_EXIST_CATEGORY("E400-40004"),
    NOT_EXIST_ITEM("E400-40005"),
    NOT_MATCH_CHALLENGE_USER("E403-40006"),
    NOT_TWO_WEEKS_YET("E400-40007"),
    NOT_EXIST_CHALLENGE("E400-40008"),
    INVALID_QUERYPARAM("E400-40009"),
    NOT_MATCH_CONTRACT_USER("E403-40010"),
    ALREADY_APPROVED_CHALLENGE("E400-40011"),
    KID_CHALLENGE_COUNT_OVER_FIVE("E403-40012"),
    NOT_EXIST_KID("E400-40013"),
    SUNDAY_ERROR("E403-40014"),
    TIMELOGIC_ERROR("E403-40015"),
    USER_ROLE_ERROR("E403-40016"),

    // Progress
    NOT_WALKING_CHALLENGE("E400-50001"),
    NOT_EXIST_PROGRESS("E400-50002"),
    ALREADY_WALK_PROGRESS("E400-50003"),

    // S3
    PRESIGNEDURI_ERROR("E400-60001"),
    PRESIGNEDURI_NPE("E400-60002");

    private final String errorCode;

    @Override
    public String getCode() {
        return name();
    }
}
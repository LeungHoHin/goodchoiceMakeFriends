package com.lhx.goodchoice.pojo.enums;

public enum TeamStatusEnum {
    PUBLIC(0, "公开"),
    PRIVATE(1, "私有"),
    SECRET(2, "加密");

    private int value;

    private String status;

    public static TeamStatusEnum getStatusByValue(Integer value) {
        if (value == null) {
            return null;
        }
        TeamStatusEnum[] values = TeamStatusEnum.values();
        for (TeamStatusEnum teamStatusEnum : values) {
            if (teamStatusEnum.getValue() == value) {
                return teamStatusEnum;
            }
        }
        return null;
    }

    TeamStatusEnum(int value, String status) {
        this.value = value;
        this.status = status;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}

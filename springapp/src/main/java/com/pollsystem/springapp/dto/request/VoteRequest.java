package com.pollsystem.springapp.dto.request;

import jakarta.validation.constraints.NotNull;

public class VoteRequest {
    @NotNull
    private Long optionId;

    public Long getOptionId() {
        return optionId;
    }

    public void setOptionId(Long optionId) {
        this.optionId = optionId;
    }
}
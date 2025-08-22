package com.pollsystem.springapp.dto.response;

public class OptionResponse {
    private Long id;
    private String text;
    private Integer voteCount;
    private Double percentage;

    public OptionResponse() {}

    public OptionResponse(Long id, String text, Integer voteCount, Double percentage) {
        this.id = id;
        this.text = text;
        this.voteCount = voteCount;
        this.percentage = percentage;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public Integer getVoteCount() {
        return voteCount;
    }

    public void setVoteCount(Integer voteCount) {
        this.voteCount = voteCount;
    }

    public Double getPercentage() {
        return percentage;
    }

    public void setPercentage(Double percentage) {
        this.percentage = percentage;
    }
}
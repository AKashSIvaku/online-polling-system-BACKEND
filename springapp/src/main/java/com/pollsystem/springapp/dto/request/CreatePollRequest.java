package com.pollsystem.springapp.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import java.util.List;

public class CreatePollRequest {
    @NotBlank
    @Size(max = 1000)
    private String question;

    @NotEmpty
    @Size(min = 2, max = 10)
    private List<String> options;

    private String privacy = "PUBLIC";

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public List<String> getOptions() {
        return options;
    }

    public void setOptions(List<String> options) {
        this.options = options;
    }

    public String getPrivacy() {
        return privacy;
    }

    public void setPrivacy(String privacy) {
        this.privacy = privacy;
    }
}
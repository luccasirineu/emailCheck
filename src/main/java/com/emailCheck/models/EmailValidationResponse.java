package com.emailCheck.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class EmailValidationResponse {

    private String email;
    private String status;
    private String reason;


    @JsonProperty("score")
    private int score;


    public EmailValidationResponse() {
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    @Override
    public String toString() {
        return "EmailValidationResponse{" +
                "email='" + email + '\'' +
                ", status='" + status + '\'' +
                ", reason='" + reason + '\'' +
                ", score=" + score +
                '}';
    }

    public boolean isEmailExists() {
        return "deliverable".equalsIgnoreCase(status);
    }
}
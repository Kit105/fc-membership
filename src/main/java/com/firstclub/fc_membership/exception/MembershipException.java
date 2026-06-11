package com.firstclub.fc_membership.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class MembershipException extends RuntimeException {

    private final HttpStatus status;

    public MembershipException(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }
}
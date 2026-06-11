package com.firstclub.fc_membership.exception;

import org.springframework.http.HttpStatus;

public class ActiveMembershipExistsException extends MembershipException {

    public ActiveMembershipExistsException(Long userId) {
        super("User " + userId + " already has an active membership. Cancel it before subscribing again.",
                HttpStatus.CONFLICT);
    }
}
package com.firstclub.fc_membership.service;

import com.firstclub.fc_membership.dto.request.SubscribeMembershipRequest;
import com.firstclub.fc_membership.dto.response.MembershipResponse;
import java.util.List;

public interface MembershipService {

    MembershipResponse subscribe(Long userId, SubscribeMembershipRequest request);

    MembershipResponse getCurrentMembership(Long userId);

    List<MembershipResponse> getMembershipHistory(Long userId);

    MembershipResponse upgradeTier(Long userId);

    MembershipResponse downgradeTier(Long userId);

    MembershipResponse cancelMembership(Long userId);

    void processExpiredMemberships();
}
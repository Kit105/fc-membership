package com.firstclub.fc_membership.service;

import com.firstclub.fc_membership.dto.request.CreateUserRequest;
import com.firstclub.fc_membership.dto.response.UserResponse;
import com.firstclub.fc_membership.entity.User;
import com.firstclub.fc_membership.exception.MembershipException;
import com.firstclub.fc_membership.exception.ResourceNotFoundException;
import com.firstclub.fc_membership.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    @Transactional
    public UserResponse createUser(CreateUserRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new MembershipException(
                    "Email already registered: " + request.getEmail(), HttpStatus.CONFLICT);
        }
        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .cohort(request.getCohort())
                .build();
        return toResponse(userRepository.save(user));
    }

    @Transactional(readOnly = true)
    public UserResponse getUserById(Long userId) {
        return userRepository.findById(userId)
                .map(this::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));
    }

    @Transactional(readOnly = true)
    public List<UserResponse> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    private UserResponse toResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .cohort(user.getCohort())
                .createdAt(user.getCreatedAt())
                .build();
    }
}
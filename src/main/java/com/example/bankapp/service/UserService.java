package com.example.bankapp.service;

import com.example.bankapp.dto.request.ChangePasswordRequest;
import com.example.bankapp.entity.User;

public interface UserService {
    User getUserByUsername(String username);
    User updateProfile(String username, String fullName, String phoneNumber);
    void changePassword(String username, ChangePasswordRequest request);
}
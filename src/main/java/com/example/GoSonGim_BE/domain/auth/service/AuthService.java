package com.example.GoSonGim_BE.domain.auth.service;

import com.example.GoSonGim_BE.domain.auth.dto.request.SignupRequest;
import com.example.GoSonGim_BE.domain.auth.dto.response.SignupResponse;

public interface AuthService {
    SignupResponse signup(SignupRequest request);
}

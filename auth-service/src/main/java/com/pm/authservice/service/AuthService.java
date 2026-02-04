package com.pm.authservice.service;

import com.pm.authservice.dto.LoginRequestDTO;
import com.pm.authservice.dto.SignupRequestDTO;
import com.pm.authservice.grpc.UserProfileGrpcClient;
import com.pm.authservice.model.User;
import com.pm.authservice.repository.UserRepository;
import com.pm.authservice.util.JwtUtil;
import jakarta.transaction.Transactional;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import userservice.UserProfileRequest;
import userservice.UserProfileResponse;
import userservice.UserProfileServiceGrpc;

import java.util.Optional;

@Service
public class AuthService {

        private final JwtUtil jwtUtil;
        private final UserService userService;
        private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final UserProfileGrpcClient userProfileGrpcClient;

        public AuthService(UserService userService, PasswordEncoder passwordEncoder, JwtUtil jwtUtil, UserRepository userRepository, UserProfileGrpcClient userProfileGrpcClient){
            this.userService = userService;
            this.userProfileGrpcClient = userProfileGrpcClient;
            this.passwordEncoder = passwordEncoder;
            this.jwtUtil = jwtUtil;
        this.userRepository = userRepository;
    }
        @Transactional
        public String register(SignupRequestDTO signupRequestDTO){
            if(userService.findByEmail(signupRequestDTO.getEmail()).isPresent()){
                throw new RuntimeException("Email already in use");
            }
            User newUser = new User();
            newUser.setEmail(signupRequestDTO.getEmail());
            newUser.setPassword(passwordEncoder.encode(signupRequestDTO.getPassword()));
            newUser.setRole(signupRequestDTO.getRole());
            userRepository.save(newUser);

            userProfileGrpcClient.createProfile(
                    newUser.getId().toString(),
                    signupRequestDTO.getFirstName(),
                    signupRequestDTO.getLastName(),
                    String.valueOf(signupRequestDTO.getPhone())
            );

            return jwtUtil.generateToken(newUser.getEmail(), newUser.getRole());
        }
        public Optional<String> authenticate(LoginRequestDTO loginRequestDTO){
            Optional<String> token = userService.findByEmail(loginRequestDTO.getEmail())
                    .filter( u -> passwordEncoder.matches(loginRequestDTO.getPassword(), u.getPassword()))
                    .map( u -> jwtUtil.generateToken(u.getEmail(), u.getRole()));

            return token;
        }
        public boolean validateToken(String token){
            try{
                jwtUtil.validateToken(token);
                return true;
            } catch(Exception e){
                return false;
            }
        }
}

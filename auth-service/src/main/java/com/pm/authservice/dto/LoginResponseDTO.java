package com.pm.authservice.dto;

public class LoginResponseDTO {
    public LoginResponseDTO(String token){ this.token = token;}

    private final String token;

    public String getToken(){return token;}

}

package edu.utec.sienep.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LoginResponseDto {

    private String token;
    private Integer userId;
    private String username;
}

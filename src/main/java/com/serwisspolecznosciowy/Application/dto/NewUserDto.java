package com.serwisspolecznosciowy.Application.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NewUserDto {


    @NotNull(message = "Username can not be null!")
    @Size(min = 3, max = 45)
    private String username;

    @NotNull(message = "Password can not be null!")
    @Size(min = 6, max = 64)
    private String password;

    private String profilePicture;

}

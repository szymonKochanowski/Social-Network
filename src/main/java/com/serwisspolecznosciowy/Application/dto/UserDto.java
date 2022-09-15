package com.serwisspolecznosciowy.Application.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserDto {

    @Column(unique = true)
    @NotNull(message = "Username can not be null!")
    @Size(min = 3, max = 45)
    private String username;

    private String profilePicture;

    private LocalDateTime created;

    private LocalDateTime updated;

}

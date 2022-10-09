package com.serwisspolecznosciowy.Application.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PostDto {

    @NotNull
    private String body;

    @NotNull
    private LocalDateTime created;

    private LocalDateTime updated;

    private List<LikeDto> likeDtoList;

    private List<DislikeDto> dislikeDtoList;

    @NotNull(message = "Username can not be null!")
    @Size(min = 3, max = 45)
    private String username;

    private String profilePicture;

    private Integer numberOfComments;

}

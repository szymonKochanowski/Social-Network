package com.serwisspolecznosciowy.Application.dto;

import com.serwisspolecznosciowy.Application.entity.Dislike;
import com.serwisspolecznosciowy.Application.entity.Like;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PostDtoWithAuthor {

    @NotNull
    private String body;

    @NotNull
    private LocalDateTime created;

    private LocalDateTime updated;

    private List<Like> likeList;

    private List<Dislike> dislikeList;

    @NotNull(message = "Username can not be null!")
    @Size(min = 3, max = 45)
    private String username;

    private String profilePicture;

    private Integer numberOfComments;

}

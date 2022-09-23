package com.serwisspolecznosciowy.Application.dto;

import com.serwisspolecznosciowy.Application.entity.Dislike;
import com.serwisspolecznosciowy.Application.entity.Like;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CommentDtoWithAuthor {

    @NotNull
    private String body;

    @NotNull
    private LocalDateTime created;

    private LocalDateTime updated;

    private List<Like> likeList;

    private List<Dislike> dislikeList;

    private UserDto user;

}

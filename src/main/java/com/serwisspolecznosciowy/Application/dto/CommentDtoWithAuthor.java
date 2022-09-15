package com.serwisspolecznosciowy.Application.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.CascadeType;
import javax.persistence.ManyToOne;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CommentDtoWithAuthor {

    @NotNull
    private String body;

    @NotNull
    private LocalDateTime created;

    private LocalDateTime updated;

    private Integer numberOfLikes;

    private Integer numberOfDislikes;

    @NotNull
    @ManyToOne(cascade = CascadeType.DETACH)
    private UserDto user;

}

package com.serwisspolecznosciowy.Application.entity;

import lombok.*;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@RequiredArgsConstructor
@Data
@Table(name = "comments")
public class Comment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @NonNull
    private String body;

    @NonNull
    private LocalDateTime created;

    private LocalDateTime updated;

    @NonNull
    private Integer postId;

    @NonNull
    @ManyToOne(cascade = CascadeType.DETACH)
    private User user;

    @NonNull
    private Integer numberOfLikes;

    @NonNull
    private Integer numberOfDislikes;
}

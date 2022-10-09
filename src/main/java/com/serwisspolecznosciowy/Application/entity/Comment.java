package com.serwisspolecznosciowy.Application.entity;

import lombok.*;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

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

    @OneToMany(cascade = CascadeType.DETACH)
    @JoinColumn(name = "commentLikeId", updatable = false, insertable = false)
    private List<Like> likeList;

    @OneToMany(cascade = CascadeType.DETACH)
    @JoinColumn(name = "commentDislikeId", updatable = false, insertable = false)
    private List<Dislike> dislikeList;
}

package com.serwisspolecznosciowy.Application.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Data
@Table(name = "posts")
public class Post {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @NonNull
    private String body;

    @NonNull
    private LocalDateTime created;

    private LocalDateTime updated;

    @NonNull
    @ManyToOne(cascade = CascadeType.DETACH)
    private User user;

    @OneToMany(cascade = CascadeType.DETACH)
    @JoinColumn(name = "postId", updatable = false, insertable = false)
    private List<Comment> commentList;

    @OneToMany(cascade = CascadeType.DETACH)
    @JoinColumn(name = "postLikeId", updatable = false, insertable = false)
    private List<Like> likeList;

    @OneToMany(cascade = CascadeType.DETACH)
    @JoinColumn(name = "postDislikeId", updatable = false, insertable = false)
    private List<Dislike> dislikeList;

    private Integer numberOfComments;

}

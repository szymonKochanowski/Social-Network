package com.serwisspolecznosciowy.Application.repository;

import com.serwisspolecznosciowy.Application.entity.Comment;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Integer> {

    List<Comment> findAllByPostIdIn(List<Integer> postsIds);

    @Query("Select p From Comment p")
    List<Comment> findAllComments(Pageable page);

    Optional<List<Comment>> findAllCommentByUserId(Integer userId);

    List<Comment> findAllCommentsByPostId(Integer postId);

    List<Comment> findAllByBodyContaining(String body);

}

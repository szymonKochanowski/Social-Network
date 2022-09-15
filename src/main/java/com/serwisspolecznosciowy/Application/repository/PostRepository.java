package com.serwisspolecznosciowy.Application.repository;

import com.serwisspolecznosciowy.Application.entity.Post;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PostRepository extends JpaRepository<Post, Integer> {

    @Query("Select p From Post p")
    List<Post> findAllPostsWithComments(Pageable pageable);

    Optional<List<Post>> findAllByUserId(Integer userId);

    List<Post> findAllByBodyContaining(String keywordInBody);
}

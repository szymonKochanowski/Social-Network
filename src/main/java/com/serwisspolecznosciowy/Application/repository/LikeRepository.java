package com.serwisspolecznosciowy.Application.repository;

import com.serwisspolecznosciowy.Application.entity.Like;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LikeRepository extends JpaRepository<Like, Integer> {

    List<Like> findByPostLikeId(Integer postId);

}

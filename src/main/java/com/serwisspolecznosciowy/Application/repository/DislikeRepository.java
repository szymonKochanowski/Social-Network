package com.serwisspolecznosciowy.Application.repository;

import com.serwisspolecznosciowy.Application.entity.Dislike;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DislikeRepository extends JpaRepository<Dislike, Integer> {

    List<Dislike> findByPostDislikeId(Integer postId);

}

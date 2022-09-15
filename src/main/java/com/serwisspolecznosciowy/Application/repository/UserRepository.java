package com.serwisspolecznosciowy.Application.repository;

import com.serwisspolecznosciowy.Application.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {

    Optional<User> findByUsername(String username);

    @Query(value = "SELECT * FROM `portal-spolecznosciowy`.`users` where username like %:keyword%", nativeQuery = true)
    List<User> findByUsernameLike(@Param(value = "keyword") String keyword);

    @Query(value = "select u.* from `portal-spolecznosciowy`.`users` u left join `portal-spolecznosciowy`.`posts` p on p.user_id = u.id where p.id = :postId", nativeQuery = true)
    Optional<User> findByPostId(@Param(value = "postId") Integer postId);

}

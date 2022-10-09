package com.serwisspolecznosciowy.Application.testData;

import com.serwisspolecznosciowy.Application.dto.*;
import com.serwisspolecznosciowy.Application.entity.*;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Component
public class TestData {

    public Post preparedPost() {
        Post post = new Post();
        post.setId(1);
        post.setBody("test post body");
        post.setCreated(LocalDateTime.now());
        post.setUpdated(null);
        post.setUser(preparedUser());
        post.setCommentList(null);//set as null to avoid StackOverflowError
        post.setLikeList(Collections.emptyList());
        post.setDislikeList(Collections.emptyList());
        return post;
    }

    public PostDto preparedPostDto() {
        Post post = preparedPost();
        User user = preparedUser();
        PostDto postDto = new PostDto();
        postDto.setBody(post.getBody());
        postDto.setCreated(post.getCreated());
        postDto.setUpdated(post.getUpdated());
        postDto.setLikeDtoList(Collections.emptyList());
        postDto.setDislikeDtoList(Collections.emptyList());
        postDto.setUsername(user.getUsername());
        postDto.setProfilePicture(null);
        return postDto;
    }

    public PostBodyDto preparedPostBodyDto() {
        Post post = preparedPost();
        PostBodyDto postBodyDto = new PostBodyDto(post.getBody());
        return postBodyDto;
    }

    public List<Post> preparedPostsList() {
        Post post = preparedPost();
        List<Post> postList = new ArrayList<>();
        postList.add(post);
        return postList;
    }

    public List<PostDto> preparedPostDtoWithAuthorList() {
        PostDto postDto = preparedPostDto();
        List<PostDto> postDtoList = new ArrayList<>();
        postDtoList.add(postDto);
        return postDtoList;
    }

    public User preparedUser() {
        User user = new User();
        user.setId(1);
        user.setUsername("test12!A");
        user.setPassword("test12!A");
        user.setCreated(LocalDateTime.now());
        user.setUpdated(null);
        user.setRole("ROLE_USER");
        user.setEnabled(true);
        user.setProfilePicture(null);
        return user;
    }

    public UserDto preparedUserDto() {
        User user = preparedUser();
        UserDto userDto = new UserDto();
        userDto.setUsername(user.getUsername());
        userDto.setProfilePicture(user.getProfilePicture());
        userDto.setCreated(user.getCreated());
        userDto.setUpdated(user.getUpdated());
        return userDto;
    }

    public User preparedAdmin() {
        User admin = new User();
        admin.setId(1);
        admin.setUsername("admin12!A");
        admin.setPassword("admin12!A");
        admin.setCreated(LocalDateTime.now());
        admin.setUpdated(null);
        admin.setRole("ROLE_ADMIN");
        admin.setEnabled(true);
        admin.setProfilePicture(null);
        return admin;
    }

    public UserDto preparedAdminDto() {
        User admin = preparedAdmin();
        UserDto adminDto = new UserDto();
        adminDto.setUsername(admin.getUsername());
        adminDto.setProfilePicture(admin.getProfilePicture());
        adminDto.setCreated(admin.getCreated());
        adminDto.setUpdated(admin.getUpdated());
        return adminDto;
    }

    public NewUserDto preparedNewUserDto() {
        User user = preparedUser();
        NewUserDto newUserDto = new NewUserDto(user.getUsername(), user.getPassword(), user.getProfilePicture());
        return newUserDto;
    }

    public NewUserDto preparedNewAdminDto() {
        User admin = preparedAdmin();
        NewUserDto newAdminDto = new NewUserDto(admin.getUsername(), admin.getPassword(), admin.getProfilePicture());
        return newAdminDto;
    }

    public List<User> preparedUsersList() {
        List<User> userList = new ArrayList<>();
        userList.add(preparedUser());
        userList.add(preparedAdmin());
        return userList;
    }

    public List<UserDto> preparedUsersDtoList() {
        List<UserDto> userDtoList = new ArrayList<>();
        userDtoList.add(preparedUserDto());
        return userDtoList;
    }

    public Comment preparedComment() {
        Comment comment = new Comment();
        comment.setId(1);
        comment.setBody("test comment body");
        comment.setCreated(LocalDateTime.now());
        comment.setUpdated(null);
        comment.setPostId(preparedPost().getId());
        comment.setUser(preparedUser());
        comment.setLikeList(Collections.emptyList());
        comment.setDislikeList(Collections.emptyList());
        return comment;
    }

    public List<Comment> preparedCommentList() {
        List<Comment> commentList = new ArrayList<>();
        commentList.add(preparedComment());
        return commentList;
    }

    public CommentBodyDto prepareCommentBodyDto() {
        Comment comment = preparedComment();
        String body = comment.getBody();
        CommentBodyDto commentBodyDto = new CommentBodyDto();
        commentBodyDto.setBody(body);
        return  commentBodyDto;
    }

    public CommentDto preparedCommentDto() {
        Comment comment = preparedComment();
        CommentDto commentDto = new CommentDto();
        commentDto.setBody(comment.getBody());
        commentDto.setCreated(comment.getCreated());
        commentDto.setUpdated(comment.getUpdated());
        commentDto.setLikeDtoList(Collections.emptyList());
        commentDto.setDislikeDtoList(Collections.emptyList());
        commentDto.setUser(preparedUserDto());
        return commentDto;
    }

    public List<CommentDto> preparedCommentDtoList() {
        List<CommentDto> commentDtoList = new ArrayList<>();
        commentDtoList.add(preparedCommentDto());
        return commentDtoList;
    }

    public PostBodyDto preparedEditPostDto() {
        PostBodyDto postBodyDto = new PostBodyDto();
        postBodyDto.setBody(preparedPost().getBody());
        return postBodyDto;
    }

    public Like preparedLike(){
        Like like = new Like();
        like.setId(1);
        like.setUserId(preparedAdmin().getId());
        like.setPostLikeId(preparedPost().getId());
        like.setCommentLikeId(null);
        like.setUsername(preparedAdmin().getUsername());
        return like;
    }

    public List<Like> preparedLikeList() {
        List<Like> likeList = new ArrayList<>();
        likeList.add(preparedLike());
        return likeList;
    }

    public Dislike preparedDislike() {
        Dislike dislike = new Dislike();
        dislike.setId(1);
        dislike.setPostDislikeId(preparedPost().getId());
        dislike.setCommentDislikeId(null);
        dislike.setUserId(preparedAdmin().getId());
        return dislike;
    }

    public List<Dislike> preparedDislikeList() {
        List<Dislike> dislikeList = new ArrayList<>();
        dislikeList.add(preparedDislike());
        return dislikeList;
    }

    public LikeDto preparedLikeDto() {
        LikeDto likeDto = new LikeDto(preparedAdmin().getUsername());
        return likeDto;
    }

    public List<LikeDto> preparedLikeDtoList() {
        List<LikeDto> likeDtoList = new ArrayList<>();
        likeDtoList.add(preparedLikeDto());
        return likeDtoList;
    }

    public DislikeDto preparedDislikeDto() {
        DislikeDto dislikeDto = new DislikeDto(preparedAdmin().getUsername());
        return dislikeDto;
    }

    public List<DislikeDto> preparedDislikeDtoList() {
        List<DislikeDto> dislikeDtoList = new ArrayList<>();
        dislikeDtoList.add(preparedDislikeDto());
        return dislikeDtoList;
    }

}

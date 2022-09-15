package com.serwisspolecznosciowy.Application.testData;

import com.serwisspolecznosciowy.Application.dto.*;
import com.serwisspolecznosciowy.Application.entity.Comment;
import com.serwisspolecznosciowy.Application.entity.Post;
import com.serwisspolecznosciowy.Application.entity.User;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
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
        post.setNumberOfLikes(0);
        post.setNumberOfDislikes(0);
        return post;
    }

    public PostDtoWithAuthor preparedPostDtoWithAuthor() {
        Post post = preparedPost();
        User user = preparedUser();
        PostDtoWithAuthor postDtoWithAuthor = new PostDtoWithAuthor();
        postDtoWithAuthor.setBody(post.getBody());
        postDtoWithAuthor.setCreated(post.getCreated());
        postDtoWithAuthor.setUpdated(post.getUpdated());
        postDtoWithAuthor.setNumberOfLikes(post.getNumberOfLikes());
        postDtoWithAuthor.setNumberOfDislikes(post.getNumberOfDislikes());
        postDtoWithAuthor.setUsername(user.getUsername());
        postDtoWithAuthor.setProfilePicture(null);
        return postDtoWithAuthor;
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

    public List<PostDtoWithAuthor> preparedPostDtoWithAuthorList() {
        PostDtoWithAuthor postDtoWithAuthor = preparedPostDtoWithAuthor();
        List<PostDtoWithAuthor> postDtoWithAuthorList = new ArrayList<>();
        postDtoWithAuthorList.add(postDtoWithAuthor);
        return postDtoWithAuthorList;
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
        comment.setNumberOfLikes(0);
        comment.setNumberOfDislikes(0);
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

    public CommentDtoWithAuthor preparedCommentDtoWithAuthor() {
        Comment comment = preparedComment();
        CommentDtoWithAuthor commentDtoWithAuthor = new CommentDtoWithAuthor();
        commentDtoWithAuthor.setBody(comment.getBody());
        commentDtoWithAuthor.setCreated(comment.getCreated());
        commentDtoWithAuthor.setUpdated(comment.getUpdated());
        commentDtoWithAuthor.setNumberOfLikes(comment.getNumberOfLikes());
        commentDtoWithAuthor.setNumberOfDislikes(comment.getNumberOfDislikes());
        commentDtoWithAuthor.setUser(preparedUserDto());
        return commentDtoWithAuthor;
    }

    public List<CommentDtoWithAuthor> preparedCommentDtoWithAuthorList() {
        List<CommentDtoWithAuthor> commentDtoWithAuthorList = new ArrayList<>();
        commentDtoWithAuthorList.add(preparedCommentDtoWithAuthor());
        return commentDtoWithAuthorList;
    }

    public PostBodyDto preparedEditPostDto() {
        PostBodyDto postBodyDto = new PostBodyDto();
        postBodyDto.setBody("test post body");
        return postBodyDto;
    }

}

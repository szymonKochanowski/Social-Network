package com.serwisspolecznosciowy.Application.mappers;

import com.serwisspolecznosciowy.Application.dto.PostDtoWithAuthor;
import com.serwisspolecznosciowy.Application.entity.Post;
import com.serwisspolecznosciowy.Application.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.ReportingPolicy;

import java.time.LocalDateTime;
import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface PostMapper {

    @Mappings({
        @Mapping(target = "username", expression = "java(getUsername(user))"),
        @Mapping(target = "profilePicture", expression = "java(getProfilePicture(user))"),
        @Mapping(target = "created", expression = "java(getPostCreated(post))"),
        @Mapping(target = "updated", expression = "java(getPostUpdated(post))")
    })
    PostDtoWithAuthor postToPostDtoWithAuthor(Post post, User user);

    List<PostDtoWithAuthor> postListToPostDtoWithAuthorList(List<Post> postList);

    default String getUsername(User user) {
        return user.getUsername();
    }

    default String getProfilePicture(User user) {
        return user.getProfilePicture();
    }

    default LocalDateTime getPostCreated(Post post) {
        return post.getCreated();
    }

    default LocalDateTime getPostUpdated(Post post) {
        return post.getUpdated();
    }

}

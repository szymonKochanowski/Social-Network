package com.serwisspolecznosciowy.Application.mappers;

import com.serwisspolecznosciowy.Application.dto.CommentDto;
import com.serwisspolecznosciowy.Application.dto.DislikeDto;
import com.serwisspolecznosciowy.Application.dto.LikeDto;
import com.serwisspolecznosciowy.Application.entity.Comment;
import com.serwisspolecznosciowy.Application.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

import java.time.LocalDateTime;
import java.util.List;

@Mapper(componentModel = "spring")
public interface CommentMapper {

    @Mappings({
            @Mapping(target = "created", expression = "java(getCommentCreated(comment))"),
            @Mapping(target = "updated", expression = "java(getCommentUpdated(comment))"),
            @Mapping(target = "likeDtoList", expression = "java(getLikeDtoList(likeDtoList))"),
            @Mapping(target = "dislikeDtoList", expression = "java(getDislikeDtoList(dislikeDtoList))")
    })
    CommentDto commentToCommentDto(Comment comment, User user, List<LikeDto> likeDtoList, List<DislikeDto> dislikeDtoList);

    List<CommentDto> commentListToCommentDtoList(List<Comment> commentList);

    default LocalDateTime getCommentCreated(Comment comment) {
        return comment.getCreated();
    }

    default LocalDateTime getCommentUpdated(Comment comment) {
        return comment.getUpdated();
    }

    default List<LikeDto> getLikeDtoList(List<LikeDto> likeDtoList) {
        return likeDtoList;
    }

    default List<DislikeDto> getDislikeDtoList(List<DislikeDto> dislikeDtoList) {
        return dislikeDtoList;
    }

}

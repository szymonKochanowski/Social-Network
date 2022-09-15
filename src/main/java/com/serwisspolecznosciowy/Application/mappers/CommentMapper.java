package com.serwisspolecznosciowy.Application.mappers;

import com.serwisspolecznosciowy.Application.dto.CommentDtoWithAuthor;
import com.serwisspolecznosciowy.Application.entity.Comment;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface CommentMapper {

    CommentDtoWithAuthor commentToCommentDtoWithAuthor(Comment comment);

    List<CommentDtoWithAuthor> commentListToCommentDtoList(List<Comment> commentList);
}

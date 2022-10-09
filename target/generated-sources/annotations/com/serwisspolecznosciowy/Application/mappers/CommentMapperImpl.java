package com.serwisspolecznosciowy.Application.mappers;

import com.serwisspolecznosciowy.Application.dto.CommentDto;
import com.serwisspolecznosciowy.Application.dto.DislikeDto;
import com.serwisspolecznosciowy.Application.dto.LikeDto;
import com.serwisspolecznosciowy.Application.dto.UserDto;
import com.serwisspolecznosciowy.Application.entity.Comment;
import com.serwisspolecznosciowy.Application.entity.User;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2022-10-09T17:05:00+0200",
    comments = "version: 1.5.2.Final, compiler: javac, environment: Java 11.0.12 (Eclipse Foundation)"
)
@Component
public class CommentMapperImpl implements CommentMapper {

    @Override
    public CommentDto commentToCommentDto(Comment comment, User user, List<LikeDto> likeDtoList, List<DislikeDto> dislikeDtoList) {
        if ( comment == null && user == null && likeDtoList == null && dislikeDtoList == null ) {
            return null;
        }

        CommentDto commentDto = new CommentDto();

        if ( comment != null ) {
            commentDto.setBody( comment.getBody() );
            commentDto.setUser( userToUserDto( comment.getUser() ) );
        }
        commentDto.setCreated( getCommentCreated(comment) );
        commentDto.setUpdated( getCommentUpdated(comment) );
        commentDto.setLikeDtoList( getLikeDtoList(likeDtoList) );
        commentDto.setDislikeDtoList( getDislikeDtoList(dislikeDtoList) );

        return commentDto;
    }

    @Override
    public List<CommentDto> commentListToCommentDtoList(List<Comment> commentList) {
        if ( commentList == null ) {
            return null;
        }

        List<CommentDto> list = new ArrayList<CommentDto>( commentList.size() );
        for ( Comment comment : commentList ) {
            list.add( commentToCommentDto1( comment ) );
        }

        return list;
    }

    protected UserDto userToUserDto(User user) {
        if ( user == null ) {
            return null;
        }

        UserDto userDto = new UserDto();

        userDto.setUsername( user.getUsername() );
        userDto.setProfilePicture( user.getProfilePicture() );
        userDto.setCreated( user.getCreated() );
        userDto.setUpdated( user.getUpdated() );

        return userDto;
    }

    protected CommentDto commentToCommentDto1(Comment comment) {
        if ( comment == null ) {
            return null;
        }

        CommentDto commentDto = new CommentDto();

        commentDto.setBody( comment.getBody() );
        commentDto.setCreated( comment.getCreated() );
        commentDto.setUpdated( comment.getUpdated() );
        commentDto.setUser( userToUserDto( comment.getUser() ) );

        return commentDto;
    }
}

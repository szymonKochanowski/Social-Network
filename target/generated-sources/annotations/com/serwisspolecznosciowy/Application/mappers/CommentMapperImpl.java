package com.serwisspolecznosciowy.Application.mappers;

import com.serwisspolecznosciowy.Application.dto.CommentDto;
import com.serwisspolecznosciowy.Application.dto.UserDto;
import com.serwisspolecznosciowy.Application.entity.Comment;
import com.serwisspolecznosciowy.Application.entity.Dislike;
import com.serwisspolecznosciowy.Application.entity.Like;
import com.serwisspolecznosciowy.Application.entity.User;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2022-10-09T13:01:06+0200",
    comments = "version: 1.5.2.Final, compiler: javac, environment: Java 11.0.12 (Eclipse Foundation)"
)
@Component
public class CommentMapperImpl implements CommentMapper {

    @Override
    public CommentDto commentToCommentDto(Comment comment) {
        if ( comment == null ) {
            return null;
        }

        CommentDto commentDto = new CommentDto();

        commentDto.setBody( comment.getBody() );
        commentDto.setCreated( comment.getCreated() );
        commentDto.setUpdated( comment.getUpdated() );
        List<Like> list = comment.getLikeList();
        if ( list != null ) {
            commentDto.setLikeList( new ArrayList<Like>( list ) );
        }
        List<Dislike> list1 = comment.getDislikeList();
        if ( list1 != null ) {
            commentDto.setDislikeList( new ArrayList<Dislike>( list1 ) );
        }
        commentDto.setUser( userToUserDto( comment.getUser() ) );

        return commentDto;
    }

    @Override
    public List<CommentDto> commentListToCommentDtoList(List<Comment> commentList) {
        if ( commentList == null ) {
            return null;
        }

        List<CommentDto> list = new ArrayList<CommentDto>( commentList.size() );
        for ( Comment comment : commentList ) {
            list.add( commentToCommentDto( comment ) );
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
}

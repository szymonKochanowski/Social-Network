package com.serwisspolecznosciowy.Application.mappers;

import com.serwisspolecznosciowy.Application.dto.CommentDtoWithAuthor;
import com.serwisspolecznosciowy.Application.dto.UserDto;
import com.serwisspolecznosciowy.Application.entity.Comment;
import com.serwisspolecznosciowy.Application.entity.User;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2022-09-15T18:54:15+0200",
    comments = "version: 1.5.2.Final, compiler: javac, environment: Java 11.0.12 (Eclipse Foundation)"
)
@Component
public class CommentMapperImpl implements CommentMapper {

    @Override
    public CommentDtoWithAuthor commentToCommentDtoWithAuthor(Comment comment) {
        if ( comment == null ) {
            return null;
        }

        CommentDtoWithAuthor commentDtoWithAuthor = new CommentDtoWithAuthor();

        commentDtoWithAuthor.setBody( comment.getBody() );
        commentDtoWithAuthor.setCreated( comment.getCreated() );
        commentDtoWithAuthor.setUpdated( comment.getUpdated() );
        commentDtoWithAuthor.setNumberOfLikes( comment.getNumberOfLikes() );
        commentDtoWithAuthor.setNumberOfDislikes( comment.getNumberOfDislikes() );
        commentDtoWithAuthor.setUser( userToUserDto( comment.getUser() ) );

        return commentDtoWithAuthor;
    }

    @Override
    public List<CommentDtoWithAuthor> commentListToCommentDtoList(List<Comment> commentList) {
        if ( commentList == null ) {
            return null;
        }

        List<CommentDtoWithAuthor> list = new ArrayList<CommentDtoWithAuthor>( commentList.size() );
        for ( Comment comment : commentList ) {
            list.add( commentToCommentDtoWithAuthor( comment ) );
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

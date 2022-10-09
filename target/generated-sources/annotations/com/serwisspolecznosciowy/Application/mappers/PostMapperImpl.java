package com.serwisspolecznosciowy.Application.mappers;

import com.serwisspolecznosciowy.Application.dto.DislikeDto;
import com.serwisspolecznosciowy.Application.dto.LikeDto;
import com.serwisspolecznosciowy.Application.dto.PostDto;
import com.serwisspolecznosciowy.Application.entity.Post;
import com.serwisspolecznosciowy.Application.entity.User;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2022-10-09T13:01:05+0200",
    comments = "version: 1.5.2.Final, compiler: javac, environment: Java 11.0.12 (Eclipse Foundation)"
)
@Component
public class PostMapperImpl implements PostMapper {

    @Override
    public PostDto postToPostDto(Post post, User user, List<LikeDto> likeDtoList, List<DislikeDto> dislikeDtoList) {
        if ( post == null && user == null && likeDtoList == null && dislikeDtoList == null ) {
            return null;
        }

        PostDto postDto = new PostDto();

        if ( post != null ) {
            postDto.setBody( post.getBody() );
            postDto.setNumberOfComments( post.getNumberOfComments() );
        }
        postDto.setUsername( getUsername(user) );
        postDto.setProfilePicture( getProfilePicture(user) );
        postDto.setCreated( getPostCreated(post) );
        postDto.setUpdated( getPostUpdated(post) );
        postDto.setLikeDtoList( getLikeDtoList(likeDtoList) );
        postDto.setDislikeDtoList( getDislikeDtoList(dislikeDtoList) );

        return postDto;
    }

    @Override
    public List<PostDto> postListToPostDtoList(List<Post> postList) {
        if ( postList == null ) {
            return null;
        }

        List<PostDto> list = new ArrayList<PostDto>( postList.size() );
        for ( Post post : postList ) {
            list.add( postToPostDto1( post ) );
        }

        return list;
    }

    protected PostDto postToPostDto1(Post post) {
        if ( post == null ) {
            return null;
        }

        PostDto postDto = new PostDto();

        postDto.setBody( post.getBody() );
        postDto.setCreated( post.getCreated() );
        postDto.setUpdated( post.getUpdated() );
        postDto.setNumberOfComments( post.getNumberOfComments() );

        return postDto;
    }
}

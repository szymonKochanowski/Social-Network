package com.serwisspolecznosciowy.Application.mappers;

import com.serwisspolecznosciowy.Application.dto.PostDtoWithAuthor;
import com.serwisspolecznosciowy.Application.entity.Dislike;
import com.serwisspolecznosciowy.Application.entity.Like;
import com.serwisspolecznosciowy.Application.entity.Post;
import com.serwisspolecznosciowy.Application.entity.User;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2022-09-23T15:11:09+0200",
    comments = "version: 1.5.2.Final, compiler: javac, environment: Java 11.0.12 (Eclipse Foundation)"
)
@Component
public class PostMapperImpl implements PostMapper {

    @Override
    public PostDtoWithAuthor postToPostDtoWithAuthor(Post post, User user) {
        if ( post == null && user == null ) {
            return null;
        }

        PostDtoWithAuthor postDtoWithAuthor = new PostDtoWithAuthor();

        if ( post != null ) {
            postDtoWithAuthor.setBody( post.getBody() );
            List<Like> list = post.getLikeList();
            if ( list != null ) {
                postDtoWithAuthor.setLikeList( new ArrayList<Like>( list ) );
            }
            List<Dislike> list1 = post.getDislikeList();
            if ( list1 != null ) {
                postDtoWithAuthor.setDislikeList( new ArrayList<Dislike>( list1 ) );
            }
            postDtoWithAuthor.setNumberOfComments( post.getNumberOfComments() );
        }
        postDtoWithAuthor.setUsername( getUsername(user) );
        postDtoWithAuthor.setProfilePicture( getProfilePicture(user) );
        postDtoWithAuthor.setCreated( getPostCreated(post) );
        postDtoWithAuthor.setUpdated( getPostUpdated(post) );

        return postDtoWithAuthor;
    }

    @Override
    public List<PostDtoWithAuthor> postListToPostDtoWithAuthorList(List<Post> postList) {
        if ( postList == null ) {
            return null;
        }

        List<PostDtoWithAuthor> list = new ArrayList<PostDtoWithAuthor>( postList.size() );
        for ( Post post : postList ) {
            list.add( postToPostDtoWithAuthor1( post ) );
        }

        return list;
    }

    protected PostDtoWithAuthor postToPostDtoWithAuthor1(Post post) {
        if ( post == null ) {
            return null;
        }

        PostDtoWithAuthor postDtoWithAuthor = new PostDtoWithAuthor();

        postDtoWithAuthor.setBody( post.getBody() );
        postDtoWithAuthor.setCreated( post.getCreated() );
        postDtoWithAuthor.setUpdated( post.getUpdated() );
        List<Like> list = post.getLikeList();
        if ( list != null ) {
            postDtoWithAuthor.setLikeList( new ArrayList<Like>( list ) );
        }
        List<Dislike> list1 = post.getDislikeList();
        if ( list1 != null ) {
            postDtoWithAuthor.setDislikeList( new ArrayList<Dislike>( list1 ) );
        }
        postDtoWithAuthor.setNumberOfComments( post.getNumberOfComments() );

        return postDtoWithAuthor;
    }
}

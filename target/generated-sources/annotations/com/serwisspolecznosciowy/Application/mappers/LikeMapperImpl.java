package com.serwisspolecznosciowy.Application.mappers;

import com.serwisspolecznosciowy.Application.dto.LikeDto;
import com.serwisspolecznosciowy.Application.entity.Like;
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
public class LikeMapperImpl implements LikeMapper {

    @Override
    public List<LikeDto> likeListToLikeDtoList(List<Like> likeList) {
        if ( likeList == null ) {
            return null;
        }

        List<LikeDto> list = new ArrayList<LikeDto>( likeList.size() );
        for ( Like like : likeList ) {
            list.add( likeToLikeDto( like ) );
        }

        return list;
    }

    protected LikeDto likeToLikeDto(Like like) {
        if ( like == null ) {
            return null;
        }

        LikeDto likeDto = new LikeDto();

        likeDto.setUsername( like.getUsername() );

        return likeDto;
    }
}

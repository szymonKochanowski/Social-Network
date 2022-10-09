package com.serwisspolecznosciowy.Application.mappers;

import com.serwisspolecznosciowy.Application.dto.DislikeDto;
import com.serwisspolecznosciowy.Application.entity.Dislike;
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
public class DislikeMapperImpl implements DislikeMapper {

    @Override
    public List<DislikeDto> dislikeListToDislikeDtoList(List<Dislike> dislikeList) {
        if ( dislikeList == null ) {
            return null;
        }

        List<DislikeDto> list = new ArrayList<DislikeDto>( dislikeList.size() );
        for ( Dislike dislike : dislikeList ) {
            list.add( dislikeToDislikeDto( dislike ) );
        }

        return list;
    }

    protected DislikeDto dislikeToDislikeDto(Dislike dislike) {
        if ( dislike == null ) {
            return null;
        }

        DislikeDto dislikeDto = new DislikeDto();

        dislikeDto.setUsername( dislike.getUsername() );

        return dislikeDto;
    }
}

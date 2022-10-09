package com.serwisspolecznosciowy.Application.service;

import com.serwisspolecznosciowy.Application.dto.CommentBodyDto;
import com.serwisspolecznosciowy.Application.dto.CommentDto;
import com.serwisspolecznosciowy.Application.dto.DislikeDto;
import com.serwisspolecznosciowy.Application.dto.LikeDto;
import com.serwisspolecznosciowy.Application.entity.*;
import com.serwisspolecznosciowy.Application.exception.CommentEmptyBodyException;
import com.serwisspolecznosciowy.Application.exception.CommentNotFoundException;
import com.serwisspolecznosciowy.Application.exception.PostNotFoundException;
import com.serwisspolecznosciowy.Application.exception.UserForbiddenAccessException;
import com.serwisspolecznosciowy.Application.mappers.CommentMapper;
import com.serwisspolecznosciowy.Application.mappers.DislikeMapper;
import com.serwisspolecznosciowy.Application.mappers.LikeMapper;
import com.serwisspolecznosciowy.Application.repository.CommentRepository;
import com.serwisspolecznosciowy.Application.repository.DislikeRepository;
import com.serwisspolecznosciowy.Application.repository.LikeRepository;
import com.serwisspolecznosciowy.Application.testData.TestData;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
@SpringBootTest
public class CommentServiceTest {

    @Autowired
    TestData testData;
    @InjectMocks
    private CommentService commentService;
    @Mock
    private CommentRepository commentRepository;
    @Mock
    private PostService postService;
    @Mock
    private UserService userService;
    @Mock
    private CommentMapper commentMapper;
    @Mock
    private LikeMapper likeMapper;
    @Mock
    private DislikeMapper dislikeMapper;
    @Mock
    private DislikeRepository dislikeRepository;
    @Mock
    private LikeRepository likeRepository;

    @Test
    void addNewComment() throws PostNotFoundException {
        //given
        User user = testData.preparedUser();
        when(userService.getLoginUser()).thenReturn(user);

        Comment comment = testData.preparedComment();
        CommentDto expectedCommentDto = testData.preparedCommentDto();
        CommentBodyDto commentBodyDto = testData.prepareCommentBodyDto();
        when(commentRepository.save(any(Comment.class))).thenReturn(comment);

        Post post = testData.preparedPost();
        Integer postId = post.getId();
        post.setNumberOfComments(1);
        when(postService.findPostById(anyInt())).thenReturn(post);

        when(commentMapper.commentToCommentDto(any(Comment.class), any(User.class), any(), any())).thenReturn(expectedCommentDto);
        //when
        CommentDto actualCommentDto = commentService.addNewComment(postId, commentBodyDto);
        //then
        assertEquals(expectedCommentDto.getBody(), actualCommentDto.getBody());
        assertEquals(expectedCommentDto.getUser().getUsername(), actualCommentDto.getUser().getUsername());
    }

    @Test
    void addNewCommentWithCommentEmptyBodyException() throws PostNotFoundException {
        //given
        User user = testData.preparedUser();
        when(userService.getLoginUser()).thenReturn(user);

        CommentBodyDto commentBodyDto = testData.prepareCommentBodyDto();
        commentBodyDto.setBody("");

        Post post = testData.preparedPost();
        Integer postId = post.getId();
        //when
        //then
        assertThrows(CommentEmptyBodyException.class, () -> commentService.addNewComment(postId, commentBodyDto));
    }

    @Test
    void getAllComments() {
        //given
        Integer pageNumber = 0;
        Integer pageSize = 10;
        Sort.Direction wayOfSort = Sort.Direction.ASC;
        PageRequest pageRequest = PageRequest.of(pageNumber, pageSize, Sort.by(wayOfSort, "created"));
        List<Comment> expectedCommentList = testData.preparedCommentList();
        when(commentRepository.findAllComments(pageRequest)).thenReturn(expectedCommentList);
        //when
        List<Comment> actualCommentsList = commentService.getAllComments(pageNumber, pageSize, wayOfSort);
        //then
        assertEquals(expectedCommentList, actualCommentsList);
    }

    @Test
    void getAllCommentsDto() {
        //given
        Integer pageNumber = 0;
        Integer pageSize = 10;
        Sort.Direction wayOfSort = Sort.Direction.ASC;
        List<Comment> commentList = testData.preparedCommentList();
        List<CommentDto> expectedCommentDtoList = testData.preparedCommentDtoList();
        CommentDto commentDto = testData.preparedCommentDto();

        User user = testData.preparedUser();

        when(commentRepository.findAllComments(PageRequest.of(pageNumber, pageSize, Sort.by(wayOfSort, "created")))).thenReturn(commentList);

        List<Like> likeList = testData.preparedLikeList();
        List<LikeDto> likeDtoList = testData.preparedLikeDtoList();
        List<Dislike> dislikeList = testData.preparedDislikeList();
        List<DislikeDto> dislikeDtoList = testData.preparedDislikeDtoList();
        when(likeMapper.likeListToLikeDtoList(likeList)).thenReturn(likeDtoList);
        when(dislikeMapper.dislikeListToDislikeDtoList(dislikeList)).thenReturn(dislikeDtoList);

        when(commentMapper.commentToCommentDto(any(Comment.class), any(User.class), any(), any())).thenReturn(commentDto);

        //when
        List<CommentDto> actualCommentDtoList = commentService.getAllCommentsDto(pageNumber, pageSize, wayOfSort);

        //then
        assertEquals(expectedCommentDtoList.size(), actualCommentDtoList.size());
        assertEquals(expectedCommentDtoList.get(0).getBody(), actualCommentDtoList.get(0).getBody());
        assertEquals(expectedCommentDtoList.get(0).getUser(), actualCommentDtoList.get(0).getUser());
    }

    @Test
    void getCommentById() throws CommentNotFoundException {
        //Given
        Comment expectedComment = testData.preparedComment();
        Integer commentId = expectedComment.getId();
        when(commentRepository.findById(commentId)).thenReturn(Optional.of(expectedComment));
        //When
        Comment actualComment = commentService.getCommentById(commentId);
        //Then
        assertEquals(expectedComment.getBody(), actualComment.getBody());
        assertEquals(expectedComment.getId(), actualComment.getId());
        assertEquals(expectedComment.getCreated(), actualComment.getCreated());
        assertEquals(expectedComment.getUser().getUsername(), actualComment.getUser().getUsername());
        assertEquals(expectedComment.getUser().getRole(), actualComment.getUser().getRole());
        assertEquals(expectedComment.getUser().getPassword(), actualComment.getUser().getPassword());
    }

    @Test
    void getCommentByIdWithCommentNotFoundException() throws CommentNotFoundException {
        //Given
        Integer incorrectCommentId = 999999999;
        //When
        CommentNotFoundException exception = assertThrows(CommentNotFoundException.class, () -> {
            commentService.getCommentById(incorrectCommentId);
        });
        //Then
        assertEquals("Comment with id: " + incorrectCommentId + " not found in our database!", exception.getMessage());
    }

    @Test
    void getCommentDtoById() throws CommentNotFoundException {
        //Given
        Comment comment = testData.preparedComment();
        Integer commentId = comment.getId();
        CommentDto expectedCommentDto = testData.preparedCommentDto();
        when(commentRepository.findById(commentId)).thenReturn(Optional.of(comment));

        List<Like> likeList = testData.preparedLikeList();
        List<LikeDto> likeDtoList = testData.preparedLikeDtoList();
        List<Dislike> dislikeList = testData.preparedDislikeList();
        List<DislikeDto> dislikeDtoList = testData.preparedDislikeDtoList();
        when(likeMapper.likeListToLikeDtoList(likeList)).thenReturn(likeDtoList);
        when(dislikeMapper.dislikeListToDislikeDtoList(dislikeList)).thenReturn(dislikeDtoList);

        when(commentMapper.commentToCommentDto(any(Comment.class), any(User.class), any(), any())).thenReturn(expectedCommentDto);

        //When
        CommentDto actualCommentDtoById = commentService.getCommentDtoById(commentId);

        //Then
        assertEquals(expectedCommentDto.getBody(), actualCommentDtoById.getBody());
        assertEquals(expectedCommentDto.getCreated(), actualCommentDtoById.getCreated());
        assertEquals(expectedCommentDto.getUser().getUsername(), actualCommentDtoById.getUser().getUsername());
        assertEquals(expectedCommentDto.getUser().getProfilePicture(), actualCommentDtoById.getUser().getProfilePicture());
    }

    @Test
    void getCommentDtoByIdWithCommentNotFoundException() throws CommentNotFoundException {
        //Given
        Integer incorrectCommentId = 999999999;
        //When
        CommentNotFoundException exception = assertThrows(CommentNotFoundException.class, () -> {
            commentService.getCommentDtoById(incorrectCommentId);
        });
        //Then
        assertEquals("Comment with id: '" + incorrectCommentId + "' not found in our database!", exception.getMessage());
    }

    @Test
    void deleteCommentById() throws UserForbiddenAccessException, CommentNotFoundException {
        //Given
        Comment comment = testData.preparedComment();
        Integer commentId = comment.getId();
        User user = testData.preparedUser();
        when(commentRepository.findById(commentId)).thenReturn(Optional.of(comment));
        when(userService.getLoginUser()).thenReturn(user);
        doNothing().when(commentRepository).deleteById(commentId);
        //When
        commentService.deleteCommentById(commentId);
        //Then
        verify(commentRepository, times(1)).deleteById(commentId);
    }

    @Test
    void deleteCommentByIdWithUserForbiddenAccessException() throws UserForbiddenAccessException, CommentNotFoundException {
        //Given
        Comment comment = testData.preparedComment();
        Integer commentId = comment.getId();
        when(commentRepository.findById(commentId)).thenReturn(Optional.of(comment));

        User incorrectUser = testData.preparedUser();
        incorrectUser.setId(9999);
        incorrectUser.setUsername("wrong");
        incorrectUser.setPassword("wrong");
        when(userService.getLoginUser()).thenReturn(incorrectUser);
        //When
        UserForbiddenAccessException exception = assertThrows(UserForbiddenAccessException.class, () -> {
            commentService.deleteCommentById(commentId);
        });
        //Then
        assertEquals("You are not authorized to delete this comment!", exception.getMessage());
    }

    @Test
    void findAllCommentByUserId() {
        //Given
        Integer userId = testData.preparedUser().getId();
        List<Comment> expectedCommentsList = testData.preparedCommentList();
        when(commentRepository.findAllCommentByUserId(userId)).thenReturn(Optional.ofNullable(expectedCommentsList));
        //When
        List<Comment> actualCommentsList = commentService.findAllCommentByUserId(userId);
        //Then
        assertEquals(expectedCommentsList.get(0).getBody(), actualCommentsList.get(0).getBody());
        assertEquals(expectedCommentsList.get(0).getCreated(), actualCommentsList.get(0).getCreated());
        assertEquals(expectedCommentsList.get(0).getDislikeList(), actualCommentsList.get(0).getDislikeList());
        assertEquals(expectedCommentsList.get(0).getUser(), actualCommentsList.get(0).getUser());
    }

    @Test
    void findAllCommentByUserIdRetrunNull() {
        //Given
        Integer userId = testData.preparedUser().getId();
        when(commentRepository.findAllCommentByUserId(userId)).thenReturn(Optional.ofNullable(null));
        //When
        List<Comment> actualCommentsList = commentService.findAllCommentByUserId(userId);
        //Then
        assertNull(actualCommentsList);
    }

    @Test
    void getCommentsByBody() throws CommentNotFoundException {
        //Given
        String body = testData.preparedComment().getBody();
        List<Comment> expectedCommentsList = testData.preparedCommentList();
        when(commentRepository.findAllByBodyContaining(body)).thenReturn(expectedCommentsList);
        //When
        List<Comment> actualCommentsList = commentService.getCommentsByBody(body);
        //Then
        assertEquals(expectedCommentsList.get(0).getBody().contains(body), actualCommentsList.get(0).getBody().contains(body));
        assertEquals(expectedCommentsList.get(0).getBody(), actualCommentsList.get(0).getBody());
        assertEquals(expectedCommentsList.get(0).getCreated(), actualCommentsList.get(0).getCreated());
        assertEquals(expectedCommentsList.get(0).getDislikeList(), actualCommentsList.get(0).getDislikeList());
        assertEquals(expectedCommentsList.get(0).getUser(), actualCommentsList.get(0).getUser());
    }

    @Test
    void getCommentsByBodyWithCommentNotFoundException() throws CommentNotFoundException {
        //Given
        String body = testData.preparedComment().getBody();
        List<Comment> expectedCommentsList = Collections.emptyList();
        when(commentRepository.findAllByBodyContaining(body)).thenReturn(expectedCommentsList);
        //When
        //Then
        assertThrows(CommentNotFoundException.class, () -> commentService.getCommentsByBody(body),
                "Not found any comment with keyword: '" + body  + "' in our database!");
    }

    @Test
    void getCommentsDtoByBody() throws CommentNotFoundException {
        //Given
        String body = testData.preparedComment().getBody();
        List<Comment> commentList = testData.preparedCommentList();
        List<CommentDto> expectedCommentDtoList = testData.preparedCommentDtoList();
        when(commentRepository.findAllByBodyContaining(body)).thenReturn(commentList);
        when(commentMapper.commentListToCommentDtoList(commentList)).thenReturn(expectedCommentDtoList);
        //When
        List<CommentDto> actualCommentDtoList = commentService.getCommentsDtoByBody(body);
        //Then
        assertEquals(expectedCommentDtoList.get(0).getBody().contains(body), actualCommentDtoList.get(0).getBody().contains(body));
        assertEquals(expectedCommentDtoList.get(0).getBody(), actualCommentDtoList.get(0).getBody());
        assertEquals(expectedCommentDtoList.get(0).getCreated(), actualCommentDtoList.get(0).getCreated());
        assertEquals(expectedCommentDtoList.get(0).getDislikeDtoList(), actualCommentDtoList.get(0).getDislikeDtoList());
        assertEquals(expectedCommentDtoList.get(0).getUser(), actualCommentDtoList.get(0).getUser());
    }

    @Test
    void getCommentsDtoByBodyWithCommentNotFoundException() throws CommentNotFoundException {
        //Given
        String body = testData.preparedComment().getBody();
        when(commentRepository.findAllByBodyContaining(body)).thenReturn(null);
        //When
        //Then
        assertThrows(CommentNotFoundException.class, () -> commentService.getCommentsDtoByBody(body),
                "Not found any comment with keyword: '" + body  + "' in our database!");
    }

    @Test
    void addOneLikeToComment() throws CommentNotFoundException {
        //Given
        User user = testData.preparedUser();
        when(userService.getLoginUser()).thenReturn(user);

        Comment comment = testData.preparedComment();
        Integer commentId = comment.getId();
        when(commentRepository.findById(commentId)).thenReturn(Optional.of(comment));

        Like like = testData.preparedLike();
        when(likeRepository.save(like)).thenReturn(like);

        List<Like> likeList = testData.preparedLikeList();
        List<LikeDto> likeDtoList = testData.preparedLikeDtoList();
        List<Dislike> dislikeList = testData.preparedDislikeList();
        List<DislikeDto> dislikeDtoList = testData.preparedDislikeDtoList();
        when(likeMapper.likeListToLikeDtoList(likeList)).thenReturn(likeDtoList);
        when(dislikeMapper.dislikeListToDislikeDtoList(dislikeList)).thenReturn(dislikeDtoList);

        when(commentRepository.save(comment)).thenReturn(comment);

        CommentDto expectedCommentDto = testData.preparedCommentDto();
        expectedCommentDto.setLikeDtoList(likeDtoList);

        when(commentMapper.commentToCommentDto(comment, user, likeDtoList, dislikeDtoList)).thenReturn(expectedCommentDto);

        //When
        CommentDto actualCommentDto = commentService.addOneLikeToComment(commentId);

        //Then
        assertEquals(expectedCommentDto.getLikeDtoList(), actualCommentDto.getLikeDtoList());
        assertEquals(expectedCommentDto.getDislikeDtoList(), actualCommentDto.getDislikeDtoList());
        assertEquals(expectedCommentDto.getBody(), actualCommentDto.getBody());
        assertEquals(expectedCommentDto.getUser(), actualCommentDto.getUser());
    }

    @Test
    void addOneLikeToCommentWithCommentNotFoundException() throws CommentNotFoundException {
        //Given
        Integer incorrectCommentId = 99999999;
        when(commentRepository.findById(incorrectCommentId)).thenReturn(Optional.ofNullable(null));
        //When
        //Then
        assertThrows(CommentNotFoundException.class, () -> commentService.addOneLikeToComment(incorrectCommentId),
                "Unable to add like to comment because not found comment with  id: '" + incorrectCommentId  + "' in our database!");
    }

    @Test
    void addOneDisLikeToComment() throws CommentNotFoundException {
        //Given
        User user = testData.preparedUser();
        when(userService.getLoginUser()).thenReturn(user);

        Comment comment = testData.preparedComment();
        Integer commentId = comment.getId();
        when(commentRepository.findById(commentId)).thenReturn(Optional.of(comment));

        Dislike dislike = testData.preparedDislike();
        when(dislikeRepository.save(dislike)).thenReturn(dislike);

        List<Like> likeList = testData.preparedLikeList();
        List<LikeDto> likeDtoList = testData.preparedLikeDtoList();
        List<Dislike> dislikeList = testData.preparedDislikeList();
        List<DislikeDto> dislikeDtoList = testData.preparedDislikeDtoList();
        when(likeMapper.likeListToLikeDtoList(likeList)).thenReturn(likeDtoList);
        when(dislikeMapper.dislikeListToDislikeDtoList(dislikeList)).thenReturn(dislikeDtoList);

        when(commentRepository.save(comment)).thenReturn(comment);

        CommentDto expectedCommentDto = testData.preparedCommentDto();
        expectedCommentDto.setDislikeDtoList(dislikeDtoList);

        when(commentMapper.commentToCommentDto(comment, user, likeDtoList, dislikeDtoList)).thenReturn(expectedCommentDto);

        //When
        CommentDto actualCommentDto = commentService.addOneDisLikeToComment(commentId);

        //Then
        assertEquals(expectedCommentDto.getDislikeDtoList(), actualCommentDto.getDislikeDtoList());
        assertEquals(expectedCommentDto.getLikeDtoList(), actualCommentDto.getLikeDtoList());
        assertEquals(expectedCommentDto.getBody(), actualCommentDto.getBody());
        assertEquals(expectedCommentDto.getUser(), actualCommentDto.getUser());
    }

    @Test
    void addOneDisLikeToCommentWithCommentNotFoundException() throws CommentNotFoundException {
        //Given
        Integer incorrectCommentId = 99999999;
        when(commentRepository.findById(incorrectCommentId)).thenReturn(Optional.ofNullable(null));
        //When
        //Then
        assertThrows(CommentNotFoundException.class, () -> commentService.addOneDisLikeToComment(incorrectCommentId),
                "Unable to add dislike to comment because not found comment with  id: '" + incorrectCommentId  + "' in our database!");
    }

    @Test
    void editComment() throws UserForbiddenAccessException, CommentNotFoundException {
        //Given
        Comment comment = testData.preparedComment();
        comment.setBody("test edit");
        Integer commentId = comment.getId();

        CommentDto expectedCommentDto = testData.preparedCommentDto();
        CommentBodyDto commentBodyDto = testData.prepareCommentBodyDto();

        List<Like> likeList = Collections.emptyList();
        List<LikeDto> likeDtoList = Collections.emptyList();
        List<Dislike> dislikeList = Collections.emptyList();
        List<DislikeDto> dislikeDtoList = Collections.emptyList();

        User user = testData.preparedUser();

        when(commentRepository.findById(commentId)).thenReturn(Optional.of(comment));
        when(commentRepository.save(comment)).thenReturn(comment);
        when(likeMapper.likeListToLikeDtoList(likeList)).thenReturn(likeDtoList);
        when(dislikeMapper.dislikeListToDislikeDtoList(dislikeList)).thenReturn(dislikeDtoList);
        when(commentMapper.commentToCommentDto(comment, user, likeDtoList, dislikeDtoList)).thenReturn(expectedCommentDto);
        //When
        CommentDto actualCommentDto = commentService.editComment(commentBodyDto, user, commentId);
        //Then
        assertEquals(expectedCommentDto.getDislikeDtoList(), actualCommentDto.getDislikeDtoList());
        assertEquals(expectedCommentDto.getLikeDtoList(), actualCommentDto.getLikeDtoList());
        assertEquals(expectedCommentDto.getBody(), actualCommentDto.getBody());
        assertEquals(expectedCommentDto.getUser(), actualCommentDto.getUser());
    }

    @Test
    void editCommentWithUserForbiddenAccessException() throws UserForbiddenAccessException, CommentNotFoundException {
        //Given
        Comment comment = testData.preparedComment();
        comment.setBody("test edit");
        comment.setUser(testData.preparedAdmin());
        Integer commentId = comment.getId();

        CommentBodyDto commentBodyDto = testData.prepareCommentBodyDto();

        User user = testData.preparedUser();

        when(commentRepository.findById(commentId)).thenReturn(Optional.of(comment));
        //When
        //Then
        assertThrows(UserForbiddenAccessException.class, () -> commentService.editComment(commentBodyDto, user, commentId),
                "Username: '" + user.getUsername() + "' have not permission to edit comment with id: '" + commentId + " !" );
    }

    @Test
    void getCommentsDtoListByPostId() throws PostNotFoundException {
        //Given
        Integer postId = testData.preparedPost().getId();
        List<Comment> commentList = testData.preparedCommentList();
        List<CommentDto> expectedCommentDtoList = testData.preparedCommentDtoList();
        when(commentRepository.findAllCommentsByPostId(postId)).thenReturn(commentList);
        when(commentMapper.commentListToCommentDtoList(commentList)).thenReturn(expectedCommentDtoList);
        //When
        List<CommentDto> actualCommentsDtoListByPostId = commentService.getCommentsDtoListByPostId(postId);
        //Then
        assertEquals(expectedCommentDtoList.get(0).getBody(), actualCommentsDtoListByPostId.get(0).getBody());
        assertEquals(expectedCommentDtoList.get(0).getCreated(), actualCommentsDtoListByPostId.get(0).getCreated());
        assertEquals(expectedCommentDtoList.get(0).getDislikeDtoList(), actualCommentsDtoListByPostId.get(0).getDislikeDtoList());
        assertEquals(expectedCommentDtoList.get(0).getUser(), actualCommentsDtoListByPostId.get(0).getUser());
    }

    @Test
    void getCommentsDtoListByPostIdWithPostNotFoundException() throws PostNotFoundException {
        //Given
        Integer postId = testData.preparedPost().getId();
        when(commentRepository.findAllCommentsByPostId(postId)).thenReturn(null);
        //When
        //Then
        assertThrows(PostNotFoundException.class, () ->  commentService.getCommentsDtoListByPostId(postId),
                "Not found any comments for post with id: " + postId + " !");
    }


}

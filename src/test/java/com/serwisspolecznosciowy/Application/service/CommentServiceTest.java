package com.serwisspolecznosciowy.Application.service;

import com.serwisspolecznosciowy.Application.dto.CommentBodyDto;
import com.serwisspolecznosciowy.Application.dto.CommentDtoWithAuthor;
import com.serwisspolecznosciowy.Application.entity.Comment;
import com.serwisspolecznosciowy.Application.entity.Post;
import com.serwisspolecznosciowy.Application.entity.User;
import com.serwisspolecznosciowy.Application.exception.CommentEmptyBodyException;
import com.serwisspolecznosciowy.Application.exception.CommentNotFoundException;
import com.serwisspolecznosciowy.Application.exception.PostNotFoundException;
import com.serwisspolecznosciowy.Application.exception.UserForbiddenAccessException;
import com.serwisspolecznosciowy.Application.mappers.CommentMapper;
import com.serwisspolecznosciowy.Application.repository.CommentRepository;
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

    @Test
    public void addNewComment() throws PostNotFoundException {
        //given
        User user = testData.preparedUser();
        when(userService.getLoginUser()).thenReturn(user);

        Comment comment = testData.preparedComment();
        CommentDtoWithAuthor expectedCommentDtoWithAuthor = testData.preparedCommentDtoWithAuthor();
        CommentBodyDto commentBodyDto = testData.prepareCommentBodyDto();
        when(commentRepository.save(any(Comment.class))).thenReturn(comment);

        Post post = testData.preparedPost();
        Integer postId = post.getId();
        post.setNumberOfComments(1);
        when(postService.findPostById(anyInt())).thenReturn(post);

        when(commentMapper.commentToCommentDtoWithAuthor(any(Comment.class))).thenReturn(expectedCommentDtoWithAuthor);
        //when
        CommentDtoWithAuthor actualCommentDtoWithAuthor = commentService.addNewComment(postId, commentBodyDto);
        //then
        assertEquals(expectedCommentDtoWithAuthor.getBody(), actualCommentDtoWithAuthor.getBody());
        assertEquals(expectedCommentDtoWithAuthor.getUser().getUsername(), actualCommentDtoWithAuthor.getUser().getUsername());
    }

    @Test
    public void addNewCommentWithCommentEmptyBodyException() throws PostNotFoundException {
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
    public void getAllComments() {
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
    public void getAllCommentsDto() {
        //given
        Integer pageNumber = 0;
        Integer pageSize = 10;
        Sort.Direction wayOfSort = Sort.Direction.ASC;
        List<CommentDtoWithAuthor> expectedCommentDtoWithAuthorList = testData.preparedCommentDtoWithAuthorList();
        when(commentMapper.commentListToCommentDtoList(anyList())).thenReturn(expectedCommentDtoWithAuthorList);
        //when
        List<CommentDtoWithAuthor> actualCommentDtoWithAuthorList = commentService.getAllCommentsDto(pageNumber, pageSize, wayOfSort);
        //then
        assertEquals(expectedCommentDtoWithAuthorList, actualCommentDtoWithAuthorList);
    }

    @Test
    public void getCommentById() throws CommentNotFoundException {
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
    public void getCommentByIdWithCommentNotFoundException() throws CommentNotFoundException {
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
    public void getCommentDtoById() throws CommentNotFoundException {
        //Given
        Comment comment = testData.preparedComment();
        Integer commentId = comment.getId();
        CommentDtoWithAuthor expectedCommentDtoWithAuthor = testData.preparedCommentDtoWithAuthor();
        when(commentRepository.findById(commentId)).thenReturn(Optional.of(comment));
        when(commentMapper.commentToCommentDtoWithAuthor(comment)).thenReturn(expectedCommentDtoWithAuthor);
        //When
        CommentDtoWithAuthor actualCommentDtoById = commentService.getCommentDtoById(commentId);
        //Then
        assertEquals(expectedCommentDtoWithAuthor.getBody(), actualCommentDtoById.getBody());
        assertEquals(expectedCommentDtoWithAuthor.getCreated(), actualCommentDtoById.getCreated());
        assertEquals(expectedCommentDtoWithAuthor.getUser().getUsername(), actualCommentDtoById.getUser().getUsername());
        assertEquals(expectedCommentDtoWithAuthor.getUser().getProfilePicture(), actualCommentDtoById.getUser().getProfilePicture());
    }

    @Test
    public void getCommentDtoByIdWithCommentNotFoundException() throws CommentNotFoundException {
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
    public void deleteCommentById() throws UserForbiddenAccessException, CommentNotFoundException {
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
    public void deleteCommentByIdWithUserForbiddenAccessException() throws UserForbiddenAccessException, CommentNotFoundException {
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
    public void findAllCommentByUserId() {
        //Given
        Integer userId = testData.preparedUser().getId();
        List<Comment> expectedCommentsList = testData.preparedCommentList();
        when(commentRepository.findAllCommentByUserId(userId)).thenReturn(Optional.ofNullable(expectedCommentsList));
        //When
        List<Comment> actualCommentsList = commentService.findAllCommentByUserId(userId);
        //Then
        assertEquals(expectedCommentsList.get(0).getBody(), actualCommentsList.get(0).getBody());
        assertEquals(expectedCommentsList.get(0).getCreated(), actualCommentsList.get(0).getCreated());
        assertEquals(expectedCommentsList.get(0).getNumberOfDislikes(), actualCommentsList.get(0).getNumberOfDislikes());
        assertEquals(expectedCommentsList.get(0).getUser(), actualCommentsList.get(0).getUser());
    }

    @Test
    public void findAllCommentByUserIdRetrunNull() {
        //Given
        Integer userId = testData.preparedUser().getId();
        when(commentRepository.findAllCommentByUserId(userId)).thenReturn(Optional.ofNullable(null));
        //When
        List<Comment> actualCommentsList = commentService.findAllCommentByUserId(userId);
        //Then
        assertNull(actualCommentsList);
    }

    @Test
    public void getCommentsByBody() throws CommentNotFoundException {
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
        assertEquals(expectedCommentsList.get(0).getNumberOfDislikes(), actualCommentsList.get(0).getNumberOfDislikes());
        assertEquals(expectedCommentsList.get(0).getUser(), actualCommentsList.get(0).getUser());
    }

    @Test
    public void getCommentsByBodyWithCommentNotFoundException() throws CommentNotFoundException {
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
    public void getCommentsDtoByBody() throws CommentNotFoundException {
        //Given
        String body = testData.preparedComment().getBody();
        List<Comment> commentList = testData.preparedCommentList();
        List<CommentDtoWithAuthor> expectedCommentDtoWithAuthorList = testData.preparedCommentDtoWithAuthorList();
        when(commentRepository.findAllByBodyContaining(body)).thenReturn(commentList);
        when(commentMapper.commentListToCommentDtoList(commentList)).thenReturn(expectedCommentDtoWithAuthorList);
        //When
        List<CommentDtoWithAuthor> actualCommentDtoWithAuthorList = commentService.getCommentsDtoByBody(body);
        //Then
        assertEquals(expectedCommentDtoWithAuthorList.get(0).getBody().contains(body), actualCommentDtoWithAuthorList.get(0).getBody().contains(body));
        assertEquals(expectedCommentDtoWithAuthorList.get(0).getBody(), actualCommentDtoWithAuthorList.get(0).getBody());
        assertEquals(expectedCommentDtoWithAuthorList.get(0).getCreated(), actualCommentDtoWithAuthorList.get(0).getCreated());
        assertEquals(expectedCommentDtoWithAuthorList.get(0).getNumberOfDislikes(), actualCommentDtoWithAuthorList.get(0).getNumberOfDislikes());
        assertEquals(expectedCommentDtoWithAuthorList.get(0).getUser(), actualCommentDtoWithAuthorList.get(0).getUser());
    }

    @Test
    public void getCommentsDtoByBodyWithCommentNotFoundException() throws CommentNotFoundException {
        //Given
        String body = testData.preparedComment().getBody();
        when(commentRepository.findAllByBodyContaining(body)).thenReturn(null);
        //When
        //Then
        assertThrows(CommentNotFoundException.class, () -> commentService.getCommentsDtoByBody(body),
                "Not found any comment with keyword: '" + body  + "' in our database!");
    }

    @Test
    public void addOneLikeToComment() throws CommentNotFoundException {
        //Given
        Comment comment = testData.preparedComment();
        comment.setNumberOfLikes(22);
        Integer commentId = comment.getId();
        when(commentRepository.findById(commentId)).thenReturn(Optional.of(comment));
        when(commentRepository.save(comment)).thenReturn(comment);
        CommentDtoWithAuthor expectedCommentDtoWithAuthor = testData.preparedCommentDtoWithAuthor();
        expectedCommentDtoWithAuthor.setNumberOfLikes(comment.getNumberOfLikes() + 1);
        when(commentMapper.commentToCommentDtoWithAuthor(comment)).thenReturn(expectedCommentDtoWithAuthor);
        //When
        CommentDtoWithAuthor actualCommentDtoWithAuthor = commentService.addOneLikeToComment(commentId);
        //Then
        assertEquals(expectedCommentDtoWithAuthor.getNumberOfLikes(), actualCommentDtoWithAuthor.getNumberOfLikes());
        assertEquals(expectedCommentDtoWithAuthor.getBody(), actualCommentDtoWithAuthor.getBody());
        assertEquals(expectedCommentDtoWithAuthor.getUser(), actualCommentDtoWithAuthor.getUser());
    }

    @Test
    public void addOneLikeToCommentWithCommentNotFoundException() throws CommentNotFoundException {
        //Given
        Integer incorrectCommentId = 99999999;
        when(commentRepository.findById(incorrectCommentId)).thenReturn(Optional.ofNullable(null));
        //When
        //Then
        assertThrows(CommentNotFoundException.class, () -> commentService.addOneLikeToComment(incorrectCommentId),
                "Unable to add like to comment because not found comment with  id: '" + incorrectCommentId  + "' in our database!");
    }

    @Test
    public void addOneDisLikeToComment() throws CommentNotFoundException {
        //Given
        Comment comment = testData.preparedComment();
        comment.setNumberOfDislikes(10);
        Integer commentId = comment.getId();
        when(commentRepository.findById(commentId)).thenReturn(Optional.of(comment));
        when(commentRepository.save(comment)).thenReturn(comment);
        CommentDtoWithAuthor expectedCommentDtoWithAuthor = testData.preparedCommentDtoWithAuthor();
        expectedCommentDtoWithAuthor.setNumberOfDislikes(comment.getNumberOfDislikes() + 1);
        when(commentMapper.commentToCommentDtoWithAuthor(comment)).thenReturn(expectedCommentDtoWithAuthor);
        //When
        CommentDtoWithAuthor actualCommentDtoWithAuthor = commentService.addOneDisLikeToComment(commentId);
        //Then
        assertEquals(expectedCommentDtoWithAuthor.getNumberOfDislikes(), actualCommentDtoWithAuthor.getNumberOfDislikes());
        assertEquals(expectedCommentDtoWithAuthor.getNumberOfLikes(), actualCommentDtoWithAuthor.getNumberOfLikes());
        assertEquals(expectedCommentDtoWithAuthor.getBody(), actualCommentDtoWithAuthor.getBody());
        assertEquals(expectedCommentDtoWithAuthor.getUser(), actualCommentDtoWithAuthor.getUser());
    }

    @Test
    public void addOneDisLikeToCommentWithCommentNotFoundException() throws CommentNotFoundException {
        //Given
        Integer incorrectCommentId = 99999999;
        when(commentRepository.findById(incorrectCommentId)).thenReturn(Optional.ofNullable(null));
        //When
        //Then
        assertThrows(CommentNotFoundException.class, () -> commentService.addOneDisLikeToComment(incorrectCommentId),
                "Unable to add dislike to comment because not found comment with  id: '" + incorrectCommentId  + "' in our database!");
    }

    @Test
    public void editComment() throws UserForbiddenAccessException, CommentNotFoundException {
        //Given
        Comment comment = testData.preparedComment();
        comment.setBody("test edit");
        Integer commentId = comment.getId();

        CommentDtoWithAuthor expectedCommentDtoWithAuthor = testData.preparedCommentDtoWithAuthor();
        CommentBodyDto commentBodyDto = testData.prepareCommentBodyDto();

        User user = testData.preparedUser();

        when(commentRepository.findById(commentId)).thenReturn(Optional.of(comment));
        when(commentRepository.save(comment)).thenReturn(comment);
        when(commentMapper.commentToCommentDtoWithAuthor(comment)).thenReturn(expectedCommentDtoWithAuthor);
        //When
        CommentDtoWithAuthor actualCommentDtoWithAuthor = commentService.editComment(commentBodyDto, user, commentId);
        //Then
        assertEquals(expectedCommentDtoWithAuthor.getNumberOfDislikes(), actualCommentDtoWithAuthor.getNumberOfDislikes());
        assertEquals(expectedCommentDtoWithAuthor.getNumberOfLikes(), actualCommentDtoWithAuthor.getNumberOfLikes());
        assertEquals(expectedCommentDtoWithAuthor.getBody(), actualCommentDtoWithAuthor.getBody());
        assertEquals(expectedCommentDtoWithAuthor.getUser(), actualCommentDtoWithAuthor.getUser());
    }

    @Test
    public void editCommentWithUserForbiddenAccessException() throws UserForbiddenAccessException, CommentNotFoundException {
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
    public void getCommentsDtoListByPostId() throws PostNotFoundException {
        //Given
        Integer postId = testData.preparedPost().getId();
        List<Comment> commentList = testData.preparedCommentList();
        List<CommentDtoWithAuthor> expectedCommentDtoWithAuthorList = testData.preparedCommentDtoWithAuthorList();
        when(commentRepository.findAllCommentsByPostId(postId)).thenReturn(commentList);
        when(commentMapper.commentListToCommentDtoList(commentList)).thenReturn(expectedCommentDtoWithAuthorList);
        //When
        List<CommentDtoWithAuthor> actualCommentsDtoListByPostId = commentService.getCommentsDtoListByPostId(postId);
        //Then
        assertEquals(expectedCommentDtoWithAuthorList.get(0).getBody(), actualCommentsDtoListByPostId.get(0).getBody());
        assertEquals(expectedCommentDtoWithAuthorList.get(0).getCreated(), actualCommentsDtoListByPostId.get(0).getCreated());
        assertEquals(expectedCommentDtoWithAuthorList.get(0).getNumberOfDislikes(), actualCommentsDtoListByPostId.get(0).getNumberOfDislikes());
        assertEquals(expectedCommentDtoWithAuthorList.get(0).getUser(), actualCommentsDtoListByPostId.get(0).getUser());
    }

    @Test
    public void getCommentsDtoListByPostIdWithPostNotFoundException() throws PostNotFoundException {
        //Given
        Integer postId = testData.preparedPost().getId();
        when(commentRepository.findAllCommentsByPostId(postId)).thenReturn(null);
        //When
        //Then
        assertThrows(PostNotFoundException.class, () ->  commentService.getCommentsDtoListByPostId(postId),
                "Not found any comments for post with id: " + postId + " !");
    }


}

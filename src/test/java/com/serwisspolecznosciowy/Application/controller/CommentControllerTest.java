package com.serwisspolecznosciowy.Application.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.serwisspolecznosciowy.Application.dto.*;
import com.serwisspolecznosciowy.Application.entity.*;
import com.serwisspolecznosciowy.Application.exception.*;
import com.serwisspolecznosciowy.Application.service.CommentService;
import com.serwisspolecznosciowy.Application.service.PostService;
import com.serwisspolecznosciowy.Application.service.UserService;
import com.serwisspolecznosciowy.Application.testData.TestData;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.web.context.WebApplicationContext;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@WithMockUser(username = "admin12!A", password = "admin12!A", roles = {"ADMIN"})
class CommentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    CommentService commentService;

    @MockBean
    PostService postService;

    @MockBean
    UserService userService;

    @Autowired
    private WebApplicationContext context;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    private TestData testData;

    @Test
    void addNewCommentByPostId() throws Exception, PostNotFoundException {
        //Given
        Post post = testData.preparedPost();
        Integer postId = post.getId();
        CommentDto expectCommentDto = testData.preparedCommentDto();
        CommentBodyDto commentBodyDto = testData.prepareCommentBodyDto();
        when(commentService.addNewComment(postId, commentBodyDto)).thenReturn(expectCommentDto);
        //When
        MvcResult mvcResult = mockMvc.perform(post("/comment/add/{postId}", postId)
                        .content(objectMapper.writeValueAsString(commentBodyDto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().is(201))
                .andReturn();
        //Then
        CommentDto actualCommentDto = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), CommentDto.class);
        assertEquals(expectCommentDto.getBody(), actualCommentDto.getBody());
        assertEquals(expectCommentDto.getUser().getUsername(), actualCommentDto.getUser().getUsername());
    }

    @Test
    void shouldThrowPostNotFoundExceptionDuringAddNewComment() throws Exception, PostNotFoundException {
        Integer incorrectPostId = 9999;
        CommentBodyDto commentBodyDto = testData.prepareCommentBodyDto();
        when(postService.findPostById(incorrectPostId)).thenThrow(PostNotFoundException.class);
        when(commentService.addNewComment(incorrectPostId, commentBodyDto)).thenThrow(PostNotFoundException.class);

        mockMvc.perform(post("/comment/add/{postId}", incorrectPostId)
                        .content(objectMapper.writeValueAsString(commentBodyDto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is(404))
                .andReturn();
    }

    @Test
    void shouldThrowCommentEmptyBodyExceptionDuringAddNewComment() throws Exception, PostNotFoundException {
        Integer postId = testData.preparedPost().getId();
        CommentBodyDto commentBodyDto = testData.prepareCommentBodyDto();
        commentBodyDto.setBody("");
        when(commentService.addNewComment(postId, commentBodyDto)).thenThrow(CommentEmptyBodyException.class);

        mockMvc.perform(post("/comment/add/{postId}", postId)
                        .content(objectMapper.writeValueAsString(commentBodyDto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is(400))
                .andReturn();
    }

    @Test
    void getAllCommentsWithAuthors() throws Exception {
        //Given
        List<Comment> expectedCommentList = testData.preparedCommentList();
        Integer pageNumber = 0;
        Integer pageSize = 10;
        Sort.Direction wayOfSort = Sort.Direction.ASC;
        when(commentService.getAllComments(pageNumber, pageSize, wayOfSort)).thenReturn(expectedCommentList);
        //When
        MvcResult mvcResult = mockMvc.perform(get("/comment/all")
                        .param("page", String.valueOf(pageNumber))
                        .param("size", String.valueOf(pageSize))
                        .param("sort", String.valueOf(wayOfSort)))
                .andDo(print())
                .andExpect(status().is(200))
                .andReturn();
        //Then
        Comment[] actualComments = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), Comment[].class);
        assertEquals(expectedCommentList.get(0).getBody(), actualComments[0].getBody());
    }

    @Test
    void getAllCommentsDtoWithAuthorsDto() throws Exception {
        //Given
        List<CommentDto> expectedCommentDtoList = testData.preparedCommentDtoList();
        Integer pageNumber = 0;
        Integer pageSize = 10;
        Sort.Direction wayOfSort = Sort.Direction.ASC;
        when(commentService.getAllCommentsDto(pageNumber, pageSize, wayOfSort)).thenReturn(expectedCommentDtoList);
        //When
        MvcResult mvcResult = mockMvc.perform(get("/comment/all/dto")
                        .param("page", String.valueOf(pageNumber))
                        .param("size", String.valueOf(pageSize))
                        .param("sort", String.valueOf(wayOfSort)))
                .andDo(print())
                .andExpect(status().is(200))
                .andReturn();
        //Then
        Comment[] actualComments = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), Comment[].class);
        assertEquals(expectedCommentDtoList.get(0).getBody(), actualComments[0].getBody());
    }

    @Test
    void getCommentWithAuthorByCommentId() throws Exception, CommentNotFoundException {
        //Given
        Comment expectedComment = testData.preparedComment();
        when(commentService.getCommentById(expectedComment.getId())).thenReturn(expectedComment);
        //When
        MvcResult mvcResult = mockMvc.perform(get("/comment/{id}", expectedComment.getId()))
                .andDo(print())
                .andExpect(status().is(200))
                .andReturn();
        //Then
        Comment actualComment = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), Comment.class);
        assertEquals(expectedComment.getBody(), actualComment.getBody());
        assertEquals(expectedComment.getUser().getUsername(), actualComment.getUser().getUsername());
        assertEquals(expectedComment.getId(), actualComment.getId());
        assertEquals(expectedComment.getPostId(), actualComment.getPostId());
    }

    @Test
    void getCommentWithAuthorByCommentIdWithCommentNotFoundExceptionException() throws Exception, CommentNotFoundException {
        Integer incorrectId = 999;
        when(commentService.getCommentById(incorrectId)).thenThrow(CommentNotFoundException.class);
        mockMvc.perform(get("/comment/{id}", incorrectId))
                .andDo(print())
                .andExpect(status().is(404))
                .andReturn();
    }

    @Test
    void getCommentDtoWithAuthorDtoByCommentId() throws CommentNotFoundException, Exception {
        //Given
        Integer commentId = testData.preparedComment().getId();
        CommentDto expectedCommentDto = testData.preparedCommentDto();
        when(commentService.getCommentDtoById(commentId)).thenReturn(expectedCommentDto);
        //When
        MvcResult mvcResult = mockMvc.perform(get("/comment/dto/{id}", commentId))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();
        //Then
        CommentDto actualCommentDto1 = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), CommentDto.class);
        assertEquals(expectedCommentDto.getBody(), actualCommentDto1.getBody());
        assertEquals(expectedCommentDto.getUser().getUsername(), actualCommentDto1.getUser().getUsername());
    }

    @Test
    void getCommentDtoWithAuthorDtoByCommentIdWithCommentNotFoundExceptionException() throws CommentNotFoundException, Exception {
        //Given
        Integer incorrectCommentId = 99999;
        CommentDto expectedCommentDto = testData.preparedCommentDto();
        when(commentService.getCommentDtoById(incorrectCommentId)).thenThrow(CommentNotFoundException.class);
        //When
        //Then
        mockMvc.perform(get("/comment/dto/{id}", incorrectCommentId))
                .andDo(print())
                .andExpect(status().is(404))
                .andReturn();
    }

    @Test
    void deleteCommentByIdAndPostId() throws CommentNotFoundException, Exception, UserForbiddenAccessException {
        //Given
        Integer commentId = testData.preparedComment().getId();
        Integer postId = testData.preparedPost().getId();
        doNothing().when(commentService).deleteCommentById(commentId);
        //When
        //Then
        mockMvc.perform(delete("/comment/delete/{commentId}/{postId}", commentId, postId))
                .andDo(print())
                .andExpect(status().isNoContent())
                .andReturn();
    }

    @Test
    void deleteCommentByIdAndPostIdWithCommentNotFoundException() throws CommentNotFoundException, Exception, UserForbiddenAccessException {
        //Given
        Integer incorrectCommentId = 9999;
        Integer postId = testData.preparedPost().getId();
        doThrow(CommentNotFoundException.class).when(commentService).deleteCommentById(incorrectCommentId);
        //When
        //Then
        mockMvc.perform(delete("/comment/delete/{commentId}/{postId}", incorrectCommentId, postId))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andReturn();
    }

    @Test
    void deleteCommentByIdAndPostIdWithPostNotFoundException() throws PostNotFoundException, Exception {
        //Given
        Integer commentId = testData.preparedComment().getId();
        Integer incorrectPostId = 9999;
        doThrow(PostNotFoundException.class).when(postService).subtractOneCommentForNumberOfCommentForPostByPostId(incorrectPostId);
        //When
        //Then
        mockMvc.perform(delete("/comment/delete/{commentId}/{postId}", commentId, incorrectPostId))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andReturn();
    }

    @WithMockUser(username = "test123!A", password = "test123!A", roles = {"USER"})
    @Test
    void deleteCommentByIdAndPostIdWithUserNotAuthorizedException() throws CommentNotFoundException, Exception, UserForbiddenAccessException {
        //Given
        Integer commentId = testData.preparedComment().getId();
        Integer postId = testData.preparedPost().getId();
        doThrow(UserForbiddenAccessException.class).when(commentService).deleteCommentById(commentId);
        //When
        //Then
        mockMvc.perform(delete("/comment/delete/{commentId}/{postId}", commentId, postId))
                .andDo(print())
                .andExpect(status().isForbidden())
                .andReturn();
    }

    @Test
    void getCommentsListByKeywordInCommentBody() throws CommentNotFoundException, Exception {
        //Given
        String body = testData.preparedComment().getBody();
        List<Comment> expectedCommentList = testData.preparedCommentList();
        when(commentService.getCommentsByBody(body)).thenReturn(expectedCommentList);
        //When
        //Then
        MvcResult mvcResult = mockMvc.perform(get("/comment/body")
                .param("body", body))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();
        List<Comment> actualCommentList = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), new TypeReference<List<Comment>>() {
        });
        assertEquals(expectedCommentList.get(0).getUser().getUsername(), actualCommentList.get(0).getUser().getUsername());
        assertEquals(expectedCommentList.get(0).getBody(), actualCommentList.get(0).getBody());
    }

    @Test
    void getCommentsListByKeywordInCommentBodyWithCommentNotFoundException() throws CommentNotFoundException, Exception {
        //Given
        String body = "wrong body";
        when(commentService.getCommentsByBody(body)).thenThrow(CommentNotFoundException.class);
        //When
        //Then
        MvcResult mvcResult = mockMvc.perform(get("/comment/body")
                        .param("body", body))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andReturn();
    }

    @Test
    void getCommentsDtoListByKeywordInCommentBody() throws CommentNotFoundException, Exception {
        //Given
        String body = testData.preparedComment().getBody();
        List<CommentDto> expectedCommentDtoList = testData.preparedCommentDtoList();
        when(commentService.getCommentsDtoByBody(body)).thenReturn(expectedCommentDtoList);
        //When
        //Then
        MvcResult mvcResult = mockMvc.perform(get("/comment/body/dto")
                        .param("body", body))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();
        List<Comment> actualCommentList = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), new TypeReference<List<Comment>>() {
        });
        assertEquals(expectedCommentDtoList.get(0).getUser().getUsername(), actualCommentList.get(0).getUser().getUsername());
        assertEquals(expectedCommentDtoList.get(0).getBody(), actualCommentList.get(0).getBody());
    }

    @Test
    void getCommentsDtoListByKeywordInCommentBodyWithCommentNotFoundException() throws CommentNotFoundException, Exception {
        //Given
        String body = "wrong body";
        when(commentService.getCommentsDtoByBody(body)).thenThrow(CommentNotFoundException.class);
        //When
        //Then
        MvcResult mvcResult = mockMvc.perform(get("/comment/body/dto")
                        .param("body", body))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andReturn();
    }

    @Test
    void addOneLikeToCommentById() throws CommentNotFoundException, Exception {
        //Given
        Integer commentId = testData.preparedComment().getId();
        CommentDto expectedCommentDto = testData.preparedCommentDto();
        List<LikeDto> likeDtoList = testData.preparedLikeDtoList();
        expectedCommentDto.setLikeDtoList(likeDtoList);
        when(commentService.addOneLikeToComment(commentId)).thenReturn(expectedCommentDto);
        //When
        //Then
        MvcResult mvcResult = mockMvc.perform(post("/comment/like/dto/{commentId}", commentId))
                .andDo(print())
                .andExpect(status().is(200))
                .andReturn();
        CommentDto actualCommentDto = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), CommentDto.class);
        assertEquals(expectedCommentDto.getUser().getUsername(), actualCommentDto.getUser().getUsername());
        assertEquals(expectedCommentDto.getBody(), actualCommentDto.getBody());
        assertEquals(expectedCommentDto.getLikeDtoList(), actualCommentDto.getLikeDtoList());
    }

    @Test
    void addOneLikeToCommentByIdWithCommentNotFoundException() throws CommentNotFoundException, Exception {
        //Given
        Integer incorrectCommentId = 9999;
        when(commentService.addOneLikeToComment(incorrectCommentId)).thenThrow(CommentNotFoundException.class);
        //When
        //Then
        MvcResult mvcResult = mockMvc.perform(post("/comment/like/dto/{commentId}", incorrectCommentId))
                .andDo(print())
                .andExpect(status().is(404))
                .andReturn();
    }

    @Test
    void addOneLikeToCommentByIdWithDuplicateUsernameException() throws CommentNotFoundException, Exception {
        //Given
        Integer commentId = testData.preparedComment().getId();
        when(commentService.addOneLikeToComment(commentId)).thenThrow(DuplicateUsernameException.class);
        //When
        //Then
        MvcResult mvcResult = mockMvc.perform(post("/comment/like/dto/{commentId}", commentId))
                .andDo(print())
                .andExpect(status().is(409))
                .andReturn();
    }

    @Test
    void addOneDislikeToCommentById() throws CommentNotFoundException, Exception {
        //Given
        Integer commentId = testData.preparedComment().getId();
        CommentDto expectedCommentDto = testData.preparedCommentDto();
        List<DislikeDto> dislikeDtoList = testData.preparedDislikeDtoList();
        expectedCommentDto.setDislikeDtoList(dislikeDtoList);
        when(commentService.addOneDisLikeToComment(commentId)).thenReturn(expectedCommentDto);
        //When
        //Then
        MvcResult mvcResult = mockMvc.perform(post("/comment/dislike/dto/{commentId}", commentId))
                .andDo(print())
                .andExpect(status().is(200))
                .andReturn();
        CommentDto actualCommentDto = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), CommentDto.class);
        assertEquals(expectedCommentDto.getUser().getUsername(), actualCommentDto.getUser().getUsername());
        assertEquals(expectedCommentDto.getBody(), actualCommentDto.getBody());
        assertEquals(expectedCommentDto.getDislikeDtoList(), actualCommentDto.getDislikeDtoList());
    }

    @Test
    void addOneDislikeToCommentByIdWithCommentNotFoundException() throws CommentNotFoundException, Exception {
        //Given
        Integer incorrectCommentId = 9999;
        when(commentService.addOneDisLikeToComment(incorrectCommentId)).thenThrow(CommentNotFoundException.class);
        //When
        //Then
        MvcResult mvcResult = mockMvc.perform(post("/comment/dislike/dto/{commentId}", incorrectCommentId))
                .andDo(print())
                .andExpect(status().is(404))
                .andReturn();
    }

    @Test
    void addOneDislikeToCommentByIdWithDuplicateUsernameException() throws CommentNotFoundException, Exception {
        //Given
        Integer commentId = testData.preparedComment().getId();
        when(commentService.addOneDisLikeToComment(commentId)).thenThrow(DuplicateUsernameException.class);
        //When
        //Then
        MvcResult mvcResult = mockMvc.perform(post("/comment/dislike/dto/{commentId}", commentId))
                .andDo(print())
                .andExpect(status().is(409))
                .andReturn();
    }

    @Test
    void editCommentByCommentIdWithAdminUser() throws UserForbiddenAccessException, CommentNotFoundException, Exception {
        //Given
        CommentBodyDto commentBodyDto = testData.prepareCommentBodyDto();
        String body = commentBodyDto.getBody();
        Integer commentId = testData.preparedComment().getId();
        CommentDto expectedCommentDto = testData.preparedCommentDto();
        expectedCommentDto.setBody(body);
        User user = testData.preparedUser();
        when(userService.getLoginUser()).thenReturn(user);
        when(commentService.editComment(commentBodyDto, user, commentId)).thenReturn(expectedCommentDto);
        //When
        //Then
        MvcResult mvcResult = mockMvc.perform(patch("/comment/edit/dto/{commendId}", commentId)
                        .content(objectMapper.writeValueAsString(body))
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();
        CommentDto actualCommentDto = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), CommentDto.class);
        assertEquals(expectedCommentDto.getBody(), actualCommentDto.getBody());
    }

    @WithMockUser(username = "test123A!", password = "test123A!", roles = {"USER"})
    @Test
    void editCommentByCommentIdWithAuthorOfComment() throws UserForbiddenAccessException, CommentNotFoundException, Exception {
        //Given
        User user = testData.preparedUser();
        user.setUsername("test123A!");
        user.setPassword("test123A!");
        UserDto userDto = new UserDto(user.getUsername(), user.getProfilePicture(), user.getCreated(), user.getUpdated());

        CommentBodyDto commentBodyDto = testData.prepareCommentBodyDto();
        String body = commentBodyDto.getBody();
        Integer commentId = testData.preparedComment().getId();

        CommentDto expectedCommentDto = testData.preparedCommentDto();
        expectedCommentDto.setBody(body);
        expectedCommentDto.setUser(userDto);

        when(userService.getLoginUser()).thenReturn(user);
        when(commentService.editComment(commentBodyDto, user, commentId)).thenReturn(expectedCommentDto);
        //When
        //Then
        MvcResult mvcResult = mockMvc.perform(patch("/comment/edit/dto/{commendId}", commentId)
                        .content(objectMapper.writeValueAsString(body))
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();
        CommentDto actualCommentDto = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), CommentDto.class);
        assertEquals(expectedCommentDto.getBody(), actualCommentDto.getBody());
        assertEquals(expectedCommentDto.getUser().getUsername(), actualCommentDto.getUser().getUsername());
    }

    @Test
    void editCommentByCommentIdWithCommentNotFoundException() throws UserForbiddenAccessException, CommentNotFoundException, Exception {
        //Given
        Integer incorrectCommentId = 999999;
        CommentBodyDto commentBodyDto = testData.prepareCommentBodyDto();
        String body = commentBodyDto.getBody();
        User user = testData.preparedUser();
        when(userService.getLoginUser()).thenReturn(user);
        when(commentService.editComment(commentBodyDto, user, incorrectCommentId)).thenThrow(CommentNotFoundException.class);
        //When
        //Then
        MvcResult mvcResult = mockMvc.perform(patch("/comment/edit/dto/{commendId}", incorrectCommentId)
                        .content(objectMapper.writeValueAsString(body))
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andReturn();
    }

    @WithMockUser(username = "test123A!", password = "test123A!", roles = {"USER"})
    @Test
    void editCommentByCommentIdWithUserNotAuthorizedException() throws UserForbiddenAccessException, CommentNotFoundException, Exception {
        //Given
        CommentBodyDto commentBodyDto = testData.prepareCommentBodyDto();
        String body = commentBodyDto.getBody();
        Integer commentId = testData.preparedComment().getId();
        User user = testData.preparedUser();
        when(userService.getLoginUser()).thenReturn(user);
        when(commentService.editComment(commentBodyDto, user, commentId)).thenThrow(UserForbiddenAccessException.class);
        //When
        //Then
        MvcResult mvcResult = mockMvc.perform(patch("/comment/edit/dto/{commendId}", commentId)
                        .content(objectMapper.writeValueAsString(body))
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isForbidden()) /* different user is login and different user is author of comment*/
                .andReturn();
    }

    @Test
    void getCommentDtoListByPostId() throws PostNotFoundException, Exception {
        //Given
        Integer postId = testData.preparedPost().getId();
        List<CommentDto> expectedCommentDtoWithAuthorsList = testData.preparedCommentDtoList();
        when(commentService.getCommentsDtoListByPostId(postId)).thenReturn(expectedCommentDtoWithAuthorsList);
        //When
        //Then
        MvcResult mvcResult = mockMvc.perform(get("/comment/all/dto/{postId}", postId))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();
        List<CommentDto> actualCommentDtoWithAuthorsList = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), new TypeReference<List<CommentDto>>() {
        });
        assertEquals(expectedCommentDtoWithAuthorsList.get(0).getBody(), actualCommentDtoWithAuthorsList.get(0).getBody());
        assertEquals(expectedCommentDtoWithAuthorsList.get(0).getUser().getUsername(), actualCommentDtoWithAuthorsList.get(0).getUser().getUsername());
    }

    @Test
    void getCommentDtoListByPostIdWithPostNotFoundException() throws PostNotFoundException, Exception {
        //Given
        Integer incorrectPostId = 9999999;
        when(commentService.getCommentsDtoListByPostId(incorrectPostId)).thenThrow(PostNotFoundException.class);
        //When
        //Then
        MvcResult mvcResult = mockMvc.perform(get("/comment/all/dto/{postId}", incorrectPostId))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andReturn();
    }


}
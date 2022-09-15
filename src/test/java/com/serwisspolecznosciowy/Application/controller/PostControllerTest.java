package com.serwisspolecznosciowy.Application.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.serwisspolecznosciowy.Application.dto.PostBodyDto;
import com.serwisspolecznosciowy.Application.dto.PostDtoWithAuthor;
import com.serwisspolecznosciowy.Application.exception.PostEmptyBodyException;
import com.serwisspolecznosciowy.Application.exception.PostNotFoundException;
import com.serwisspolecznosciowy.Application.entity.Post;
import com.serwisspolecznosciowy.Application.entity.User;
import com.serwisspolecznosciowy.Application.exception.UserForbiddenAccessException;
import com.serwisspolecznosciowy.Application.exception.UserNotFoundException;
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

import java.util.List;
import java.util.Optional;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@WithMockUser(username = "admin12!A", password = "admin12!A", roles = {"ADMIN"})
class PostControllerTest {

    @Autowired
    MockMvc mockMvc;
    @Autowired
    ObjectMapper objectMapper;
    @MockBean
    PostService postService;
    @MockBean
    UserService userService;
    @Autowired
    TestData testData;

    @WithMockUser
    @Test
    void addNewPost() throws Exception {
        //Given
        PostDtoWithAuthor expectedPostDtoWithAuthor = testData.preparedPostDtoWithAuthor();
        PostBodyDto postBodyDto = testData.preparedPostBodyDto();
        when(postService.addNewPost(postBodyDto)).thenReturn(expectedPostDtoWithAuthor);
        //When
        MvcResult mvcResult = mockMvc.perform(post("/post/add/dto")
                        .content(objectMapper.writeValueAsString(postBodyDto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is(201))
                .andReturn();
        //Then
        PostDtoWithAuthor actualPostDtoWithAuthor = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), PostDtoWithAuthor.class);
        assertEquals(expectedPostDtoWithAuthor.getBody(), actualPostDtoWithAuthor.getBody());
        assertEquals(expectedPostDtoWithAuthor.getUsername(), actualPostDtoWithAuthor.getUsername());
    }

    @Test
    void addNewPostWithPostEmptyBodyException() throws Exception {
        //Given
        String body = "";
        PostBodyDto postBodyDto = testData.preparedPostBodyDto();
        postBodyDto.setBody(body);
        when(postService.addNewPost(postBodyDto)).thenThrow(PostEmptyBodyException.class);
        //When
        mockMvc.perform(post("/post/add/dto")
                        .content(objectMapper.writeValueAsString(postBodyDto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is(400))
                .andReturn();
    }

    @Test
    void getAllPostsWitCommentsAndAuthors() throws Exception {
        //Given
        Integer pageNumber = 0;
        Integer pageSize = 10;
        Sort.Direction wayOfSort = Sort.Direction.ASC;
        List<Post> expectedPostList = testData.preparedPostsList();
        when(postService.getAllPostsWithComments(pageNumber, pageSize, wayOfSort)).thenReturn(expectedPostList);
        //When
        MvcResult mvcResult = mockMvc.perform(get("/post/all")
                        .param("page", String.valueOf(pageNumber))
                        .param("size", String.valueOf(pageSize))
                        .param("sort", String.valueOf(wayOfSort)))
                .andDo(print())
                .andExpect(status().is(200))
                .andReturn();
        //Then
        List<Post> actualPostList = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), new TypeReference<List<Post>>() {
        });
        assertEquals(expectedPostList.get(0).getBody(), actualPostList.get(0).getBody());
        assertEquals(expectedPostList.get(0).getUser().getUsername(), actualPostList.get(0).getUser().getUsername());
    }

    @Test
    void getAllPostsDtoWithUsernameAndUserProfilePicture() throws Exception {
        //Given
        Integer pageNumber = 0;
        Integer pageSize = 10;
        Sort.Direction wayOfSort = Sort.Direction.ASC;
        List<PostDtoWithAuthor> expectedPostDtoWithAuthorstList = testData.preparedPostDtoWithAuthorList();
        when(postService.getAllPostsDtoWithUsersDto(pageNumber, pageSize, wayOfSort)).thenReturn(expectedPostDtoWithAuthorstList);
        //When
        MvcResult mvcResult = mockMvc.perform(get("/post/all/dto")
                        .param("page", String.valueOf(pageNumber))
                        .param("size", String.valueOf(pageSize))
                        .param("sort", String.valueOf(wayOfSort)))
                .andDo(print())
                .andExpect(status().is(200))
                .andReturn();
        //Then
        List<PostDtoWithAuthor> actualPostDtoWithAuthorList = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), new TypeReference<List<PostDtoWithAuthor>>() {
        });
        assertEquals(expectedPostDtoWithAuthorstList.get(0).getBody(), actualPostDtoWithAuthorList.get(0).getBody());
        assertEquals(expectedPostDtoWithAuthorstList.get(0).getUsername(), actualPostDtoWithAuthorList.get(0).getUsername());
    }

    @Test
    void editPostByPostDtoByAdmin() throws Exception, PostNotFoundException, UserForbiddenAccessException {
        //Given
        PostBodyDto postBodyDto = testData.preparedEditPostDto();
        String body = postBodyDto.getBody();

        PostDtoWithAuthor expectedPostDtoWithAuthor = testData.preparedPostDtoWithAuthor();
        expectedPostDtoWithAuthor.setBody(body);

        User admin = testData.preparedAdmin();
        Integer postId = testData.preparedPost().getId();

        when(userService.getLoginUser()).thenReturn(admin);
        when(postService.editPost(postBodyDto, admin, postId)).thenReturn(expectedPostDtoWithAuthor);
        //When
        MvcResult mvcResult = mockMvc.perform(put("/post/edit/dto/{postId}", postId)
                        .content(objectMapper.writeValueAsString(body))
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().is(200))
                .andReturn();
        //Then
        PostDtoWithAuthor actualPostDtoWithAuthor = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), PostDtoWithAuthor.class);
        assertEquals(expectedPostDtoWithAuthor.getBody(), actualPostDtoWithAuthor.getBody());
    }

    @WithMockUser(username = "test12!A", password = "test12!A")
    @Test
    void editPostByPostDtoByUser() throws Exception, PostNotFoundException, UserForbiddenAccessException {
        //Given
        PostBodyDto postBodyDto = testData.preparedEditPostDto();
        String body = postBodyDto.getBody();

        User user = testData.preparedUser();

        PostDtoWithAuthor expectedPostDtoWithAuthor = testData.preparedPostDtoWithAuthor();
        expectedPostDtoWithAuthor.setBody(body);
        expectedPostDtoWithAuthor.setUsername(user.getUsername());

        Integer postId = testData.preparedPost().getId();

        when(userService.getLoginUser()).thenReturn(user);
        when(postService.editPost(postBodyDto, user, postId)).thenReturn(expectedPostDtoWithAuthor);
        //When
        MvcResult mvcResult = mockMvc.perform(put("/post/edit/dto/{postId}", postId)
                        .content(objectMapper.writeValueAsString(body))
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().is(200))
                .andReturn();
        //Then
        PostDtoWithAuthor actualPostDtoWithAuthor = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), PostDtoWithAuthor.class);
        assertEquals(expectedPostDtoWithAuthor.getBody(), actualPostDtoWithAuthor.getBody());
        assertEquals(expectedPostDtoWithAuthor.getUsername(), actualPostDtoWithAuthor.getUsername());
    }

    @Test
    void editPostByPostDtoByUserWithPostNotFoundException() throws Exception, PostNotFoundException, UserForbiddenAccessException {
        //Given
        Integer incorrectPostId = 999999;

        PostBodyDto postBodyDto = testData.preparedEditPostDto();
        String body = postBodyDto.getBody();

        User user = testData.preparedUser();

        when(userService.getLoginUser()).thenReturn(user);
        when(postService.editPost(postBodyDto, user, incorrectPostId)).thenThrow(PostNotFoundException.class);
        //When
        mockMvc.perform(put("/post/edit/dto/{postId}", incorrectPostId)
                        .content(objectMapper.writeValueAsString(body))
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().is(404))
                .andReturn();
    }

    @Test
    void editPostByPostDtoByUserWithUserNotAuthorizedException() throws Exception, PostNotFoundException, UserForbiddenAccessException {
        //Given
        PostBodyDto postBodyDto = testData.preparedEditPostDto();
        String body = postBodyDto.getBody();

        User user = testData.preparedUser();

        PostDtoWithAuthor expectedPostDtoWithAuthor = testData.preparedPostDtoWithAuthor();
        expectedPostDtoWithAuthor.setBody(body);

        Integer postId = testData.preparedPost().getId();

        when(userService.getLoginUser()).thenReturn(user);
        when(postService.editPost(postBodyDto, user, postId)).thenThrow(UserForbiddenAccessException.class);
        //When
        MvcResult mvcResult = mockMvc.perform(put("/post/edit/dto/{postId}", postId)
                        .content(objectMapper.writeValueAsString(body))
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().is(403)) /* login mock user is different author of post */
                .andReturn();
    }

    @Test
    void editPostByPostDtoWithPostEmptyBodyException() throws Exception, PostNotFoundException, UserForbiddenAccessException {
        //Given
        PostBodyDto postBodyDto = testData.preparedEditPostDto();
        String body = "";
        postBodyDto.setBody(body);

        User admin = testData.preparedAdmin();
        Integer postId = testData.preparedPost().getId();

        when(userService.getLoginUser()).thenReturn(admin);
        when(postService.editPost(postBodyDto, admin, postId)).thenThrow(PostEmptyBodyException.class);
        //When
        mockMvc.perform(put("/post/edit/dto/{postId}", postId)
                        .content(objectMapper.writeValueAsString(body))
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().is(400))
                .andReturn();
    }

    @Test
    void deletePostById() throws Exception, UserForbiddenAccessException, PostNotFoundException {
        //Given
        Post post = testData.preparedPost();
        Integer postId = post.getId();
        User admin = testData.preparedAdmin();
        when(userService.findUserByUsername(admin.getUsername())).thenReturn(Optional.of(admin));
        doNothing().when(postService).deletePostById(Optional.of(admin), postId);
        //When
        mockMvc.perform(delete("/post/delete/{id}", postId))
                .andDo(print())
                .andExpect(status().is(204))
                .andReturn();
    }

    @WithMockUser(username = "test", password = "test")
    @Test
    void deletePostByIdWithUserNotFoundException() throws Exception, UserForbiddenAccessException, PostNotFoundException {
        //Given
        Post post = testData.preparedPost();
        Integer postId = post.getId();
        User user = testData.preparedUser();
        user.setUsername("test");
        user.setPassword("test");
        when(userService.findUserByUsername(user.getUsername())).thenThrow(UserNotFoundException.class);
        //When
        mockMvc.perform(delete("/post/delete/{id}", postId))
                .andDo(print())
                .andExpect(status().is(404))
                .andReturn();
    }

    @Test
    void deletePostByIdWithPostNotFoundException() throws Exception, UserForbiddenAccessException, PostNotFoundException {
        //Given
        Post post = testData.preparedPost();
        Integer incorrectPostId = 9999;
        User admin = testData.preparedAdmin();
        when(userService.findUserByUsername(admin.getUsername())).thenReturn(Optional.of(admin));
        doThrow(PostNotFoundException.class).when(postService).deletePostById(Optional.of(admin), incorrectPostId);
        //When
        mockMvc.perform(delete("/post/delete/{id}", incorrectPostId))
                .andDo(print())
                .andExpect(status().is(404))
                .andReturn();
    }

    @WithMockUser(username = "test", password = "test")
    @Test
    void deletePostByIdWithUserNotAuthorizedException() throws Exception, UserForbiddenAccessException, PostNotFoundException {
        //Given
        Post post = testData.preparedPost();
        Integer postId = post.getId();
        User user = testData.preparedUser();
        user.setUsername("test");
        user.setPassword("test");
        when(userService.findUserByUsername(user.getUsername())).thenReturn(Optional.of(user));
        doThrow(UserForbiddenAccessException.class).when(postService).deletePostById(Optional.of(user), postId);
        //When
        mockMvc.perform(delete("/post/delete/{id}", postId))
                .andDo(print())
                .andExpect(status().isForbidden())
                .andReturn();
    }

    @Test
    void getPostById() throws Exception, PostNotFoundException {
        //Given
        Post expectedPost = testData.preparedPost();
        Integer postId = expectedPost.getId();
        when(postService.findPostById(postId)).thenReturn(expectedPost);
        //When
        MvcResult mvcResult = mockMvc.perform(get("/post/{id}", postId))
                .andDo(print())
                .andExpect(status().is(200))
                .andReturn();
        //Then
        Optional<Post> actualOptionalPost = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), new TypeReference<Optional<Post>>() {
        });
        assertEquals(expectedPost.getBody(), actualOptionalPost.get().getBody());
        assertEquals(expectedPost.getUser().getUsername(), actualOptionalPost.get().getUser().getUsername());
        assertEquals(expectedPost.getId(), actualOptionalPost.get().getId());
    }

    @Test
    void findPostByIdWithPostNotFoundException() throws Exception, PostNotFoundException {
        //Given
        Integer incorrectPostId = 999999;
        when(postService.findPostById(incorrectPostId)).thenThrow(PostNotFoundException.class);
        //When
        mockMvc.perform(get("/post/{id}", incorrectPostId))
                .andDo(print())
                .andExpect(status().is(404))
                .andReturn();
    }

    @Test
    void getPostDtoById() throws PostNotFoundException, Exception {
        //Given
        Integer postId = testData.preparedPost().getId();
        PostDtoWithAuthor expectedPostDtoWithAuthor = testData.preparedPostDtoWithAuthor();
        when(postService.findPostDtoById(postId)).thenReturn(expectedPostDtoWithAuthor);
        //When
        MvcResult mvcResult = mockMvc.perform(get("/post/dto/{id}", postId))
                .andDo(print())
                .andExpect(status().is(200))
                .andReturn();
        //Then
        PostDtoWithAuthor actualPostDtoWithAuthor = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), PostDtoWithAuthor.class);
        assertEquals(expectedPostDtoWithAuthor.getUsername(), actualPostDtoWithAuthor.getUsername());
        assertEquals(expectedPostDtoWithAuthor.getBody(), actualPostDtoWithAuthor.getBody());
    }

    @Test
    void getPostDtoByIdWithPostNotFoundException() throws PostNotFoundException, Exception {
        //Given
        Integer incorrectPostId = 9999999;
        when(postService.findPostDtoById(incorrectPostId)).thenThrow(PostNotFoundException.class);
        //When
        mockMvc.perform(get("/post/dto/{id}", incorrectPostId))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andReturn();
    }

    @Test
    void getPostDtoListByKeywordInPostBody() throws PostNotFoundException, Exception {
        //Given
        PostDtoWithAuthor postDtoWithAuthor = testData.preparedPostDtoWithAuthor();
        postDtoWithAuthor.setBody("test post body - java in great!");
        List<PostDtoWithAuthor> expectedPostDtoWithAuthorList = testData.preparedPostDtoWithAuthorList();
        String keyword = "java";
        when(postService.getPostDtoListByBody(keyword)).thenReturn(expectedPostDtoWithAuthorList);
        //When
        MvcResult mvcResult = mockMvc.perform(get("/post/body/dto")
                        .param("keywordInBody", keyword))
                .andExpect(status().isOk())
                .andReturn();
        //Then
        List<PostDtoWithAuthor> actualPostDtoWithAuthorList1 = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), new TypeReference<List<PostDtoWithAuthor>>() {
        });
        assertEquals(expectedPostDtoWithAuthorList.get(0).getBody(), actualPostDtoWithAuthorList1.get(0).getBody());
        assertEquals(expectedPostDtoWithAuthorList.get(0).getUsername(), actualPostDtoWithAuthorList1.get(0).getUsername());
        assertEquals(expectedPostDtoWithAuthorList.get(0).getProfilePicture(), actualPostDtoWithAuthorList1.get(0).getProfilePicture());
    }

    @Test
    void getPostDtoListByKeywordInPostBodyWithPostNotFoundException() throws PostNotFoundException, Exception {
        //Given
        List<PostDtoWithAuthor> expectedPostDtoWithAuthorList = testData.preparedPostDtoWithAuthorList();
        String keyword = "java";
        when(postService.getPostDtoListByBody(keyword)).thenThrow(PostNotFoundException.class);
        //When
        MvcResult mvcResult = mockMvc.perform(get("/post/body/dto")
                        .param("keywordInBody", keyword))
                .andExpect(status().isNotFound())
                .andReturn();
    }

    @Test
    void addOneLikeToPostByPostId() throws PostNotFoundException, Exception {
        //Given
        Integer postId = testData.preparedPost().getId();
        PostDtoWithAuthor expectedPostDtoWithAuthor = testData.preparedPostDtoWithAuthor();
        expectedPostDtoWithAuthor.setNumberOfLikes(14);
        when(postService.addOneLikeToPost(postId)).thenReturn(expectedPostDtoWithAuthor);
        //When
        MvcResult mvcResult = mockMvc.perform(post("/post/addLike/dto/{postId}", postId))
                .andDo(print())
                .andExpect(status().is(200))
                .andReturn();
        //Then
        PostDtoWithAuthor actualPostDtoWithAuthor = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), PostDtoWithAuthor.class);
        assertEquals(expectedPostDtoWithAuthor.getNumberOfLikes(), actualPostDtoWithAuthor.getNumberOfLikes());
        assertEquals(expectedPostDtoWithAuthor.getUsername(), actualPostDtoWithAuthor.getUsername());
        assertEquals(expectedPostDtoWithAuthor.getBody(), actualPostDtoWithAuthor.getBody());
    }

    @Test
    void addOneLikeToPostByPostIdWithPostNotFoundException() throws PostNotFoundException, Exception {
        //Given
        Integer incorrectPostId = 999999999;
        when(postService.addOneLikeToPost(incorrectPostId)).thenThrow(PostNotFoundException.class);
        //When
        mockMvc.perform(post("/post/addLike/dto/{postId}", incorrectPostId))
                .andDo(print())
                .andExpect(status().is(404))
                .andReturn();
    }

    @Test
    void addOneDislikeToPostByPostId() throws PostNotFoundException, Exception {
        //Given
        Integer postId = testData.preparedPost().getId();
        PostDtoWithAuthor expectedPostDtoWithAuthor = testData.preparedPostDtoWithAuthor();
        expectedPostDtoWithAuthor.setNumberOfDislikes(14);
        when(postService.addOneDisLikeToPost(postId)).thenReturn(expectedPostDtoWithAuthor);
        //When
        MvcResult mvcResult = mockMvc.perform(post("/post/addDislike/dto/{postId}", postId))
                .andDo(print())
                .andExpect(status().is(200))
                .andReturn();
        //Then
        PostDtoWithAuthor actualPostDtoWithAuthor = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), PostDtoWithAuthor.class);
        assertEquals(expectedPostDtoWithAuthor.getNumberOfDislikes(), actualPostDtoWithAuthor.getNumberOfDislikes());
        assertEquals(expectedPostDtoWithAuthor.getUsername(), actualPostDtoWithAuthor.getUsername());
        assertEquals(expectedPostDtoWithAuthor.getBody(), actualPostDtoWithAuthor.getBody());
    }

    @Test
    void addOneDislikeToPostByPostIdWithPostNotFoundException() throws PostNotFoundException, Exception {
        //Given
        Integer incorrectPostId = 9999;
        when(postService.addOneDisLikeToPost(incorrectPostId)).thenThrow(PostNotFoundException.class);
        //When
        mockMvc.perform(post("/post/addDislike/dto/{postId}", incorrectPostId))
                .andDo(print())
                .andExpect(status().is(404))
                .andReturn();
    }

}
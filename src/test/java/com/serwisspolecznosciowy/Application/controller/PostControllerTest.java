package com.serwisspolecznosciowy.Application.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.serwisspolecznosciowy.Application.dto.DislikeDto;
import com.serwisspolecznosciowy.Application.dto.LikeDto;
import com.serwisspolecznosciowy.Application.dto.PostBodyDto;
import com.serwisspolecznosciowy.Application.dto.PostDto;
import com.serwisspolecznosciowy.Application.entity.Dislike;
import com.serwisspolecznosciowy.Application.entity.Like;
import com.serwisspolecznosciowy.Application.exception.*;
import com.serwisspolecznosciowy.Application.entity.Post;
import com.serwisspolecznosciowy.Application.entity.User;
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
        PostDto expectedPostDto = testData.preparedPostDto();
        PostBodyDto postBodyDto = testData.preparedPostBodyDto();
        when(postService.addNewPost(postBodyDto)).thenReturn(expectedPostDto);
        //When
        MvcResult mvcResult = mockMvc.perform(post("/post/add/dto")
                        .content(objectMapper.writeValueAsString(postBodyDto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is(201))
                .andReturn();
        //Then
        PostDto actualPostDto = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), PostDto.class);
        assertEquals(expectedPostDto.getBody(), actualPostDto.getBody());
        assertEquals(expectedPostDto.getUsername(), actualPostDto.getUsername());
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
        List<PostDto> expectedPostDtoWithAuthorstList = testData.preparedPostDtoWithAuthorList();
        when(postService.getAllPostsDto(pageNumber, pageSize, wayOfSort)).thenReturn(expectedPostDtoWithAuthorstList);
        //When
        MvcResult mvcResult = mockMvc.perform(get("/post/all/dto")
                        .param("page", String.valueOf(pageNumber))
                        .param("size", String.valueOf(pageSize))
                        .param("sort", String.valueOf(wayOfSort)))
                .andDo(print())
                .andExpect(status().is(200))
                .andReturn();
        //Then
        List<PostDto> actualPostDtoList = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), new TypeReference<List<PostDto>>() {
        });
        assertEquals(expectedPostDtoWithAuthorstList.get(0).getBody(), actualPostDtoList.get(0).getBody());
        assertEquals(expectedPostDtoWithAuthorstList.get(0).getUsername(), actualPostDtoList.get(0).getUsername());
    }

    @Test
    void editPostByPostDtoByAdmin() throws Exception, PostNotFoundException, UserForbiddenAccessException {
        //Given
        PostBodyDto postBodyDto = testData.preparedEditPostDto();
        String body = postBodyDto.getBody();

        PostDto expectedPostDto = testData.preparedPostDto();
        expectedPostDto.setBody(body);

        User admin = testData.preparedAdmin();
        Integer postId = testData.preparedPost().getId();

        when(userService.getLoginUser()).thenReturn(admin);
        when(postService.editPost(postBodyDto, admin, postId)).thenReturn(expectedPostDto);
        //When
        MvcResult mvcResult = mockMvc.perform(put("/post/edit/dto/{postId}", postId)
                        .content(objectMapper.writeValueAsString(body))
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().is(200))
                .andReturn();
        //Then
        PostDto actualPostDto = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), PostDto.class);
        assertEquals(expectedPostDto.getBody(), actualPostDto.getBody());
    }

    @WithMockUser(username = "test12!A", password = "test12!A")
    @Test
    void editPostByPostDtoByUser() throws Exception, PostNotFoundException, UserForbiddenAccessException {
        //Given
        PostBodyDto postBodyDto = testData.preparedEditPostDto();
        String body = postBodyDto.getBody();

        User user = testData.preparedUser();

        PostDto expectedPostDto = testData.preparedPostDto();
        expectedPostDto.setBody(body);
        expectedPostDto.setUsername(user.getUsername());

        Integer postId = testData.preparedPost().getId();

        when(userService.getLoginUser()).thenReturn(user);
        when(postService.editPost(postBodyDto, user, postId)).thenReturn(expectedPostDto);
        //When
        MvcResult mvcResult = mockMvc.perform(put("/post/edit/dto/{postId}", postId)
                        .content(objectMapper.writeValueAsString(body))
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().is(200))
                .andReturn();
        //Then
        PostDto actualPostDto = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), PostDto.class);
        assertEquals(expectedPostDto.getBody(), actualPostDto.getBody());
        assertEquals(expectedPostDto.getUsername(), actualPostDto.getUsername());
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

        PostDto expectedPostDto = testData.preparedPostDto();
        expectedPostDto.setBody(body);

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
        PostDto expectedPostDto = testData.preparedPostDto();
        when(postService.findPostDtoById(postId)).thenReturn(expectedPostDto);
        //When
        MvcResult mvcResult = mockMvc.perform(get("/post/dto/{id}", postId))
                .andDo(print())
                .andExpect(status().is(200))
                .andReturn();
        //Then
        PostDto actualPostDto = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), PostDto.class);
        assertEquals(expectedPostDto.getUsername(), actualPostDto.getUsername());
        assertEquals(expectedPostDto.getBody(), actualPostDto.getBody());
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
        PostDto postDto = testData.preparedPostDto();
        postDto.setBody("test post body - java in great!");
        List<PostDto> expectedPostDtoList = testData.preparedPostDtoWithAuthorList();
        String keyword = "java";
        when(postService.getPostDtoListByBody(keyword)).thenReturn(expectedPostDtoList);
        //When
        MvcResult mvcResult = mockMvc.perform(get("/post/body/dto")
                        .param("keywordInBody", keyword))
                .andExpect(status().isOk())
                .andReturn();
        //Then
        List<PostDto> actualPostDtoList1 = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), new TypeReference<List<PostDto>>() {
        });
        assertEquals(expectedPostDtoList.get(0).getBody(), actualPostDtoList1.get(0).getBody());
        assertEquals(expectedPostDtoList.get(0).getUsername(), actualPostDtoList1.get(0).getUsername());
        assertEquals(expectedPostDtoList.get(0).getProfilePicture(), actualPostDtoList1.get(0).getProfilePicture());
    }

    @Test
    void getPostDtoListByKeywordInPostBodyWithPostNotFoundException() throws PostNotFoundException, Exception {
        //Given
        List<PostDto> expectedPostDtoList = testData.preparedPostDtoWithAuthorList();
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
        PostDto expectedPostDto = testData.preparedPostDto();
        expectedPostDto.setLikeDtoList(List.of(new LikeDto("test")));
        when(postService.addOneLikeToPost(postId)).thenReturn(expectedPostDto);
        //When
        MvcResult mvcResult = mockMvc.perform(post("/post/like/dto/{postId}", postId))
                .andDo(print())
                .andExpect(status().is(200))
                .andReturn();
        //Then
        PostDto actualPostDto = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), PostDto.class);
        assertEquals(expectedPostDto.getLikeDtoList(), actualPostDto.getLikeDtoList());
        assertEquals(expectedPostDto.getUsername(), actualPostDto.getUsername());
        assertEquals(expectedPostDto.getBody(), actualPostDto.getBody());
    }

    @Test
    void addOneLikeToPostByPostIdWithPostNotFoundException() throws PostNotFoundException, Exception {
        //Given
        Integer incorrectPostId = 999999999;
        when(postService.addOneLikeToPost(incorrectPostId)).thenThrow(PostNotFoundException.class);
        //When
        mockMvc.perform(post("/post/like/dto/{postId}", incorrectPostId))
                .andDo(print())
                .andExpect(status().is(404))
                .andReturn();
    }

    @Test
    void addOneLikeToPostByPostIdWithDuplicateUsernameException() throws PostNotFoundException, Exception {
        //Given
        Integer postId = testData.preparedPost().getId();
        when(postService.addOneLikeToPost(postId)).thenThrow(DuplicateUsernameException.class);
        //When
        mockMvc.perform(post("/post/like/dto/{postId}", postId))
                .andDo(print())
                .andExpect(status().isConflict())
                .andReturn();
    }

    @Test
    void addOneDislikeToPostByPostId() throws PostNotFoundException, Exception {
        //Given
        Integer postId = testData.preparedPost().getId();
        PostDto expectedPostDto = testData.preparedPostDto();
        List<DislikeDto> dislikeDtoList = testData.preparedDislikeDtoList();
        expectedPostDto.setDislikeDtoList(dislikeDtoList);
        when(postService.addOneDisLikeToPost(postId)).thenReturn(expectedPostDto);
        //When
        MvcResult mvcResult = mockMvc.perform(post("/post/dislike/dto/{postId}", postId))
                .andDo(print())
                .andExpect(status().is(200))
                .andReturn();
        //Then
        PostDto actualPostDto = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), PostDto.class);
        assertEquals(expectedPostDto.getDislikeDtoList(), actualPostDto.getDislikeDtoList());
        assertEquals(expectedPostDto.getUsername(), actualPostDto.getUsername());
        assertEquals(expectedPostDto.getBody(), actualPostDto.getBody());
    }

    @Test
    void addOneDislikeToPostByPostIdWithPostNotFoundException() throws PostNotFoundException, Exception {
        //Given
        Integer incorrectPostId = 9999;
        when(postService.addOneDisLikeToPost(incorrectPostId)).thenThrow(PostNotFoundException.class);
        //When
        mockMvc.perform(post("/post/dislike/dto/{postId}", incorrectPostId))
                .andDo(print())
                .andExpect(status().is(404))
                .andReturn();
    }

    @Test
    void addOneDislikeToPostByPostIdWithDuplicateUsernameException() throws PostNotFoundException, Exception {
        //Given
        Integer postId = testData.preparedPost().getId();
        when(postService.addOneDisLikeToPost(postId)).thenThrow(DuplicateUsernameException.class);
        //When
        mockMvc.perform(post("/post/dislike/dto/{postId}", postId))
                .andDo(print())
                .andExpect(status().is(409))
                .andReturn();
    }

    @Test
    void getNumberOfLikesByPostId() throws PostNotFoundException, Exception {
        //Given
        Post post = testData.preparedPost();
        List<Like> listLike = testData.preparedLikeList();
        post.setLikeList(listLike);
        Integer postId = post.getId();
        Integer expectedNumberOfLikes = post.getLikeList().size();
        when(postService.getNumberOfLikesByPostId(postId)).thenReturn(expectedNumberOfLikes);
        //When
        MvcResult mvcResult = mockMvc.perform(get("/post/likes/dto/{postId}", postId))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();
        //Then
        Integer actualNumberOfLikes = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), Integer.class);
        assertEquals(expectedNumberOfLikes, actualNumberOfLikes);
    }

    @Test
    void getNumberOfLikesByPostIdWithPostNotFoundException() throws PostNotFoundException, Exception {
        //Given
        Integer incorrectPostId = 999999999;
        when(postService.getNumberOfLikesByPostId(incorrectPostId)).thenThrow(PostNotFoundException.class);
        //When
        mockMvc.perform(get("/post/likes/dto/{postId}", incorrectPostId))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andReturn();
    }

    @Test
    void getNumberOfDislikesByPostId() throws PostNotFoundException, Exception {
        //Given
        Post post = testData.preparedPost();
        List<Dislike> dilistLike = testData.preparedDislikeList();
        post.setDislikeList(dilistLike);
        Integer postId = post.getId();
        Integer expectedNumberOfDisikes = post.getDislikeList().size();
        when(postService.getNumberOfDislikesByPostId(postId)).thenReturn(expectedNumberOfDisikes);
        //When
        MvcResult mvcResult = mockMvc.perform(get("/post/dislikes/dto/{postId}", postId))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();
        //Then
        Integer actualNumberOfDisikes = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), Integer.class);
        assertEquals(expectedNumberOfDisikes, actualNumberOfDisikes);
    }

    @Test
    void getNumberOfDislikesByPostIdReturnPostNotFoundException() throws PostNotFoundException, Exception {
        //Given
        Post post = testData.preparedPost();
        Integer incorrectPostId = post.getId();
        when(postService.getNumberOfDislikesByPostId(incorrectPostId)).thenThrow(PostNotFoundException.class);
        //When
        mockMvc.perform(get("/post/dislikes/dto/{postId}", incorrectPostId))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andReturn();
    }

}
package com.serwisspolecznosciowy.Application.service;

import com.serwisspolecznosciowy.Application.dto.PostBodyDto;
import com.serwisspolecznosciowy.Application.dto.PostDtoWithAuthor;
import com.serwisspolecznosciowy.Application.entity.*;
import com.serwisspolecznosciowy.Application.exception.PostEmptyBodyException;
import com.serwisspolecznosciowy.Application.exception.PostNotFoundException;
import com.serwisspolecznosciowy.Application.exception.UserForbiddenAccessException;
import com.serwisspolecznosciowy.Application.exception.UserNotFoundException;
import com.serwisspolecznosciowy.Application.mappers.PostMapper;
import com.serwisspolecznosciowy.Application.repository.CommentRepository;
import com.serwisspolecznosciowy.Application.repository.PostRepository;
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
class PostServiceTest {

    @InjectMocks
    private PostService postService;

    @Mock
    private PostRepository postRepository;

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private UserService userService;

    @Mock
    private PostMapper postMapper;

    @Autowired
    public TestData testData;

    @Test
    void addNewPost() throws PostEmptyBodyException, UserNotFoundException {
        //given
        User user = testData.preparedUser();
        Post post = testData.preparedPost();
        PostDtoWithAuthor expectedPost = testData.preparedPostDtoWithAuthor();
        PostBodyDto postBodyDto = testData.preparedPostBodyDto();

        when(userService.getLoginUser()).thenReturn(user);
        when(postRepository.save(any(Post.class))).thenReturn(post);
        when(postMapper.postToPostDtoWithAuthor(any(Post.class), any(User.class))).thenReturn(expectedPost);

        //when
        PostDtoWithAuthor actualPost = postService.addNewPost(postBodyDto);

        //then
        assertEquals(expectedPost.getBody(), actualPost.getBody());
        assertEquals(expectedPost.getCreated(), actualPost.getCreated());
        assertEquals(expectedPost.getUsername(), actualPost.getUsername());
    }

    @Test
    void addNewPostWithPostEmptyBodyException() throws PostEmptyBodyException, UserNotFoundException {
        //given
        User user = testData.preparedUser();
        PostBodyDto postBodyDto = testData.preparedPostBodyDto();
        postBodyDto.setBody("");
        when(userService.getLoginUser()).thenReturn(user);
        //when
        //then
        assertThrows(PostEmptyBodyException.class, () -> postService.addNewPost(postBodyDto));
    }

    @Test
    void getAllPostsWithComments() {
        //given
        Integer pageNumber = 0;
        Integer pageSize = 10;
        Sort.Direction wayOfSort = Sort.Direction.ASC;
        List<Post> expectedPostsList = testData.preparedPostsList();
        when(postRepository.findAllPostsWithComments(PageRequest.of(pageNumber, pageSize, Sort.by(wayOfSort, "created")))).thenReturn(expectedPostsList);

        //when
        List<Post> actualPostsList = postService.getAllPostsWithComments(pageNumber, pageSize, wayOfSort);

        //then
        assertEquals(expectedPostsList.size(), actualPostsList.size());
        assertEquals(expectedPostsList.get(0).getBody(), actualPostsList.get(0).getBody());
        assertEquals(expectedPostsList.get(0).getUser(), actualPostsList.get(0).getUser());
    }

    @Test
    void getAllPostsDtoWithUsersDto() {
        //given
        Integer pageNumber = 0;
        Integer pageSize = 10;
        Sort.Direction wayOfSort = Sort.Direction.ASC;

        List<Post> postList = testData.preparedPostsList();
        PostDtoWithAuthor postDtoWithAuthor = testData.preparedPostDtoWithAuthor();
        List<PostDtoWithAuthor> expectedPostDtoWithAuthorList = testData.preparedPostDtoWithAuthorList();

        when(postRepository.findAllPostsWithComments(PageRequest.of(pageNumber, pageSize, Sort.by(wayOfSort, "created")))).thenReturn(postList);
        when(postMapper.postToPostDtoWithAuthor(any(Post.class), any(User.class))).thenReturn(postDtoWithAuthor);

        //when
        List<PostDtoWithAuthor> actualPostDtoWithAuthorList = postService.getAllPostsDtoWithUsersDto(pageNumber, pageSize, wayOfSort);

        //then
        assertEquals(expectedPostDtoWithAuthorList.size(), actualPostDtoWithAuthorList.size());
        assertEquals(expectedPostDtoWithAuthorList.get(0).getBody(), actualPostDtoWithAuthorList.get(0).getBody());
        assertEquals(expectedPostDtoWithAuthorList.get(0).getUsername(), actualPostDtoWithAuthorList.get(0).getUsername());
    }

    @Test
    void editPost() throws PostNotFoundException, UserForbiddenAccessException, PostEmptyBodyException {
        //given
        Post post = testData.preparedPost();
        post.setBody("test edit post body");
        Integer postId = post.getId();

        PostBodyDto postBodyDto = testData.preparedEditPostDto();
        PostDtoWithAuthor expectedPostDtoWithAuthor = testData.preparedPostDtoWithAuthor();
        User user = testData.preparedUser();

        when(postRepository.findById(postId)).thenReturn(Optional.of(post));
        when(postRepository.save(post)).thenReturn(post);
        when(postMapper.postToPostDtoWithAuthor(post, user)).thenReturn(expectedPostDtoWithAuthor);

        //when
        PostDtoWithAuthor actualPostDtoWithAuthor = postService.editPost(postBodyDto, user, postId);

        //then
        assertEquals(expectedPostDtoWithAuthor.getBody(), actualPostDtoWithAuthor.getBody());
        assertEquals(expectedPostDtoWithAuthor.getUsername(), actualPostDtoWithAuthor.getUsername());
    }

    @Test
    void editPostWithUserForbiddenAccessException() throws PostNotFoundException, UserForbiddenAccessException, PostEmptyBodyException {
        //given
        Post post = testData.preparedPost();
        post.setBody("test edit post body");
        Integer postId = post.getId();
        User authorOfPost = testData.preparedAdmin();
        post.setUser(authorOfPost);

        PostBodyDto postBodyDto = testData.preparedEditPostDto();

        User user = testData.preparedUser();

        when(postRepository.findById(postId)).thenReturn(Optional.of(post));
        //when
        //then
        assertThrows(UserForbiddenAccessException.class, () -> postService.editPost(postBodyDto, user, postId),
                "Username: '" + authorOfPost.getUsername() + "' have not permission to edit post with id: '" + postId + " !");
    }

    @Test
    public void findPostById() throws PostNotFoundException {
        //Given
        Post expectedPost = testData.preparedPost();
        Integer postId = expectedPost.getId();
        when(postRepository.findById(postId)).thenReturn(Optional.of(expectedPost));
        //When
        Post actualPostById = postService.findPostById(postId);
        //Then
        assertEquals(expectedPost, actualPostById);
    }

    @Test
    public void findPostByIdWithPostNotFoundException() throws PostNotFoundException {
        //Given
        Integer incorrectPostId = 99999999;
        when(postRepository.findById(incorrectPostId)).thenReturn(Optional.ofNullable(null));
        //When
        //Then
        assertThrows(PostNotFoundException.class, () -> postService.findPostById(incorrectPostId),
                "Post with id: " + incorrectPostId + " doesn't found in database!");
    }

    @Test
    public void findPostDtoById() throws PostNotFoundException {
        //Given
        Post post = testData.preparedPost();
        Integer postId = post.getId();
        User user = testData.preparedUser();
        PostDtoWithAuthor expectedPostDtoWithAuthor = testData.preparedPostDtoWithAuthor();
        when(postRepository.findById(postId)).thenReturn(Optional.of(post));
        when(postMapper.postToPostDtoWithAuthor(post, user)).thenReturn(expectedPostDtoWithAuthor);
        //When
        PostDtoWithAuthor actualPostDtoWithAuthor = postService.findPostDtoById(postId);
        //Then
        assertEquals(expectedPostDtoWithAuthor.getBody(), actualPostDtoWithAuthor.getBody());
        assertEquals(expectedPostDtoWithAuthor.getCreated(), actualPostDtoWithAuthor.getCreated());
        assertEquals(expectedPostDtoWithAuthor.getUsername(), actualPostDtoWithAuthor.getUsername());
    }

    @Test
    public void findPostDtoByIdWithPostNotFoundException() throws PostNotFoundException {
        //Given
        Post post = testData.preparedPost();
        Integer incorrectPostId = 99999999;
        when(postRepository.findById(incorrectPostId)).thenReturn(Optional.ofNullable(null));
        //When
        //Then
        assertThrows(PostNotFoundException.class, () -> postService.findPostDtoById(incorrectPostId),
                "Post with id: " + incorrectPostId + " doesn't found in database!");
    }

    @Test
    void deletePostById() throws PostNotFoundException, UserForbiddenAccessException {
        //given
        Post post = testData.preparedPost();
        Integer postId = post.getId();
        User user = testData.preparedUser();
        List<Comment> commentList = testData.preparedCommentList();

        when(commentRepository.findAllCommentsByPostId(postId)).thenReturn(commentList);
        when(postRepository.findById(postId)).thenReturn(Optional.of(post));
        doNothing().when(commentRepository).deleteAll(commentList);
        doNothing().when(postRepository).deleteById(postId);

        //when
        postService.deletePostById(Optional.of(user), postId);

        //then
        verify(commentRepository, times(1)).deleteAll(commentList);
        verify(postRepository, times(1)).deleteById(postId);
    }

    @Test
    void deletePostByIdWithUserForbiddenAccessException() throws PostNotFoundException, UserForbiddenAccessException {
        //given
        Post post = testData.preparedPost();
        Integer postId = post.getId();
        User authorOfPost = testData.preparedAdmin();
        post.setUser(authorOfPost);
        List<Comment> commentList = testData.preparedCommentList();
        User user = testData.preparedUser();
        when(postRepository.findById(postId)).thenReturn(Optional.of(post));
        //when
        //then
        assertThrows(UserForbiddenAccessException.class, () -> postService.deletePostById(Optional.of(user), postId),
                "User with username '" + user.getUsername() + "' don't have permission to delete post with id: " + postId + " !!!!");
    }

    @Test
    public void findAllPostsByUserId() {
        //Given
        Integer userId = testData.preparedUser().getId();
        List<Post> expectedPostsList = testData.preparedPostsList();
        when(postRepository.findAllByUserId(userId)).thenReturn(Optional.ofNullable(expectedPostsList));
        //When
        List<Post> actualPostList = postService.findAllPostsByUserId(userId);
        //Then
        assertEquals(expectedPostsList.size(), actualPostList.size());
        assertEquals(expectedPostsList.get(0).getBody(), actualPostList.get(0).getBody());
        assertEquals(expectedPostsList.get(0).getUser(), actualPostList.get(0).getUser());
    }

    @Test
    public void findAllPostsByUserIdReturnNull() {
        //Given
        Integer userId = testData.preparedUser().getId();
        when(postRepository.findAllByUserId(userId)).thenReturn(Optional.ofNullable(null));
        //When
        List<Post> actualPostList = postService.findAllPostsByUserId(userId);
        //Then
        assertNull(actualPostList);
    }

    @Test
    public void getPostDtoListByBody() throws PostNotFoundException {
        //Given
        Post post = testData.preparedPost();
        List<PostDtoWithAuthor> expectedPostDtoWithAuthorList = testData.preparedPostDtoWithAuthorList();
        List<Post> postList = testData.preparedPostsList();
        PostDtoWithAuthor postDtoWithAuthor = testData.preparedPostDtoWithAuthor();
        User user = testData.preparedUser();
        String keywordInBody = "post";

        when(postRepository.findAllByBodyContaining(anyString())).thenReturn(postList);
        when(postMapper.postToPostDtoWithAuthor(any(Post.class), any(User.class))).thenReturn(postDtoWithAuthor);

        //When
        List<PostDtoWithAuthor> actualPostDtoWithAuthorList = postService.getPostDtoListByBody(keywordInBody);

        //Then
        assertEquals(expectedPostDtoWithAuthorList.size(), actualPostDtoWithAuthorList.size());
        assertEquals(expectedPostDtoWithAuthorList.get(0).getBody(), actualPostDtoWithAuthorList.get(0).getBody());
        assertEquals(expectedPostDtoWithAuthorList.get(0).getUsername(), actualPostDtoWithAuthorList.get(0).getUsername());
    }

    @Test
    public void getPostDtoListByBodyWithPostNotFoundException() throws PostNotFoundException {
        //Given
        String keywordInBody = "post";
        when(postRepository.findAllByBodyContaining(anyString())).thenReturn(Collections.emptyList());
        //When
        //Then
       assertThrows(PostNotFoundException.class, () -> postService.getPostDtoListByBody(keywordInBody),
               "Not found any post with keyword: '" + keywordInBody + "' in our database!");
    }

    @Test
    public void addOneLikeToPost() throws PostNotFoundException {
        //Given
        Post post = testData.preparedPost();
        Integer postId = post.getId();
        post.setLikeList(List.of(new Like(1, 1, 1, null)));
        User user = testData.preparedUser();
        when(userService.getLoginUser()).thenReturn(user);
        when(postRepository.findById(postId)).thenReturn(Optional.of(post));
        when(postRepository.save(post)).thenReturn(post);
        PostDtoWithAuthor expectedPostDtoWithAuthor = testData.preparedPostDtoWithAuthor();
        expectedPostDtoWithAuthor.setLikeList(post.getLikeList());
        when(postMapper.postToPostDtoWithAuthor(post, user)).thenReturn(expectedPostDtoWithAuthor);
        //When
        PostDtoWithAuthor actualPostDtoWithAuthor = postService.addOneLikeToPost(postId);
        //Then
        assertEquals(expectedPostDtoWithAuthor.getLikeList(), actualPostDtoWithAuthor.getLikeList());
        assertEquals(expectedPostDtoWithAuthor.getBody(), actualPostDtoWithAuthor.getBody());
        assertEquals(expectedPostDtoWithAuthor.getUsername(), actualPostDtoWithAuthor.getUsername());
    }

    @Test
    public void addOneLikeToPostWithPostNotFoundException() throws PostNotFoundException {
        //Given
        Post post = testData.preparedPost();
        Integer postId = post.getId();
        post.setLikeList(List.of(new Like(1, 1, 1, null)));
        User user = testData.preparedUser();
        when(userService.getLoginUser()).thenReturn(user);
        when(postRepository.findById(postId)).thenReturn(Optional.ofNullable(null));
        //When
        //Then
        assertThrows(PostNotFoundException.class, () -> postService.addOneLikeToPost(postId),
                "Post with id: '" + postId + "' not found in our database!");
    }

    @Test
    public void addOneDisLikeToPost() throws PostNotFoundException {
        //Given
        Post post = testData.preparedPost();
        Integer postId = post.getId();
        post.setDislikeList(List.of(new Dislike(1, 1, 1, null)));
        User user = testData.preparedUser();
        when(userService.getLoginUser()).thenReturn(user);
        when(postRepository.findById(postId)).thenReturn(Optional.of(post));
        when(postRepository.save(post)).thenReturn(post);
        PostDtoWithAuthor expectedPostDtoWithAuthor = testData.preparedPostDtoWithAuthor();
        expectedPostDtoWithAuthor.setDislikeList(post.getDislikeList());
        when(postMapper.postToPostDtoWithAuthor(post, user)).thenReturn(expectedPostDtoWithAuthor);
        //When
        PostDtoWithAuthor actualPostDtoWithAuthor = postService.addOneDisLikeToPost(postId);
        //Then
        assertEquals(expectedPostDtoWithAuthor.getDislikeList(), actualPostDtoWithAuthor.getDislikeList());
        assertEquals(expectedPostDtoWithAuthor.getBody(), actualPostDtoWithAuthor.getBody());
        assertEquals(expectedPostDtoWithAuthor.getUsername(), actualPostDtoWithAuthor.getUsername());
    }

    @Test
    public void addOneDisLikeToPostWithPostNotFoundException() throws PostNotFoundException {
        //Given
        Post post = testData.preparedPost();
        Integer postId = post.getId();
        post.setDislikeList(List.of(new Dislike(1, 1, 1, null)));
        User user = testData.preparedUser();
        when(userService.getLoginUser()).thenReturn(user);
        when(postRepository.findById(postId)).thenReturn(Optional.ofNullable(null));
        //When
        //Then
        assertThrows(PostNotFoundException.class, () -> postService.addOneDisLikeToPost(postId),
                "Post with id: '" + postId + "' not found in our database!");
    }

    @Test
    public void subtractOneCommentForNumberOfCommentForPostByPostId() throws PostNotFoundException {
        //Given
        Post post = testData.preparedPost();
        post.setNumberOfComments(3);
        Integer postId = post.getId();
        when(postRepository.findById(postId)).thenReturn(Optional.of(post));
        when(postRepository.save(post)).thenReturn(post);
        //When
        postService.subtractOneCommentForNumberOfCommentForPostByPostId(postId);
        //Then
        assertEquals(post.getNumberOfComments(), 2);
        verify( postRepository, times(1)).findById(postId);
    }

}
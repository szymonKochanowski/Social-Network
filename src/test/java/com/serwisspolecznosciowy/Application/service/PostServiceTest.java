package com.serwisspolecznosciowy.Application.service;

import com.serwisspolecznosciowy.Application.dto.DislikeDto;
import com.serwisspolecznosciowy.Application.dto.LikeDto;
import com.serwisspolecznosciowy.Application.dto.PostBodyDto;
import com.serwisspolecznosciowy.Application.dto.PostDto;
import com.serwisspolecznosciowy.Application.entity.*;
import com.serwisspolecznosciowy.Application.exception.*;
import com.serwisspolecznosciowy.Application.mappers.DislikeMapper;
import com.serwisspolecznosciowy.Application.mappers.LikeMapper;
import com.serwisspolecznosciowy.Application.mappers.PostMapper;
import com.serwisspolecznosciowy.Application.repository.CommentRepository;
import com.serwisspolecznosciowy.Application.repository.DislikeRepository;
import com.serwisspolecznosciowy.Application.repository.LikeRepository;
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

    @Mock
    private LikeRepository likeRepository;

    @Autowired
    public TestData testData;

    @Mock
    private DislikeRepository dislikeRepository;

    @Mock
    private LikeMapper likeMapper;

    @Mock
    private DislikeMapper dislikeMapper;

    @Test
    void addNewPost() throws PostEmptyBodyException, UserNotFoundException {
        //given
        User user = testData.preparedUser();
        Post post = testData.preparedPost();
        PostDto expectedPost = testData.preparedPostDto();
        PostBodyDto postBodyDto = testData.preparedPostBodyDto();

        when(userService.getLoginUser()).thenReturn(user);
        when(postRepository.save(any(Post.class))).thenReturn(post);
        when(postMapper.postToPostDto(any(Post.class), any(User.class), any(), any())).thenReturn(expectedPost);

        //when
        PostDto actualPost = postService.addNewPost(postBodyDto);

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
    void getAllPosts() {
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
    void getAllPostsDto() {
        //given
        Integer pageNumber = 0;
        Integer pageSize = 10;
        Sort.Direction wayOfSort = Sort.Direction.ASC;

        List<Post> postList = testData.preparedPostsList();
        PostDto postDto = testData.preparedPostDto();
        List<PostDto> expectedPostDtoList = testData.preparedPostDtoWithAuthorList();
        List<Like> likeList = testData.preparedPost().getLikeList();
        List<LikeDto> likeDtoList = testData.preparedLikeDtoList();

        when(postRepository.findAllPostsWithComments(PageRequest.of(pageNumber, pageSize, Sort.by(wayOfSort, "created")))).thenReturn(postList);
        when(likeMapper.likeListToLikeDtoList(likeList)).thenReturn(likeDtoList);
        when(postMapper.postToPostDto(any(Post.class), any(User.class), any(), any())).thenReturn(postDto);

        //when
        List<PostDto> actualPostDtoList = postService.getAllPostsDto(pageNumber, pageSize, wayOfSort);

        //then
        assertEquals(expectedPostDtoList.size(), actualPostDtoList.size());
        assertEquals(expectedPostDtoList.get(0).getBody(), actualPostDtoList.get(0).getBody());
        assertEquals(expectedPostDtoList.get(0).getUsername(), actualPostDtoList.get(0).getUsername());
    }

    @Test
    void editPost() throws PostNotFoundException, UserForbiddenAccessException, PostEmptyBodyException {
        //given
        Post post = testData.preparedPost();
        post.setBody("test edit post body");
        Integer postId = post.getId();

        PostBodyDto postBodyDto = testData.preparedEditPostDto();
        postBodyDto.setBody(post.getBody());
        PostDto expectedPostDto = testData.preparedPostDto();
        expectedPostDto.setBody(postBodyDto.getBody());
        User user = testData.preparedUser();
        List<Like> likeList = Collections.emptyList();
        List<LikeDto> likeDtoList = Collections.emptyList();
        List<Dislike> dislikeList = Collections.emptyList();
        List<DislikeDto> dislikeDtoList = Collections.emptyList();

        when(postRepository.findById(postId)).thenReturn(Optional.of(post));
        when(postRepository.save(post)).thenReturn(post);
        when(likeMapper.likeListToLikeDtoList(likeList)).thenReturn(likeDtoList);
        when(dislikeMapper.dislikeListToDislikeDtoList(dislikeList)).thenReturn(dislikeDtoList);
        when(postMapper.postToPostDto(post, user, likeDtoList, dislikeDtoList)).thenReturn(expectedPostDto);

        //when
        PostDto actualPostDto = postService.editPost(postBodyDto, user, postId);

        //then
        assertEquals(expectedPostDto.getBody(), actualPostDto.getBody());
        assertEquals(expectedPostDto.getUsername(), actualPostDto.getUsername());
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
    void findPostById() throws PostNotFoundException {
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
    void findPostByIdWithPostNotFoundException() throws PostNotFoundException {
        //Given
        Integer incorrectPostId = 99999999;
        when(postRepository.findById(incorrectPostId)).thenReturn(Optional.ofNullable(null));
        //When
        //Then
        assertThrows(PostNotFoundException.class, () -> postService.findPostById(incorrectPostId),
                "Post with id: " + incorrectPostId + " doesn't found in database!");
    }

    @Test
    void findPostDtoById() throws PostNotFoundException {
        //Given
        Post post = testData.preparedPost();
        Integer postId = post.getId();
        User user = testData.preparedUser();
        PostDto expectedPostDto = testData.preparedPostDto();
        List<LikeDto> likeDtoList = testData.preparedLikeDtoList();
        List<DislikeDto> dislikeDtoList = testData.preparedDislikeDtoList();

        when(postRepository.findById(postId)).thenReturn(Optional.of(post));
        when(likeMapper.likeListToLikeDtoList(post.getLikeList())).thenReturn(likeDtoList);
        when(dislikeMapper.dislikeListToDislikeDtoList(post.getDislikeList())).thenReturn(dislikeDtoList);
        when(postMapper.postToPostDto(post, user, likeDtoList, dislikeDtoList)).thenReturn(expectedPostDto);

        //When
        PostDto actualPostDto = postService.findPostDtoById(postId);

        //Then
        assertEquals(expectedPostDto.getBody(), actualPostDto.getBody());
        assertEquals(expectedPostDto.getCreated(), actualPostDto.getCreated());
        assertEquals(expectedPostDto.getUsername(), actualPostDto.getUsername());
    }

    @Test
    void findPostDtoByIdWithPostNotFoundException() throws PostNotFoundException {
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
    void findAllPostsByUserId() {
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
    void findAllPostsByUserIdReturnEmptyList() {
        //Given
        Integer userId = testData.preparedUser().getId();
        when(postRepository.findAllByUserId(userId)).thenReturn(Optional.ofNullable(null));
        //When
        List<Post> actualPostList = postService.findAllPostsByUserId(userId);
        //Then
        assertEquals(Collections.emptyList(), actualPostList);
    }

    @Test
    void getPostDtoListByBody() throws PostNotFoundException {
        //Given
        Post post = testData.preparedPost();
        List<PostDto> expectedPostDtoList = testData.preparedPostDtoWithAuthorList();
        List<Post> postList = testData.preparedPostsList();
        PostDto postDto = testData.preparedPostDto();
        User user = testData.preparedUser();
        String keywordInBody = "post";

        when(postRepository.findAllByBodyContaining(anyString())).thenReturn(postList);
        when(postMapper.postToPostDto(any(Post.class), any(User.class), any(), any())).thenReturn(postDto);

        //When
        List<PostDto> actualPostDtoList = postService.getPostDtoListByBody(keywordInBody);

        //Then
        assertEquals(expectedPostDtoList.size(), actualPostDtoList.size());
        assertEquals(expectedPostDtoList.get(0).getBody(), actualPostDtoList.get(0).getBody());
        assertEquals(expectedPostDtoList.get(0).getUsername(), actualPostDtoList.get(0).getUsername());
    }

    @Test
    void getPostDtoListByBodyWithPostNotFoundException() throws PostNotFoundException {
        //Given
        String keywordInBody = "post";
        when(postRepository.findAllByBodyContaining(anyString())).thenReturn(Collections.emptyList());
        //When
        //Then
        assertThrows(PostNotFoundException.class, () -> postService.getPostDtoListByBody(keywordInBody),
                "Not found any post with keyword: '" + keywordInBody + "' in our database!");
    }

    @Test
    void addOneLikeToPost() throws PostNotFoundException {
        //Given
        User user = testData.preparedUser();
        when(userService.getLoginUser()).thenReturn(user);

        Post post = testData.preparedPost();
        Integer postId = post.getId();
        when(postRepository.findById(postId)).thenReturn(Optional.of(post));

        Like like = testData.preparedLike();
        List<Like> likeList = testData.preparedLikeList();
        List<LikeDto> likeDtoList = testData.preparedLikeDtoList();
        List<DislikeDto> dislikeDtoList = testData.preparedDislikeDtoList();
        when(likeRepository.save(like)).thenReturn(like);
        when(likeMapper.likeListToLikeDtoList(likeList)).thenReturn(likeDtoList);

        when(postRepository.save(post)).thenReturn(post);

        PostDto expectedPostDto = testData.preparedPostDto();
        expectedPostDto.setLikeDtoList(likeDtoList);
        when(postMapper.postToPostDto(post, user, likeDtoList, dislikeDtoList)).thenReturn(expectedPostDto);

        //When
        postService.addOneLikeToPost(postId);

        //Then
        verify(postRepository, times(1)).save(post);
    }

    @Test
    void addOneLikeToPostWithPostNotFoundException() throws PostNotFoundException {
        //Given
        Post post = testData.preparedPost();
        Integer postId = post.getId();
        post.setLikeList(List.of(new Like(1, 1, 1, null, "test")));
        User user = testData.preparedUser();
        when(userService.getLoginUser()).thenReturn(user);
        when(postRepository.findById(postId)).thenReturn(Optional.ofNullable(null));
        //When
        //Then
        assertThrows(PostNotFoundException.class, () -> postService.addOneLikeToPost(postId),
                "Post with id: '" + postId + "' not found in our database!");
    }

    @Test
    void addOneLikeToPostReturnDuplicateUsernameException() throws PostNotFoundException {
        //Given
        User user = testData.preparedUser();
        when(userService.getLoginUser()).thenReturn(user);

        Post post = testData.preparedPost();
        Integer postId = post.getId();
        when(postRepository.findById(postId)).thenReturn(Optional.of(post));

        List<Like> likeList = testData.preparedLikeList();
        post.setLikeList(likeList);

        //When
        //Then
        assertThrows(DuplicateUsernameException.class, () -> postService.addOneLikeToPost(postId),
                "User can add only once like to specified post!");
    }

    @Test
    void addOneDisLikeToPost() throws PostNotFoundException {
        //Given
        Post post = testData.preparedPost();
        Integer postId = post.getId();
        User user = testData.preparedUser();
        Dislike dislike = testData.preparedDislike();
        List<LikeDto> likeDtoList = testData.preparedLikeDtoList();

        when(userService.getLoginUser()).thenReturn(user);
        when(postRepository.findById(postId)).thenReturn(Optional.of(post));
        when(dislikeRepository.save(dislike)).thenReturn(dislike);
        when(postRepository.save(post)).thenReturn(post);

        PostDto expectedPostDto = testData.preparedPostDto();
        List<DislikeDto> dislikeDtoList = testData.preparedDislikeDtoList();
        expectedPostDto.setDislikeDtoList(dislikeDtoList);

        when(postMapper.postToPostDto(post, user, likeDtoList, dislikeDtoList)).thenReturn(expectedPostDto);

        //When
        postService.addOneDisLikeToPost(postId);

        //Then
        verify(postRepository, times(1)).save(post);
    }

    @Test
    void addOneDisLikeToPostWithPostNotFoundException() throws PostNotFoundException {
        //Given
        Post post = testData.preparedPost();
        Integer postId = post.getId();
        post.setDislikeList(List.of(new Dislike(1, 1, 1, null, "test")));
        User user = testData.preparedUser();
        when(userService.getLoginUser()).thenReturn(user);
        when(postRepository.findById(postId)).thenReturn(Optional.ofNullable(null));
        //When
        //Then
        assertThrows(PostNotFoundException.class, () -> postService.addOneDisLikeToPost(postId),
                "Post with id: '" + postId + "' not found in our database!");
    }

    @Test
    void addOneDisLikeToPostReturnDuplicateUsernameException() throws PostNotFoundException {
        //Given
        Post post = testData.preparedPost();
        Integer postId = post.getId();
        User user = testData.preparedUser();
        List<Dislike> dislikeList = testData.preparedDislikeList();
        post.setDislikeList(dislikeList);

        when(userService.getLoginUser()).thenReturn(user);
        when(postRepository.findById(postId)).thenReturn(Optional.of(post));

        //When
        //Then
        assertThrows(DuplicateUsernameException.class, () -> postService.addOneDisLikeToPost(postId),
                "User can add only once dislike to specified post!");
    }

    @Test
    void subtractOneCommentForNumberOfCommentForPostByPostId() throws PostNotFoundException {
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
        verify(postRepository, times(1)).findById(postId);
    }

    @Test
    void getNumberOfLikesByPostId() throws PostNotFoundException {
        //Given
        Post post = testData.preparedPost();
        int postId = post.getId();
        List<Like> expectedLikeList = testData.preparedLikeList();
        post.setLikeList(expectedLikeList);
        when(postRepository.findById(postId)).thenReturn(Optional.of(post));
        when(likeRepository.findByPostLikeId(postId)).thenReturn(expectedLikeList);

        //When
        Integer actualNumberOfLikes = postService.getNumberOfLikesByPostId(postId);

        //Then
        assertEquals(expectedLikeList.size(), actualNumberOfLikes);
    }

    @Test
    void getNumberOfDislikesByPostId() throws PostNotFoundException {
        //Given
        Post post = testData.preparedPost();
        int postId = post.getId();
        List<Dislike> expectedDislikeList = testData.preparedDislikeList();
        post.setDislikeList(expectedDislikeList);
        when(postRepository.findById(postId)).thenReturn(Optional.of(post));
        when(dislikeRepository.findByPostDislikeId(postId)).thenReturn(expectedDislikeList);

        //When
        Integer actualNumberOfDislikes = postService.getNumberOfDislikesByPostId(postId);

        //Then
        assertEquals(expectedDislikeList.size(), actualNumberOfDislikes);
    }

}
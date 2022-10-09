package com.serwisspolecznosciowy.Application.service;

import com.serwisspolecznosciowy.Application.dto.DislikeDto;
import com.serwisspolecznosciowy.Application.dto.LikeDto;
import com.serwisspolecznosciowy.Application.dto.PostBodyDto;
import com.serwisspolecznosciowy.Application.dto.PostDto;
import com.serwisspolecznosciowy.Application.entity.*;
import com.serwisspolecznosciowy.Application.exception.DuplicateUsernameException;
import com.serwisspolecznosciowy.Application.exception.PostEmptyBodyException;
import com.serwisspolecznosciowy.Application.exception.PostNotFoundException;
import com.serwisspolecznosciowy.Application.exception.UserForbiddenAccessException;
import com.serwisspolecznosciowy.Application.mappers.DislikeMapper;
import com.serwisspolecznosciowy.Application.mappers.LikeMapper;
import com.serwisspolecznosciowy.Application.mappers.PostMapper;
import com.serwisspolecznosciowy.Application.repository.CommentRepository;
import com.serwisspolecznosciowy.Application.repository.DislikeRepository;
import com.serwisspolecznosciowy.Application.repository.LikeRepository;
import com.serwisspolecznosciowy.Application.repository.PostRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class PostService {

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private PostMapper postMapper;

    @Autowired
    private UserService userService;

    @Autowired
    private LikeRepository likeRepository;

    @Autowired
    private DislikeRepository dislikeRepository;

    @Autowired
    LikeMapper likeMapper;

    @Autowired
    DislikeMapper dislikeMapper;


    public PostDto addNewPost(PostBodyDto postBodyDto) throws PostEmptyBodyException {
        User loginUser = userService.getLoginUser();
        Post post = new Post();
        String body = postBodyDto.getBody();
        isPostBodyIsNotBlank(body);
        post.setBody(body);
        post.setCreated(LocalDateTime.now());
        post.setUser(loginUser);
        post.setLikeList(Collections.emptyList());
        post.setDislikeList(Collections.emptyList());
        post.setNumberOfComments(0);
        postRepository.save(post);
        return postMapper.postToPostDto(post, loginUser, Collections.emptyList(), Collections.emptyList());
    }

    private boolean isPostBodyIsNotBlank(String postBody) throws PostEmptyBodyException {
        if (postBody.isBlank()) {
            log.error("Error in method: isPostBodyIsNotBlank! Post body can not be empty!");
            throw new PostEmptyBodyException("Post body cannot be empty!");
        } else {
            return true;
        }
    }

    @Cacheable(cacheNames = "PostsWithComments")
    public List<Post> getAllPostsWithComments(Integer pageNumber, Integer pageSize, Sort.Direction sort) {
        return postRepository.findAllPostsWithComments(PageRequest.of(pageNumber, pageSize, Sort.by(sort, "created")));
    }

    @Cacheable(cacheNames = "AllPostsDto")
    public List<PostDto> getAllPostsDto(Integer pageNumber, Integer pageSize, Sort.Direction wayOfSort) {
        List<PostDto> postsListDto = new ArrayList<>();
        List<Post> postList = postRepository.findAllPostsWithComments(PageRequest.of(pageNumber, pageSize, Sort.by(wayOfSort, "created")));
        for (Post post : postList) {
            User user = post.getUser();
            PostDto postDto = postMapper.postToPostDto(post, user, likeMapper.likeListToLikeDtoList(post.getLikeList()), dislikeMapper.dislikeListToDislikeDtoList(post.getDislikeList()));
            postsListDto.add(postDto);
        }
        return postsListDto;
    }

    public PostDto editPost(PostBodyDto postBodyDto, User userFromDb, Integer postId) throws PostNotFoundException, UserForbiddenAccessException, PostEmptyBodyException {
        Post postToEdit = findPostById(postId);
        String body = postBodyDto.getBody();
        if (isPostWasCreatedByLoginUserOrUserHaveRoleAdmin(userFromDb, postToEdit)) {
            isPostBodyIsNotBlank(body);
            postToEdit.setBody(body);
            postToEdit.setUpdated(LocalDateTime.now());
            postRepository.save(postToEdit);

        } else {
            log.error("Error in method editPost. Username: {} have not permission to edit post with id: {}", userFromDb.getUsername(), postId + "!");
            throw new UserForbiddenAccessException("Username: '" + userFromDb.getUsername() + "' have not permission to edit post with id: '" + postId + " !");
        }
        return postMapper.postToPostDto(postToEdit, userFromDb, likeMapper.likeListToLikeDtoList(postToEdit.getLikeList()), dislikeMapper.dislikeListToDislikeDtoList(postToEdit.getDislikeList()));
    }

    public Post findPostById(Integer postId) throws PostNotFoundException {
        Optional<Post> optionalPost = postRepository.findById(postId);
        if (optionalPost.isPresent()) {
            return optionalPost.get();
        } else {
            log.error("Error in method: findPostById! Post with id: " + postId + " doesn't found in database!");
            throw new PostNotFoundException("Post with id: " + postId + " doesn't found in database!");
        }
    }

    public PostDto findPostDtoById(Integer id) throws PostNotFoundException {
        Optional<Post> optionalPost = postRepository.findById(id);
        if (optionalPost.isPresent()) {
            Post post = optionalPost.get();
            return postMapper.postToPostDto(post, post.getUser(), likeMapper.likeListToLikeDtoList(post.getLikeList()), dislikeMapper.dislikeListToDislikeDtoList(post.getDislikeList()));
        } else {
            log.error("Error in method: getPostDtoById! Post with id: " + id + " doesn't found in database!");
            throw new PostNotFoundException("Post with id: " + id + " doesn't found in database!");
        }
    }

    private boolean isPostWasCreatedByLoginUserOrUserHaveRoleAdmin(User user, Post post) {
        return user.getUsername().equals(post.getUser().getUsername()) || user.getRole().equals("ROLE_ADMIN");
    }

    public void deletePostById(Optional<User> user, Integer postId) throws PostNotFoundException, UserForbiddenAccessException {
        Post post = findPostById(postId);
        User userFromDb = user.get();
            if (isPostWasCreatedByLoginUserOrUserHaveRoleAdmin(userFromDb, post)) {
                /* delete all comments belongs to specific post */
                List<Comment> commentList = commentRepository.findAllCommentsByPostId(postId);
                commentRepository.deleteAll(commentList);
                /* delete specific post */
                postRepository.deleteById(postId);
            } else {
                log.error("Error in method: deletePostById! User with username '" + userFromDb.getUsername()
                        + "' don't have permission to delete post with id: " + postId + " !!!!");
                throw new UserForbiddenAccessException("User with username '" + userFromDb.getUsername()
                        + "' don't have permission to delete post with id: " + postId + " !!!!");
            }
    }

    public List<Post> findAllPostsByUserId(Integer userId) {
        Optional<List<Post>> optionalPostList = postRepository.findAllByUserId(userId);
        if (optionalPostList.isPresent()) {
            return optionalPostList.get();
        }
        return null;
    }

    public List<PostDto> getPostDtoListByBody(String keywordInBody) throws PostNotFoundException {
        List<PostDto> postsListDto = new ArrayList<>();
        List<Post> postList = postRepository.findAllByBodyContaining(keywordInBody);
        if (!postList.isEmpty()) {
            for (Post post : postList) {
                User user = post.getUser();
                PostDto postDto = postMapper.postToPostDto(post, user, likeMapper.likeListToLikeDtoList(post.getLikeList()), dislikeMapper.dislikeListToDislikeDtoList(post.getDislikeList()));
                postsListDto.add(postDto);
            }
            return postsListDto;
        } else {
            log.error("Error in method: getPostByBody! Not found any post with keyword: '" + keywordInBody + "' in our database!");
            throw new PostNotFoundException("Not found any post with keyword: '" + keywordInBody + "' in our database!");
        }
    }

    public PostDto addOneLikeToPost(Integer postId) throws PostNotFoundException {
        User user = userService.getLoginUser();
        Optional<Post> optionalPost = postRepository.findById(postId);
        if (optionalPost.isPresent()) {
            Post post = optionalPost.get();
            checkUserNotAlreadyAddOneLikeToPost(user, post);
            Like like = new Like();
            like.setUserId(user.getId());
            like.setPostLikeId(post.getId());
            like.setUsername(user.getUsername());
            likeRepository.save(like);

            List<Like> likeList = post.getLikeList();
            likeList.add(like);
            List<LikeDto> likeDtoList = likeMapper.likeListToLikeDtoList(likeList);

            List<Dislike> dislikeList = post.getDislikeList();
            List<DislikeDto> dislikeDtoList = dislikeMapper.dislikeListToDislikeDtoList(dislikeList);
            return postMapper.postToPostDto(postRepository.save(post), user, likeDtoList, dislikeDtoList);
        } else {
            log.error("Error in method: addOneLikeToPost! Post with id: '" + postId + "' not found in our database!");
            throw new PostNotFoundException("Post with id: '" + postId + "' not found in our database!");
        }
    }

    private void checkUserNotAlreadyAddOneLikeToPost(User user, Post post) {
        if (post.getLikeList().stream().anyMatch(currentLike -> currentLike.getUserId().equals(user.getId()))) {
            log.error("Error in method checkUserNotAlreadyAddOneLikeToPost! User can add only once like to specified post!");
            throw new DuplicateUsernameException("User can add only once like to specified post!");
        }
    }

    public PostDto addOneDisLikeToPost(Integer postId) throws PostNotFoundException {
        User user = userService.getLoginUser();
        Optional<Post> optionalPost = postRepository.findById(postId);
        if (optionalPost.isPresent()) {
            Post post = optionalPost.get();
            checkIfUserNotAlreadyAddOneDislikeToPost(user, post);
            Dislike dislike = new Dislike();
            dislike.setPostDislikeId(post.getId());
            dislike.setUserId(user.getId());
            dislike.setUsername(user.getUsername());
            dislikeRepository.save(dislike);

            List<Like> likeList = post.getLikeList();
            List<LikeDto> likeDtoList = likeMapper.likeListToLikeDtoList(likeList);

            List<Dislike> dislikeList = post.getDislikeList();
            dislikeList.add(dislike);
            List<DislikeDto> dislikeDtoList = dislikeMapper.dislikeListToDislikeDtoList(dislikeList);
            return postMapper.postToPostDto(postRepository.save(post), user, likeDtoList, dislikeDtoList);
        } else {
            log.error("Error in method: addOneDisLikeToPost! Post with id: '" + postId + "' not found in our database!");
            throw new PostNotFoundException("Post with id: '" + postId + "' not found in our database!");
        }
    }

    private void checkIfUserNotAlreadyAddOneDislikeToPost(User user, Post post) {
        if (post.getDislikeList().stream().anyMatch(userInDislikeList -> userInDislikeList.getUserId().equals(user.getId()))) {
            log.error("Error in method addOneDisLikeToPost! User can add only once dislike to specified post!");
            throw new DuplicateUsernameException("User can add only once dislike to specified post!");
        }
    }

    public void subtractOneCommentForNumberOfCommentForPostByPostId(Integer postId) throws PostNotFoundException {
        Post postById = findPostById(postId);
        postById.setNumberOfComments(postById.getNumberOfComments() - 1);
    }

    public Integer getNumberOfLikesByPostId(Integer postId) throws PostNotFoundException {
        findPostById(postId);
        List<Like> postLikeList = likeRepository.findByPostLikeId(postId);
        return postLikeList.size();
    }

    public Integer getNumberOfDislikesByPostId(Integer postId) throws PostNotFoundException {
        findPostById(postId);
        List<Dislike> postDislikeList = dislikeRepository.findByPostDislikeId(postId);
        return postDislikeList.size();
    }

}

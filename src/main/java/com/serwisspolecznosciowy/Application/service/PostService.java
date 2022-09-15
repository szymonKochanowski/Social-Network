package com.serwisspolecznosciowy.Application.service;

import com.serwisspolecznosciowy.Application.dto.PostBodyDto;
import com.serwisspolecznosciowy.Application.dto.PostDtoWithAuthor;
import com.serwisspolecznosciowy.Application.entity.Comment;
import com.serwisspolecznosciowy.Application.entity.Post;
import com.serwisspolecznosciowy.Application.entity.User;
import com.serwisspolecznosciowy.Application.exception.PostEmptyBodyException;
import com.serwisspolecznosciowy.Application.exception.PostNotFoundException;
import com.serwisspolecznosciowy.Application.exception.UserForbiddenAccessException;
import com.serwisspolecznosciowy.Application.mappers.PostMapper;
import com.serwisspolecznosciowy.Application.repository.CommentRepository;
import com.serwisspolecznosciowy.Application.repository.PostRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class PostService {

    @Autowired
    PostRepository postRepository;

    @Autowired
    CommentRepository commentRepository;

    @Autowired
    PostMapper postMapper;

    @Autowired
    UserService userService;


    public PostDtoWithAuthor addNewPost(PostBodyDto postBodyDto) throws PostEmptyBodyException {
        User loginUser = userService.getLoginUser();
        Post post = new Post();
        String body = postBodyDto.getBody();
        if (isPostBodyIsNotBlank(body)) {
            post.setBody(body);
        }
        post.setCreated(LocalDateTime.now());
        post.setUser(loginUser);
        post.setNumberOfLikes(0);
        post.setNumberOfDislikes(0);
        post.setNumberOfComments(0);
        postRepository.save(post);
        return postMapper.postToPostDtoWithAuthor(post, loginUser);
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
    public List<PostDtoWithAuthor> getAllPostsDtoWithUsersDto(Integer pageNumber, Integer pageSize, Sort.Direction wayOfSort) {
        List<PostDtoWithAuthor> postsListDto = new ArrayList<>();
        List<Post> postList = postRepository.findAllPostsWithComments(PageRequest.of(pageNumber, pageSize, Sort.by(wayOfSort, "created")));
        for (Post post : postList) {
            User user = post.getUser();
            PostDtoWithAuthor postDtoWithAuthor = postMapper.postToPostDtoWithAuthor(post, user);
            postsListDto.add(postDtoWithAuthor);
        }
        return postsListDto;
    }

    public PostDtoWithAuthor editPost(PostBodyDto postBodyDto, User userFromDb, Integer postId) throws PostNotFoundException, UserForbiddenAccessException, PostEmptyBodyException {
        Post postToEdit = findPostById(postId);
        String body = postBodyDto.getBody();
        if (isPostWasCreatedByLoginUserOrUserHaveRoleAdmin(userFromDb, postToEdit)) {
            if (isPostBodyIsNotBlank(body)) {
                postToEdit.setBody(body);
                postToEdit.setUpdated(LocalDateTime.now());
                postRepository.save(postToEdit);
            }
        } else {
            log.error("Error in method editPost. Username: {} have not permission to edit post with id: {}", userFromDb.getUsername(), postId + "!");
            throw new UserForbiddenAccessException("Username: '" + userFromDb.getUsername() + "' have not permission to edit post with id: '" + postId + " !");
        }
        return postMapper.postToPostDtoWithAuthor(postToEdit, userFromDb);
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

    public PostDtoWithAuthor findPostDtoById(Integer id) throws PostNotFoundException {
        Optional<Post> optionalPost = postRepository.findById(id);
        if (optionalPost.isPresent()) {
            Post post = optionalPost.get();
            return postMapper.postToPostDtoWithAuthor(post, post.getUser());
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
            List<Post> postList = optionalPostList.get();
            return postList;
        }
        return null;
    }

    public List<PostDtoWithAuthor> getPostDtoListByBody(String keywordInBody) throws PostNotFoundException {
        List<PostDtoWithAuthor> postsListDto = new ArrayList<>();
        List<Post> postList = postRepository.findAllByBodyContaining(keywordInBody);
        if (!postList.isEmpty()) {
            for (Post post : postList) {
                User user = post.getUser();
                PostDtoWithAuthor postDtoWithAuthor = postMapper.postToPostDtoWithAuthor(post, user);
                postsListDto.add(postDtoWithAuthor);
            }
            return postsListDto;
        } else {
            log.error("Error in method: getPostByBody! Not found any post with keyword: '" + keywordInBody + "' in our database!");
            throw new PostNotFoundException("Not found any post with keyword: '" + keywordInBody + "' in our database!");
        }
    }

    public PostDtoWithAuthor addOneLikeToPost(Integer postId) throws PostNotFoundException {
        User user = userService.getLoginUser();
        Optional<Post> optionalPost = postRepository.findById(postId);
        if (optionalPost.isPresent()) {
            Post post = optionalPost.get();
            post.setNumberOfLikes(post.getNumberOfLikes() + 1);
            return postMapper.postToPostDtoWithAuthor(postRepository.save(post), user);
        } else {
            log.error("Error in method: addOneLikeToPost! Post with id: '" + postId + "' not found in our database!");
            throw new PostNotFoundException("Post with id: '" + postId + "' not found in our database!");
        }
    }

    public PostDtoWithAuthor addOneDisLikeToPost(Integer postId) throws PostNotFoundException {
        User user = userService.getLoginUser();
        Optional<Post> optionalPost = postRepository.findById(postId);
        if (optionalPost.isPresent()) {
            Post post = optionalPost.get();
            post.setNumberOfDislikes((post.getNumberOfDislikes() == null || post.getNumberOfDislikes() < 0) ? 0 : post.getNumberOfDislikes() + 1);
            return postMapper.postToPostDtoWithAuthor(postRepository.save(post), user);
        } else {
            log.error("Error in method: addOneDisLikeToPost! Post with id: '" + postId + "' not found in our database!");
            throw new PostNotFoundException("Post with id: '" + postId + "' not found in our database!");
        }
    }

    public void subtractOneCommentForNumberOfCommentForPostByPostId(Integer postId) throws PostNotFoundException {
        Post postById = findPostById(postId);
        postById.setNumberOfComments(postById.getNumberOfComments() - 1);
    }
}

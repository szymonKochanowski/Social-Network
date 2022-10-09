package com.serwisspolecznosciowy.Application.service;

import com.serwisspolecznosciowy.Application.dto.CommentBodyDto;
import com.serwisspolecznosciowy.Application.dto.CommentDto;
import com.serwisspolecznosciowy.Application.entity.Comment;
import com.serwisspolecznosciowy.Application.entity.Post;
import com.serwisspolecznosciowy.Application.entity.User;
import com.serwisspolecznosciowy.Application.exception.CommentEmptyBodyException;
import com.serwisspolecznosciowy.Application.exception.CommentNotFoundException;
import com.serwisspolecznosciowy.Application.exception.PostNotFoundException;
import com.serwisspolecznosciowy.Application.exception.UserForbiddenAccessException;
import com.serwisspolecznosciowy.Application.mappers.CommentMapper;
import com.serwisspolecznosciowy.Application.repository.CommentRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class CommentService {

    @Autowired
    CommentRepository commentRepository;

    @Autowired
    CommentMapper commentMapper;

    @Autowired
    UserService userService;

    @Autowired
    PostService postService;

    public CommentDto addNewComment(Integer postId, CommentBodyDto commentBodyDto) throws PostNotFoundException {
        User loginUser = userService.getLoginUser();
        Comment comment = new Comment();
        String commentBody = commentBodyDto.getBody();
        isCommentBodyIsNotBlank(commentBody);
        comment.setBody(commentBody);
        comment.setCreated(LocalDateTime.now());
        comment.setPostId(postId);
        comment.setUser(loginUser);
        comment.setLikeList(null);
        comment.setDislikeList(null);
        commentRepository.save(comment);

        Post postById = postService.findPostById(postId);
        postById.setNumberOfComments(postById.getNumberOfComments() + 1);

        log.info("New comment with body: '" + commentBody + "' added to database.");
        return commentMapper.commentToCommentDto(comment);
    }

    private boolean isCommentBodyIsNotBlank(String commentBody) {
        if (commentBody.isBlank()) {
            log.error("Error in method: isCommentBodyIsNotBlank. Comment body can not be empty!");
            throw new CommentEmptyBodyException("Comment body cannot be empty!");
        } else {
            return true;
        }
    }

    @Cacheable(cacheNames = "AllComments")
    public List<Comment> getAllComments(Integer pageNumber, Integer pageSize, Sort.Direction sort) {
        return commentRepository.findAllComments(PageRequest.of(pageNumber, pageSize, Sort.by(sort, "created")));
    }

    @Cacheable(cacheNames = "AllCommentsDto")
    public List<CommentDto> getAllCommentsDto(Integer pageNumber, Integer pageSize, Sort.Direction wayOfSort) {
        return commentMapper.commentListToCommentDtoList(commentRepository.findAllComments(PageRequest.of(pageNumber, pageSize, Sort.by(wayOfSort, "created"))));
    }

    public Comment getCommentById(Integer id) throws CommentNotFoundException {
        Optional<Comment> optionalComment = commentRepository.findById(id);
        if (optionalComment.isPresent()) {
            return optionalComment.get();
        } else {
            log.error("Comment with id: " + id + " not found!");
            throw new CommentNotFoundException("Comment with id: " + id + " not found in our database!");
        }
    }

    public CommentDto getCommentDtoById(Integer id) throws CommentNotFoundException {
        Comment comment = commentRepository.findById(id).orElseThrow(() -> new CommentNotFoundException("Comment with id: '" + id + "' not found in our database!"));
        return commentMapper.commentToCommentDto(comment);
    }

    public void deleteCommentById(Integer commentId) throws CommentNotFoundException, UserForbiddenAccessException {
        Comment commentFromDb = getCommentById(commentId);
        User loginUser = userService.getLoginUser();
        if (isCommentWasCreatedByLoginUserOrUserHaveRoleAdmin(loginUser, commentFromDb)) {
            commentRepository.deleteById(commentId);
        } else {
            log.error("Error in method: deleteCommentById. User with id: " + loginUser.getId() + " has no permission to delete comment with id: " + commentId + "!");
            throw new UserForbiddenAccessException("You are not authorized to delete this comment!");
        }
    }

    public List<Comment> findAllCommentByUserId(Integer userId) {
        Optional<List<Comment>> optionalCommentList = commentRepository.findAllCommentByUserId(userId);
        if (optionalCommentList.isPresent()) {
            List<Comment> commentList = optionalCommentList.get();
            return commentList;
        }
        return null;
    }

    public List<Comment> getCommentsByBody(String body) throws CommentNotFoundException {
        List<Comment> commentList = commentRepository.findAllByBodyContaining(body);
        if (!commentList.isEmpty()) {
            return commentList;
        } else {
            log.error("Error in method: getCommentsByBody. Not found any comment with  keyword: '" + body  + "' in our database!");
            throw new CommentNotFoundException("Not found any comment with keyword: '" + body  + "' in our database!");
        }
    }

    public List<CommentDto> getCommentsDtoByBody(String body) throws CommentNotFoundException {
        Optional<List<Comment>> commentList = Optional.ofNullable(commentRepository.findAllByBodyContaining(body));
        if (commentList.isPresent()) {
            return commentMapper.commentListToCommentDtoList(commentList.get());
        } else {
            log.error("Error in method: getCommentsDtoByBody. Not found any comment with provided keyword: '" + body  + "' in our database!");
            throw new CommentNotFoundException("Not found any comment with keyword: '" + body  + "' in our database!");
        }
    }

    public CommentDto addOneLikeToComment(Integer commentId) throws CommentNotFoundException {
        Optional<Comment> comment = commentRepository.findById(commentId);
        if (comment.isPresent()) {
            Comment commentFromDb = comment.get();
            commentFromDb.setLikeList(commentFromDb.getLikeList());
            return commentMapper.commentToCommentDto(commentRepository.save(commentFromDb));
        } else {
            log.error("Error in method: addOneLikeToComment. Not found comment with id: '" + commentId  + "'!");
            throw new CommentNotFoundException("Unable to add like to comment because not found comment with  id: '" + commentId  + "' in our database!");
        }
    }

    public CommentDto addOneDisLikeToComment(Integer commentId) throws CommentNotFoundException {
        Optional<Comment> comment = commentRepository.findById(commentId);
        if (comment.isPresent()) {
            Comment commentFromDb = comment.get();
            commentFromDb.setDislikeList(commentFromDb.getDislikeList());
            return commentMapper.commentToCommentDto(commentRepository.save(commentFromDb));
        } else {
            log.error("Error in method: addOneDisLikeToComment. Not found comment with id: '" + commentId  + "' in our database!");
            throw new CommentNotFoundException("Unable to add dislike to comment because not found comment with  id: '" + commentId  + "' in our database!");
        }
    }

    public CommentDto editComment(CommentBodyDto commentBodyDto, User loginUser, Integer commentId) throws CommentNotFoundException, UserForbiddenAccessException {
        Comment commentToEdit = getCommentById(commentId);
        String body = commentBodyDto.getBody();
        if (isCommentWasCreatedByLoginUserOrUserHaveRoleAdmin(loginUser, commentToEdit)) {
            isCommentBodyIsNotBlank(body);
            commentToEdit.setBody(body);
            commentToEdit.setUpdated(LocalDateTime.now());
            commentRepository.save(commentToEdit);

        } else {
            log.error("Error in method editComment. Username: {} have not permission to edit specified comment with id: {}", loginUser.getUsername(), commentId);
            throw new UserForbiddenAccessException("Username: '" + loginUser.getUsername() + "' have not permission to edit comment with id: '" + commentId + " !");
        }
        return commentMapper.commentToCommentDto(commentToEdit);
    }

    private boolean isCommentWasCreatedByLoginUserOrUserHaveRoleAdmin(User loginUser, Comment commentToEdit) {
        return loginUser.getUsername().equals(commentToEdit.getUser().getUsername()) || loginUser.getRole().equals("ROLE_ADMIN");
    }

    public List<CommentDto> getCommentsDtoListByPostId(Integer postId) throws PostNotFoundException {
        List<Comment> allCommentsByPostId = commentRepository.findAllCommentsByPostId(postId);
        if (allCommentsByPostId != null) {
            return commentMapper.commentListToCommentDtoList(allCommentsByPostId);
        } else {
            log.error("Error in method: getCommentsDtoListByPostId! Not found any comments for post with id: " + postId);
            throw new PostNotFoundException("Not found any comments for post with id: " + postId + " !");
        }
    }
}

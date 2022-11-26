package com.serwisspolecznosciowy.Application.service;

import com.serwisspolecznosciowy.Application.dto.CommentBodyDto;
import com.serwisspolecznosciowy.Application.dto.CommentDto;
import com.serwisspolecznosciowy.Application.dto.DislikeDto;
import com.serwisspolecznosciowy.Application.dto.LikeDto;
import com.serwisspolecznosciowy.Application.entity.*;
import com.serwisspolecznosciowy.Application.exception.*;
import com.serwisspolecznosciowy.Application.mappers.CommentMapper;
import com.serwisspolecznosciowy.Application.mappers.DislikeMapper;
import com.serwisspolecznosciowy.Application.mappers.LikeMapper;
import com.serwisspolecznosciowy.Application.repository.CommentRepository;
import com.serwisspolecznosciowy.Application.repository.DislikeRepository;
import com.serwisspolecznosciowy.Application.repository.LikeRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

@Service
@Slf4j
public class CommentService {

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private CommentMapper commentMapper;

    @Autowired
    private UserService userService;

    @Autowired
    private PostService postService;

    @Autowired
    private LikeRepository likeRepository;

    @Autowired
    private LikeMapper likeMapper;

    @Autowired
    private DislikeRepository dislikeRepository;

    @Autowired
    private DislikeMapper dislikeMapper;

    public CommentDto addNewComment(Integer postId, CommentBodyDto commentBodyDto) throws PostNotFoundException {
        User loginUser = userService.getLoginUser();
        Comment comment = new Comment();
        String commentBody = commentBodyDto.getBody();
        isCommentBodyIsNotBlank(commentBody);
        comment.setBody(commentBody);
        comment.setCreated(LocalDateTime.now());
        comment.setPostId(postId);
        comment.setUser(loginUser);
        comment.setLikeList(Collections.emptyList());
        comment.setDislikeList(Collections.emptyList());
        commentRepository.save(comment);

        Post postById = postService.findPostById(postId);
        postById.setNumberOfComments(postById.getNumberOfComments() + 1);

        log.info("New comment with body: '" + commentBody + "' added to database.");
        return commentMapper.commentToCommentDto(comment, loginUser, Collections.emptyList(), Collections.emptyList());
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
        List<CommentDto> commentDtoList = new ArrayList<>();
        List<Comment> commentList = commentRepository.findAllComments(PageRequest.of(pageNumber, pageSize, Sort.by(wayOfSort, "created")));
        for (Comment comment : commentList) {
            User user = comment.getUser();
            CommentDto commentDto = commentMapper.commentToCommentDto(comment, user, likeMapper.likeListToLikeDtoList(comment.getLikeList()), dislikeMapper.dislikeListToDislikeDtoList(comment.getDislikeList()));
            commentDtoList.add(commentDto);
        }
        return commentDtoList;
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
        return commentMapper.commentToCommentDto(comment, comment.getUser(), likeMapper.likeListToLikeDtoList(comment.getLikeList()), dislikeMapper.dislikeListToDislikeDtoList(comment.getDislikeList()));
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
        return Collections.emptyList();
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
        User user = userService.getLoginUser();
        Optional<Comment> optionalComment = commentRepository.findById(commentId);
        if (optionalComment.isPresent()) {
            Comment comment = optionalComment.get();
            checkUserNotAlreadyAddOneLikeToComment(user, comment);
            Like like = new Like();
            like.setUserId(user.getId());
            like.setCommentLikeId(comment.getId());
            like.setUsername(user.getUsername());
            likeRepository.save(like);

            List<Like> likeList = new LinkedList<>();
            likeList.addAll(comment.getLikeList());
            likeList.add(like);
            List<LikeDto> likeDtoList = likeMapper.likeListToLikeDtoList(likeList);

            List<Dislike> dislikeList = new LinkedList<>();
            dislikeList.addAll(comment.getDislikeList());
            List<DislikeDto> dislikeDtoList = dislikeMapper.dislikeListToDislikeDtoList(dislikeList);

            return commentMapper.commentToCommentDto(commentRepository.save(comment), user, likeDtoList, dislikeDtoList);
        } else {
            log.error("Error in method: addOneLikeToComment. Not found comment with id: '" + commentId  + "'!");
            throw new CommentNotFoundException("Unable to add like to comment because not found comment with  id: '" + commentId  + "' in our database!");
        }
    }

    private void checkUserNotAlreadyAddOneLikeToComment(User user, Comment comment) {
        if (comment.getLikeList().stream().anyMatch(currentLike -> currentLike.getUserId().equals(user.getId()))) {
            log.error("Error in method checkUserNotAlreadyAddOneLikeToComment! User can add only once like to specified comment!");
            throw new DuplicateUsernameException("User can add only once like to specified comment!");
        }
    }

    public CommentDto addOneDisLikeToComment(Integer commentId) throws CommentNotFoundException {
        User user = userService.getLoginUser();
        Optional<Comment> optionalComment = commentRepository.findById(commentId);
        if (optionalComment.isPresent()) {
            Comment comment = optionalComment.get();
            checkUserNotAlreadyAddOneDisikeToComment(user, comment);
            Dislike dislike = new Dislike();
            dislike.setCommentDislikeId(comment.getId());
            dislike.setUserId(user.getId());
            dislike.setUsername(user.getUsername());
            dislikeRepository.save(dislike);

            List<Like> likeList = new LinkedList<>();
            likeList.addAll(comment.getLikeList());
            List<LikeDto> likeDtoList = likeMapper.likeListToLikeDtoList(likeList);

            List<Dislike> dislikeList = new LinkedList<>();
            dislikeList.addAll(comment.getDislikeList());
            dislikeList.add(dislike);
            List<DislikeDto> dislikeDtoList = dislikeMapper.dislikeListToDislikeDtoList(dislikeList);
            return commentMapper.commentToCommentDto(commentRepository.save(comment), user, likeDtoList, dislikeDtoList);
        } else {
            log.error("Error in method: addOneDisLikeToComment. Not found comment with id: '" + commentId  + "' in our database!");
            throw new CommentNotFoundException("Unable to add dislike to comment because not found comment with  id: '" + commentId  + "' in our database!");
        }
    }

    private void checkUserNotAlreadyAddOneDisikeToComment(User user, Comment comment) {
        if (comment.getDislikeList().stream().anyMatch(userInDislikeList -> userInDislikeList.getUserId().equals(user.getId()))) {
            log.error("Error in method addOneDisLikeToPost! User can add only once dislike to specified comment!");
            throw new DuplicateUsernameException("User can add only once dislike to specified comment!");
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
        return commentMapper.commentToCommentDto(commentToEdit, loginUser, likeMapper.likeListToLikeDtoList(commentToEdit.getLikeList()), dislikeMapper.dislikeListToDislikeDtoList(commentToEdit.getDislikeList()));
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

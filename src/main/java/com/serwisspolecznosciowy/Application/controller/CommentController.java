package com.serwisspolecznosciowy.Application.controller;

import com.serwisspolecznosciowy.Application.dto.CommentBodyDto;
import com.serwisspolecznosciowy.Application.dto.CommentDtoWithAuthor;
import com.serwisspolecznosciowy.Application.entity.Comment;
import com.serwisspolecznosciowy.Application.entity.User;
import com.serwisspolecznosciowy.Application.exception.CommentEmptyBodyException;
import com.serwisspolecznosciowy.Application.exception.CommentNotFoundException;
import com.serwisspolecznosciowy.Application.exception.PostNotFoundException;
import com.serwisspolecznosciowy.Application.exception.UserForbiddenAccessException;
import com.serwisspolecznosciowy.Application.service.CommentService;
import com.serwisspolecznosciowy.Application.service.PostService;
import com.serwisspolecznosciowy.Application.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Slf4j
@RequestMapping("/comment")
public class CommentController {

    @Autowired
    CommentService commentService;

    @Autowired
    UserService userService;

    @Autowired
    PostService postService;

    @PostMapping("/add/{postId}")
    @Operation(summary = "Add new comment by post id", description = "User needs to provide only comment body in request body and post id in patch. " +
            "Method required to provide also post id, but in frontend it will be done automatically.")
    public ResponseEntity<CommentDtoWithAuthor> addNewCommentByPostId(@PathVariable Integer postId, @RequestBody CommentBodyDto commentBodyDto)  {
        log.info("Start to add new comment.");
        try {
            return new ResponseEntity(commentService.addNewComment(postId, commentBodyDto), HttpStatus.CREATED);

        } catch (PostNotFoundException e) {
            return new ResponseEntity(e.getMessage(), HttpStatus.NOT_FOUND);
        } catch (CommentEmptyBodyException exc) {
            return new ResponseEntity(exc.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/all")
    public ResponseEntity<List<Comment>> getAllCommentsWithAuthors(@RequestParam(required = false) Integer page, @RequestParam(required = false) Integer size, Sort.Direction sort) {
        Integer pageNumber =  page != null && page > 0 ? page : 0;
        Integer pageSize = size != null && size > 0 ? size : 10;
        Sort.Direction wayOfSort = sort != null ? sort : Sort.Direction.DESC;
        log.info("Start to get all comments.");
        return ResponseEntity.ok(commentService.getAllComments(pageNumber, pageSize, wayOfSort));
    }

    @GetMapping("/all/dto")
    @Operation(summary = "Get all comments with authors", description = "Default comment page is set as 0 and page size is set for 10.\nIf you want see more comments than 10 set size for bigger or change page.\n" +
            "Page way of sort is set as DESC (from the newest to the older) based on date of created. " +
            "This method also using cache with is refreshed after 30 seconds.",
            parameters = { @Parameter(name = "size", example = "10"), @Parameter(name = "page", example = "0"), @Parameter(name = "sort", example = "DESC")})
    public ResponseEntity<List<CommentDtoWithAuthor>> getAllCommentsDtoWithAuthorsDto(@RequestParam(required = false) Integer page, @RequestParam(required = false) Integer size, Sort.Direction sort) {
        Integer pageNumber = page != null && page > 0 ? page : 0;
        Integer pageSize = size != null && size > 0 ? size : 10;
        Sort.Direction wayOfSort = sort != null ? sort : Sort.Direction.DESC;
        log.info("Start to get all comments dto.");
        return ResponseEntity.ok(commentService.getAllCommentsDto(pageNumber, pageSize, wayOfSort));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Comment> getCommentWithAuthorByCommentId(@PathVariable Integer id) {
        log.info("Start to get comment with id: " + id);
        try {
            return ResponseEntity.ok(commentService.getCommentById(id));
        } catch (CommentNotFoundException e) {
            return new ResponseEntity(e.getMessage(), HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("/dto/{id}")
    @Operation(summary = "Get comment with author by comment id")
    public ResponseEntity<CommentDtoWithAuthor> getCommentDtoWithAuthorDtoByCommentId(@PathVariable Integer id) {
        log.info("Start to get comment with id: " + id);
        try {
            return ResponseEntity.ok(commentService.getCommentDtoById(id));
        } catch (CommentNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @DeleteMapping("/delete/{commentId}/{postId}")
    @Operation(summary = "Delete comment by comment id and post id", description = "Only user with admin right or login author of comment can delete specific comment. " +
            "Method required to provide only comment id in path, but in frontend id will be took automatically.")
    public ResponseEntity<Void> deleteCommentByIdAndPostId(@PathVariable Integer commentId, @PathVariable Integer postId) {
        log.info("Start to delete comment with id: " + commentId);
        try {
            postService.subtractOneCommentForNumberOfCommentForPostByPostId(postId);
            commentService.deleteCommentById(commentId);
            log.info("Comment with id: " + commentId + " was deleted.");
            return new ResponseEntity(HttpStatus.NO_CONTENT);

        } catch (CommentNotFoundException | PostNotFoundException ex) {
            return new ResponseEntity(ex.getMessage(), HttpStatus.NOT_FOUND);
        } catch (UserForbiddenAccessException exp) {
            return new ResponseEntity(exp.getMessage(), HttpStatus.FORBIDDEN);
        }
    }

    @GetMapping("/body")
    public ResponseEntity<List<Comment>> getCommentsListByKeywordInCommentBody(@RequestParam String body) throws CommentNotFoundException {
        log.info("Start to get comment with body: " + body);
        try {
            return ResponseEntity.ok(commentService.getCommentsByBody(body));
        } catch (CommentNotFoundException e) {
            return new ResponseEntity(e.getMessage(), HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("/body/dto")
    @Operation(summary = "Get comments list by keyword in comment body", description = "Keyword is not sensitive - you can provide small or " +
            "capital characters and polish marks. Maybe rather that error in case when we not found body we should return empty list and information about that - to discuss." +
            "But probably in real app I will not allows user to do that due to too many records.")
    public ResponseEntity<List<CommentDtoWithAuthor>> getCommentsDtoListByKeywordInCommentBody(@RequestParam String body) {
        log.info("Start to get comment with body: " + body);
        try {
            return ResponseEntity.ok(commentService.getCommentsDtoByBody(body));
        } catch (CommentNotFoundException e) {
            return new ResponseEntity(e.getMessage(), HttpStatus.NOT_FOUND);
        }
    }

    @PostMapping("/like/dto/{commentId}")
    @Operation(summary = "Add one like to comment by id", description = "Method allows user to add couple likes by push the button couple times.")
    public ResponseEntity<CommentDtoWithAuthor> addOneLikeToCommentById(@PathVariable Integer commentId) throws CommentNotFoundException {
        log.info("Start to add like to comment with id: " + commentId);
        try {
            return ResponseEntity.ok(commentService.addOneLikeToComment(commentId));
        } catch (CommentNotFoundException e) {
            log.error("Error in POST method: addOneLikeToComment");
            return new ResponseEntity(e.getMessage(), HttpStatus.NOT_FOUND);
        }
    }

    @PostMapping("/dislike/dto/{commentId}")
    @Operation(summary = "Add one dislike to comment by id", description = "Method allows user to add couple likes by push the button couple times.")
    public ResponseEntity<CommentDtoWithAuthor> addOneDislikeToCommentById(@PathVariable Integer commentId) throws CommentNotFoundException {
        log.info("Start to add dislike to comment with id: " + commentId);
        try {
            return ResponseEntity.ok(commentService.addOneDisLikeToComment(commentId));
        } catch (CommentNotFoundException e) {
            log.error("Error in POST method: addOneLikeToComment");
            return new ResponseEntity(e.getMessage(), HttpStatus.NOT_FOUND);
        }
    }

    @PatchMapping("/edit/dto/{commentId}")
    @Operation(summary = "Edit comment by id", description = "Only login author of comment can edit specific comment. " +
            "Method required to provide only comment id in path, but in frontend id will be took automatically.")
    public ResponseEntity<CommentDtoWithAuthor> editCommentByCommentId(@PathVariable Integer commentId, @RequestBody CommentBodyDto commentBodyDto) {
        log.info("Start to edit editPostDto");
        try {
            User loginUser = userService.getLoginUser();
            return ResponseEntity.ok(commentService.editComment(commentBodyDto, loginUser, commentId));

        } catch (CommentNotFoundException e) {
            return new ResponseEntity(e.getMessage(), HttpStatus.NOT_FOUND);
        } catch (UserForbiddenAccessException exc) {
            return new ResponseEntity(exc.getMessage(), HttpStatus.FORBIDDEN);
        }
    }

    @GetMapping("/all/dto/{postId}")
    @Operation(summary = "Get comment by post id", description = "This method was created only for test " +
            "purpose. In normal app probably I will not allows that for users because they don't know post id and " +
            "can't find them.")
    public ResponseEntity<List<CommentDtoWithAuthor>> getCommentDtoListByPostId(@PathVariable Integer postId) {
        log.info("Start to get all comments by post with id: ", postId);
        try {
            return ResponseEntity.ok(commentService.getCommentsDtoListByPostId(postId));
        } catch (PostNotFoundException e) {
            return new ResponseEntity(e.getMessage(), HttpStatus.NOT_FOUND);
        }
    }

}
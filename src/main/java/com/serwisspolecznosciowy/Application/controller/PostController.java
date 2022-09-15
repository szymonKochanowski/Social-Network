package com.serwisspolecznosciowy.Application.controller;

import com.serwisspolecznosciowy.Application.dto.PostBodyDto;
import com.serwisspolecznosciowy.Application.dto.PostDtoWithAuthor;
import com.serwisspolecznosciowy.Application.entity.Post;
import com.serwisspolecznosciowy.Application.entity.User;
import com.serwisspolecznosciowy.Application.exception.PostEmptyBodyException;
import com.serwisspolecznosciowy.Application.exception.PostNotFoundException;
import com.serwisspolecznosciowy.Application.exception.UserForbiddenAccessException;
import com.serwisspolecznosciowy.Application.exception.UserNotFoundException;
import com.serwisspolecznosciowy.Application.service.PostService;
import com.serwisspolecznosciowy.Application.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@Slf4j
@RequestMapping("/post")
public class PostController {

    @Autowired
    PostService postService;

    @Autowired
    UserService userService;

    @PostMapping("/add/dto")
    @Operation(summary = "Add new post", description = "User need to provide only body/context for new post.")
    public ResponseEntity<PostDtoWithAuthor> addNewPost(@RequestBody PostBodyDto postBodyDto) throws UserNotFoundException {
        log.info("Start to add new post");
        try {
            return new ResponseEntity(postService.addNewPost(postBodyDto), HttpStatus.CREATED);
        } catch (PostEmptyBodyException ex) {
           return new ResponseEntity(ex.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/all")
    public ResponseEntity<List<Post>> getAllPostsWitCommentsAndAuthors(@RequestParam(required = false) Integer page, @RequestParam(required = false) Integer size, Sort.Direction sort) {
        Integer pageNumber = page != null && page > 0 ? page : 0;
        Integer pageSize = size != null && size > 0 ? size : 10;
        Sort.Direction wayOfSort = sort != null ? sort : Sort.Direction.DESC;
        log.info("Start to get all posts");
        return ResponseEntity.ok(postService.getAllPostsWithComments(pageNumber, pageSize, wayOfSort));
    }

    @GetMapping("/all/dto")
    @Operation(summary = "Get all posts with authors names and  profile picture", description = "Default post page is set as 0 and page size is set for 10.\nIf you want see more post than 10 set size for bigger or change page.\n" +
            "Page way of sort is set as DESC (from the newest to the older) based on date of created. " +
            "This method also using cache with is refreshed after 30 seconds.",
            parameters = { @Parameter(name = "size", example = "10"), @Parameter(name = "page", example = "0"), @Parameter(name = "sort", example = "DESC")})
    public ResponseEntity<List<PostDtoWithAuthor>> getAllPostsDtoWithUsernameAndUserProfilePicture(@RequestParam(required = false) Integer page, @RequestParam(required = false) Integer size, Sort.Direction sort) {
        Integer pageNumber = page != null && page > 0 ? page : 0;
        Integer pageSize = size != null && size > 0 ? size : 10;
        Sort.Direction wayOfSort = sort != null ? sort : Sort.Direction.DESC;
        log.info("Start to get all posts dto");
        return ResponseEntity.ok(postService.getAllPostsDtoWithUsersDto(pageNumber, pageSize, wayOfSort));
    }

    @PutMapping("/edit/dto/{postId}")
    @Operation(summary = "Edit existing post by id", description = "Only login author of post can edit specific post. Method required to provide also post id, but in" +
            " frontend it will be done automatically.")
    public ResponseEntity<PostDtoWithAuthor> editPostByPostDto(@PathVariable Integer postId, @RequestBody PostBodyDto postBodyDto) {
        log.info("Start to edit editPostDto");
        try {
            User userFromDb = userService.getLoginUser();
            return ResponseEntity.ok(postService.editPost(postBodyDto, userFromDb, postId));

        } catch (PostNotFoundException e) {
            return new ResponseEntity(e.getMessage(), HttpStatus.NOT_FOUND);
        } catch (UserForbiddenAccessException exc) {
            return new ResponseEntity(exc.getMessage(), HttpStatus.FORBIDDEN);
        } catch (PostEmptyBodyException exce) {
            return new ResponseEntity(exce.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @DeleteMapping("/delete/{id}")
    @Operation(summary = "Delete existing post by id", description = "Only user with admin right or login author of post can delete specific post. " +
            "Method required to provide also post id, but in frontend it will be done automatically.")
    public ResponseEntity<Void> deletePostById(@PathVariable Integer id) {
        log.info("Start to delete post");
        try {
            Optional<User> user = userService.findUserByUsername(SecurityContextHolder.getContext().getAuthentication().getName());
            postService.deletePostById(user, id);
            return new ResponseEntity(HttpStatus.NO_CONTENT);

        } catch (UserNotFoundException |  PostNotFoundException e) {
            return new ResponseEntity(e.getMessage(), HttpStatus.NOT_FOUND);
        } catch (UserForbiddenAccessException exp) {
            return new ResponseEntity(exp.getMessage(), HttpStatus.FORBIDDEN);
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<Post> getPostById(@PathVariable Integer id) throws PostNotFoundException {
        log.info("Start to get post with id: " + id);
        try {
            return ResponseEntity.ok(postService.findPostById(id));
        } catch (PostNotFoundException e) {
            return new ResponseEntity(e.getMessage(), HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("/dto/{id}")
    @Operation(summary = "Get post by id", description = "Only login user can search post by id. This " +
            "method was created only for test purpose. In normal app probably I will not allows that" +
            " for users because they don't know post id and can't find them.")
    public ResponseEntity<PostDtoWithAuthor> getPostDtoById(@PathVariable Integer id) throws PostNotFoundException {
        log.info("Start to get post dto with id: " + id);
        try {
            return ResponseEntity.ok(postService.findPostDtoById(id));
        } catch (PostNotFoundException e) {
            return new ResponseEntity(e.getMessage(), HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("/body/dto")
    @Operation(summary = "Get posts by keyword in post body", description = "Keyword is not sensitive - you can provide small or " +
            "capital characters and polish marks.")
    public ResponseEntity<List<PostDtoWithAuthor>> getPostDtoListByKeywordInPostBody(@RequestParam String keywordInBody) throws PostNotFoundException {
        log.info("Start to get posts with specific text in keywordInBody: " + keywordInBody);
        try {
            return ResponseEntity.ok(postService.getPostDtoListByBody(keywordInBody));
        } catch (PostNotFoundException e) {
            return new ResponseEntity(e.getMessage(), HttpStatus.NOT_FOUND);
        }
    }

    @PostMapping("/addLike/dto/{postId}")
    @Operation(summary = "Add one like to post by id", description = "Method allows user to add couple likes by push the button couple times.")
    public ResponseEntity<PostDtoWithAuthor> addOneLikeToPostByPostId(@PathVariable Integer postId) {
        log.info("Start to add like to post with id: " + postId);
        try {
            return ResponseEntity.ok(postService.addOneLikeToPost(postId));
        } catch (PostNotFoundException e) {
            return new ResponseEntity(e.getMessage(), HttpStatus.NOT_FOUND);
        }
    }

    @PostMapping("/addDislike/dto/{postId}")
    @Operation(summary = "Add one dislike to post by id", description = "Method allows user to add couple dislikes by push the button couple times.")
    public ResponseEntity<PostDtoWithAuthor> addOneDislikeToPostByPostId(@PathVariable Integer postId) {
        log.info("Start to add dislike to post with id: " + postId);
        try {
            return ResponseEntity.ok(postService.addOneDisLikeToPost(postId));
        } catch (PostNotFoundException e) {
            return new ResponseEntity(e.getMessage(), HttpStatus.NOT_FOUND);
        }
    }

}
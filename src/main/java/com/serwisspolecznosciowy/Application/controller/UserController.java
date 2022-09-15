package com.serwisspolecznosciowy.Application.controller;

import com.serwisspolecznosciowy.Application.dto.NewUserDto;
import com.serwisspolecznosciowy.Application.dto.UserDto;
import com.serwisspolecznosciowy.Application.entity.User;
import com.serwisspolecznosciowy.Application.exception.*;
import com.serwisspolecznosciowy.Application.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.Optional;

@Slf4j
@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    UserService userService;

    @GetMapping("/all")
    public ResponseEntity<List<User>> getUsers() {
        log.info("Start to get all users");
        return ResponseEntity.ok(userService.findAllUsers());
    }


    @GetMapping("/{id}")
    public ResponseEntity<Optional<User>> findUserById(@PathVariable("id") Integer id) {
        log.info("Start to get user with id: {}", id);
        try {
            return ResponseEntity.ok(userService.findUserById(id));
        } catch (UserNotFoundException e) {
            log.error("User with id: {} not found", id);
            return new ResponseEntity(e.getMessage(), HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("/all/username/dto")
    @Operation(summary = "Get users by keyword in username", description = "Keyword is not sensitive - you can provide small or " +
            "capital characters and polish marks. Method is returning empty list with message if not found eny.")
    public ResponseEntity<List<UserDto>> getUserDtoListByKeywordInUsername(@RequestParam String keyword) {
        log.info("Start to get user by keyword: '" + keyword + "' in username.");
        List<UserDto> userDtoList = userService.findUserByKeywordInUsername(keyword);
        if (!userDtoList.isEmpty()) {
            return ResponseEntity.ok(userDtoList);
        } else {
            return new ResponseEntity("User with keyword '" + keyword + "' in username not found!", HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping("/add/dto")
    @Operation(summary = "Add new user", description = "User need to provide correct username and password. User profile picture is optional.")
    public ResponseEntity<UserDto> addNewUser(@Valid @RequestBody NewUserDto newUserDto) throws Exception {
        log.info("Start to add new user with username: " + newUserDto.getUsername());
        try {
            return new ResponseEntity(userService.addNewUser(newUserDto), HttpStatus.CREATED);
        } catch (DuplicateUsernameException ex) {
            return new ResponseEntity(ex.getMessage(), HttpStatus.CONFLICT);
        } catch (IncorrectNewPasswordException e) {
            return new ResponseEntity(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping("/login")
    public ResponseEntity<Void> login(@RequestParam String username, @RequestParam String password) throws UserNotFoundException {
        log.info("Start to log user");
        try {
            User userFromDb = userService.findUserByUsername(username).get();
            userService.checkProvidedPasswordWithPasswordFromDb(password, userFromDb.getPassword());
            return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
        } catch (UserNotFoundException e) {
            return new ResponseEntity(e.getMessage(), HttpStatus.NOT_FOUND);
        } catch (WrongPasswordException ex) {
            return new ResponseEntity(ex.getMessage(), HttpStatus.UNAUTHORIZED);
        }
    }

    @PatchMapping("/password/dto/{userId}")
    @Operation(summary = "Update user password by id", description = "Only owner, login user can update password. User need to provide correctly new password two time and old password. " +
            "User need to provide user id but in fronted it will be done automatically.")
    public ResponseEntity<String> updatePasswordByUserId(@PathVariable Integer userId, @RequestParam String newPassword1, @RequestParam String newPassword2, @RequestParam String oldPassword) throws UserNotFoundException {
        log.info("Start to update user password.");
        try {
            return ResponseEntity.ok(userService.updatePassword(userId, newPassword1, newPassword2, oldPassword));
        } catch (IncorrectOldPasswordException e) {
            return new ResponseEntity(e.getMessage(), HttpStatus.UNAUTHORIZED);
        } catch (NewPasswordNotMatchException e) {
            return new ResponseEntity(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @PatchMapping("/picture/dto/{userId}")
    @Operation(summary = "Update user profile picture by id", description = "Only owner, login user can update link to profile picture." +
            " User need to provide user id but in fronted it will be done automatically.")
    public ResponseEntity<String> updateUserProfilePictureByUserId(@PathVariable Integer userId, @RequestParam String profilePictureUrl) {
        log.info("Start to update user profile picture.");
        try {
            return ResponseEntity.ok(userService.updateUserProfilePicture(userId, profilePictureUrl));
        } catch (UserNotFoundException e) {
            return new ResponseEntity(e.getMessage(), HttpStatus.NOT_FOUND);
        } catch (UserForbiddenAccessException ex) {
            return new ResponseEntity(ex.getMessage(), HttpStatus.FORBIDDEN);
        }
    }

    /* Allows user with role admin to block user */
    @PatchMapping("/enable/{userId}")
    public ResponseEntity<String> updateUserEnabledByUserId(@PathVariable Integer userId, @RequestParam Boolean enabled) {
        log.info("Start to change user enable.");
        try {
            return ResponseEntity.ok(userService.updateUserEnable(userId, enabled));
        } catch (UserNotFoundException e) {
            return new ResponseEntity(e.getMessage(), HttpStatus.NOT_FOUND);
        }
    }

    @DeleteMapping("/delete/dto/{userId}")
    @Operation(summary = "Delete user by id", description = "Only user with admin right or owner, login user can" +
            "delete specific user. User need to provide user id but in fronted it will be done automatically.")
    public ResponseEntity<Void> deleteUserById(@PathVariable Integer userId) {
        log.info("Start to delete user with id: " + userId);
        try {
            userService.deleteUserById(userId);
            log.info("User with id: " + userId + " deleted successfully!");
            return new ResponseEntity(HttpStatus.NO_CONTENT);

        } catch (UserNotFoundException | PostNotFoundException | CommentNotFoundException e) {
            return new ResponseEntity(e.getMessage(), HttpStatus.NOT_FOUND);
        } catch (UserForbiddenAccessException ex) {
            return new ResponseEntity(ex.getMessage(), HttpStatus.FORBIDDEN);
        }
    }

    @GetMapping("/post/{postId}")
    public ResponseEntity<UserDto> getUserByPostId(@PathVariable Integer postId) {
        log.info("Start to get user by post id: " + postId);
        try {
            return ResponseEntity.ok(userService.getUserByPostId(postId));
        } catch (UserNotFoundException e) {
            return new ResponseEntity(e.getMessage(), HttpStatus.NOT_FOUND);
        }
    }

}

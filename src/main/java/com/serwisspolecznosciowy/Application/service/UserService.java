package com.serwisspolecznosciowy.Application.service;

import com.serwisspolecznosciowy.Application.dto.NewUserDto;
import com.serwisspolecznosciowy.Application.dto.UserDto;
import com.serwisspolecznosciowy.Application.entity.Comment;
import com.serwisspolecznosciowy.Application.entity.Post;
import com.serwisspolecznosciowy.Application.entity.User;
import com.serwisspolecznosciowy.Application.exception.*;
import com.serwisspolecznosciowy.Application.mappers.UserMapper;
import com.serwisspolecznosciowy.Application.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class UserService {

    @Autowired
    private CommentService commentService;

    @Autowired
    private PostService postService;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private UserRepository userRepository;

    private static final PasswordEncoder PASSWORD_ENCODER = new BCryptPasswordEncoder();

    public List<User> findAllUsers() {
        return this.userRepository.findAll();
    }

    public UserDto addNewUser(NewUserDto newUserDto) throws UserNotFoundException {
        User user = new User();
        String password = newUserDto.getPassword();
        String username = newUserDto.getUsername();

        checkUsernameNotAlreadyExisting(username);
        user.setUsername(username);

        checkPasswordSyntax(password);
        String encodePassword = PASSWORD_ENCODER.encode(password);
        user.setPassword(encodePassword);

        user.setCreated(LocalDateTime.now());
        user.setEnabled(true);

        setAppropriateRoleForUser(user, username);
        user.setProfilePicture(user.getProfilePicture());
        userRepository.save(user);
        return userMapper.userToUserDto(user);
    }

    private void checkUsernameNotAlreadyExisting(String username) {
        Optional<User> optionalUser = userRepository.findByUsername(username);
        if (optionalUser.isPresent()) {
            log.error("The username '{}' already exists!", username);
            throw new DuplicateUsernameException("User with username '" + username + "' already exists! Please choose another username.");
        }
    }

    private boolean checkPasswordSyntax(String password) {
        String reqex = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[!@#&()–[{}]:;',?/*~$^+=<>.]).{6,}$";
        if (!password.isBlank() && password.matches(reqex) && (password.length() >= 6)){
            return true;
        } else
            log.error("Wrong password! Password have to contain between 6 and 64 characters including special marks! " +
                    "Password must not be blank and contains at least one number, one capital letter and one special mark!");
            throw new IncorrectNewPasswordException("Wrong password! \nPassword have to contain between 6 and 64 characters including special marks!" +
                    "\nPassword must not be blank and contains at least one number, one small character, one capital character and one special mark!");
    }

    private void setAppropriateRoleForUser(User user, String username) throws UserNotFoundException {
        if (getLoginUser() != null && getLoginUser().getRole().equals("ROLE_ADMIN")) {
            log.info("Set 'ROLE_ADMIN' for username: {}", username);
            user.setRole("ROLE_ADMIN");
        } else {
            log.info("Set 'ROLE_USER' for username: {}", username);
            user.setRole("ROLE_USER");
        }
    }

    public Optional<User> findUserByUsername(String username) throws UserNotFoundException {
        Optional<User> usersFromDb = this.userRepository.findByUsername(username);
        if (usersFromDb.isPresent() && !username.isBlank()) {
            return usersFromDb;
        } else {
            log.error("Error in method findUserByUsername! User with username: " + username + " not found!");
            throw new UserNotFoundException("User with username: " + username + " not found!");
        }
    }

    public String updatePassword(Integer userId, String newPassword1, String newPassword2, String oldRawPassword) throws UserNotFoundException {
        User user = findUserById(userId).get();
        String encodedUserPassword = user.getPassword();
        if (PASSWORD_ENCODER.matches(oldRawPassword, encodedUserPassword)) {
            checkPasswordSyntax(newPassword1);
            if (newPassword1.equals(newPassword2)) {
                String encodePassword = PASSWORD_ENCODER.encode(newPassword1);
                user.setPassword(encodePassword);
                user.setUpdated(LocalDateTime.now());
                userRepository.save(user);
            } else {
                log.error("New passwords are not the same!");
                throw new NewPasswordNotMatchException("New passwords are not the same!");
            }

        } else {
            log.error("Incorrect old password!");
            throw new IncorrectOldPasswordException("Incorrect old password!");
        }
        return "Password changed successfully!";
    }

    public void deleteUserById(Integer userId) throws UserNotFoundException, PostNotFoundException, UserForbiddenAccessException, CommentNotFoundException {
        Optional<User> userFromDb = findUserById(userId);
        deleteCommentsAndPostsByUserId(userFromDb);
        userRepository.deleteById(userId);
    }


    public Optional<User> findUserById(Integer id) throws UserNotFoundException{
        Optional<User> optionalUser = userRepository.findById(id);
        if (optionalUser.isPresent()){
            return optionalUser;
        } else {
            log.error("User with id: {} not found in our database.", id);
            throw new UserNotFoundException("User with id: " + id + " not found!");
        }
    }

    private void deleteCommentsAndPostsByUserId(Optional<User> userFromDb) throws PostNotFoundException, UserForbiddenAccessException, CommentNotFoundException {
        Integer userId = userFromDb.get().getId();

        List<Comment> commentList = commentService.findAllCommentByUserId(userId);
        if (commentList != null || !commentList.isEmpty()) {
            for (Comment comment : commentList) {
                commentService.deleteCommentById(comment.getId());
            }
        }

        List<Post> postsList = postService.findAllPostsByUserId(userId);
        if (postsList != null || !postsList.isEmpty()) {
            for (Post post : postsList) {
                postService.deletePostById(userFromDb, post.getId());
            }
        }
    }

    public User getLoginUser() {
        Optional<Authentication> auth = Optional.ofNullable(SecurityContextHolder.getContext().getAuthentication());
        String username = null;
        if (auth.isPresent()) {
            username = auth.get().getName();
        } else {
            log.info("No user is logged in.");
            return null;
        }
        return userRepository.findByUsername(username).orElse(null);
    }

    public List<UserDto> findUserByKeywordInUsername(String keyword) {
        List<User> userList = userRepository.findByUsernameLike(keyword);
        if (!userList.isEmpty()) {
            return userMapper.userListToUserDtoList(userList);
        } else {
            log.info("User with keyword '" + keyword + "' in username not found!");
            return Collections.emptyList();
        }
    }

    public boolean checkProvidedPasswordWithPasswordFromDb(String passwordProvidedByUser, String passwordFromDb) {
        if (PASSWORD_ENCODER.matches(passwordProvidedByUser, passwordFromDb)) {
            return true;
        } else {
            log.error("Error on method checkPassword! Password is incorrect!");
            throw new WrongPasswordException("Incorrect password!");
        }
    }

    public String updateUserProfilePicture(Integer userId, String profilePictureUrl) throws UserNotFoundException, UserForbiddenAccessException {
        Optional<User> optionalUser = findUserById(userId);
        User loginUser = getLoginUser();
        if (loginUser.getUsername().equals(optionalUser.get().getUsername())) {
            User user = optionalUser.get();
            user.setUpdated(LocalDateTime.now());
            user.setProfilePicture(profilePictureUrl);
            userRepository.save(user);
            return "User profile picture saved successfully!";
        } else {
            log.error("Error in method: updateUserProfilePicture! User is not authorized to change profile picture!");
            throw new UserForbiddenAccessException("User is not authorized to change profile picture!");
        }
    }

    public String updateUserEnable(Integer userId, Boolean enabled) throws UserNotFoundException {
        Optional<User> optionalUser = findUserById(userId);
        String username = optionalUser.get().getUsername();
        User user = optionalUser.get();
        user.setUpdated(LocalDateTime.now());
        user.setEnabled(enabled);
        userRepository.save(user);
        return "Enabled saved successfully for '" + enabled + "' for username '" + username + "'!";
    }

    public UserDto getUserByPostId(Integer postId) throws UserNotFoundException {
        Optional<User> optionalUser = userRepository.findByPostId(postId);
        if (optionalUser.isPresent()) {
            User user = optionalUser.get();
            return userMapper.userToUserDto(user);
        } else {
            log.error("Error in method: getUserByPostId! Author of post with id: '" + postId + "' was not found!");
            throw new UserNotFoundException("Author of post with id: '" + postId + "' was not found!");
        }
    }
}


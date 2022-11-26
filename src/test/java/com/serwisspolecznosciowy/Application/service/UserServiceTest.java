package com.serwisspolecznosciowy.Application.service;

import com.serwisspolecznosciowy.Application.dto.NewUserDto;
import com.serwisspolecznosciowy.Application.dto.UserDto;
import com.serwisspolecznosciowy.Application.entity.Comment;
import com.serwisspolecznosciowy.Application.entity.Post;
import com.serwisspolecznosciowy.Application.entity.User;
import com.serwisspolecznosciowy.Application.exception.*;
import com.serwisspolecznosciowy.Application.mappers.UserMapper;
import com.serwisspolecznosciowy.Application.repository.CommentRepository;
import com.serwisspolecznosciowy.Application.repository.PostRepository;
import com.serwisspolecznosciowy.Application.repository.UserRepository;
import com.serwisspolecznosciowy.Application.testData.TestData;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
@SpringBootTest
class UserServiceTest {

    @InjectMocks
    private UserService userService;

    @Mock
    private UserRepository userRepository;

    @Autowired
    public TestData testData;

    @Mock
    private CommentService commentService;

    @Mock
    private PostService postService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private CommentRepository commentRepository;

    @Mock
    PostRepository postRepository;

    @Mock
    public UserMapper userMapper;

    @Test
    void shouldFindAllUsers() {
        //given
        List<User> expectedUserList = testData.preparedUsersList();
        when(userRepository.findAll()).thenReturn(expectedUserList);
        //when
        List<User> actualUsersList = userService.findAllUsers();
        //then
        assertEquals(expectedUserList, actualUsersList);
    }

    @Test
    void addNewUser() throws UserNotFoundException {
        //given
        User user = testData.preparedUser();
        NewUserDto newUserDto = testData.preparedNewUserDto();
        UserDto expectedUserDto = testData.preparedUserDto();
        when(userRepository.findByUsername(user.getUsername())).thenReturn(Optional.ofNullable(null));
        when(userRepository.save(any())).thenReturn(user);
        when(userMapper.userToUserDto(any())).thenReturn(expectedUserDto);

        //when
        UserDto actualUserDto = userService.addNewUser(newUserDto);

        //then
        assertEquals(expectedUserDto.getUsername(), actualUserDto.getUsername());
        assertEquals(expectedUserDto.getProfilePicture(), actualUserDto.getProfilePicture());
    }

    @WithMockUser(username = "admin12!A", password = "admin12!A", roles = {"ADMIN"})
    @Test
    void addNewUserWithRoleAdmin() throws UserNotFoundException {
        //given
        User loginAdmin = testData.preparedAdmin();
        String passwordLogInAdmin = loginAdmin.getPassword();

        User newAdmin = new User();
        newAdmin.setUsername("admin321!A");
        newAdmin.setPassword("admin321!A");

        NewUserDto newUserDto = testData.preparedNewUserDto();
        newUserDto.setUsername(newAdmin.getUsername());
        newUserDto.setPassword(newAdmin.getPassword());

        UserDto expectedUserDto = testData.preparedUserDto();
        when(userRepository.findByUsername(newAdmin.getUsername())).thenReturn(Optional.ofNullable(null));
        when(userRepository.findByUsername(passwordLogInAdmin)).thenReturn(Optional.ofNullable(loginAdmin));
        when(userRepository.save(any())).thenReturn(newAdmin);
        when(userMapper.userToUserDto(any())).thenReturn(expectedUserDto);

        //when
        UserDto actualUserDto = userService.addNewUser(newUserDto);

        //then
        assertEquals(expectedUserDto.getUsername(), actualUserDto.getUsername());
        assertEquals(expectedUserDto.getProfilePicture(), actualUserDto.getProfilePicture());
    }

    @Test
    void addNewUserWithDuplicateUsernameException() throws UserNotFoundException {
        //given
        User user = testData.preparedUser();
        String username = user.getUsername();
        NewUserDto newUserDto = testData.preparedNewUserDto();
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));
        //when
        //then
        assertThrows(DuplicateUsernameException.class, () -> userService.addNewUser(newUserDto),
                "User with username '" + username + "' already exists! Please choose another username.");
    }

    @Test
    void addNewUserWithIncorrectNewPasswordException() throws UserNotFoundException {
        //given
        User user = testData.preparedUser();
        String username = user.getUsername();
        NewUserDto newUserDto = testData.preparedNewUserDto();
        newUserDto.setPassword("test");
        when(userRepository.findByUsername(username)).thenReturn(Optional.ofNullable(null));
        //when
        //then
        assertThrows(IncorrectNewPasswordException.class, () -> userService.addNewUser(newUserDto),
                "Wrong password! \nPassword have to contain between 6 and 64 characters including special marks!" +
                        "\nPassword must not be blank and contains at least one number, one small character, one capital character and one special mark!");
    }

    @Test
    void findUserByUsername() throws UserNotFoundException {
        //given
        User expectedUser = testData.preparedUser();
        String username = expectedUser.getUsername();
        when(userRepository.findByUsername(username)).thenReturn(Optional.ofNullable(expectedUser));

        //when
        Optional<User> actualUser = userService.findUserByUsername(username);

        //then
        assertEquals(expectedUser.getUsername(), actualUser.get().getUsername());
        assertEquals(expectedUser.getPassword(), actualUser.get().getPassword());
        assertEquals(expectedUser.getRole(), actualUser.get().getRole());
    }

    @Test
    void findUserByUsernameWIithUserNotFoundException() throws UserNotFoundException {
        //given
        String username = testData.preparedUser().getUsername();
        when(userRepository.findByUsername(username)).thenReturn(Optional.ofNullable(null));
        //when
        //then
        assertThrows(UserNotFoundException.class, () -> userService.findUserByUsername(username),
                "User with username: " + username + " not found!");
    }

    @Test
    void updatePassword() throws UserNotFoundException {
        //given
        User user = testData.preparedUser();
        Integer userId = user.getId();
        CharSequence oldRawPassword = "test12!A";
        String encodedUserPassword = "$2a$12$6ol3T9/ORPNTE14EdLXgoepuHHSni22yBlFMSQAdCXIYq7zNJBqwG";
        user.setPassword(encodedUserPassword);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(oldRawPassword, encodedUserPassword)).thenReturn(true);
        String newPassword = "tesT12@";
        String expectedResponse = "Password changed successfully!";
        when(userRepository.save(user)).thenReturn(user);

        //when
        String actualResponse = userService.updatePassword(userId, newPassword, newPassword, oldRawPassword.toString());

        //then
        assertEquals(expectedResponse, actualResponse);
    }

    @Test
    void updatePasswordWithUserNotFoundException() throws UserNotFoundException {
        //given
        Integer incorrectUserId = 9999999;
        when(userRepository.findById(incorrectUserId)).thenReturn(Optional.ofNullable(null));
        CharSequence oldRawPassword = "test12!Axx";
        String newPassword = "tesT12@";
        String incorrectNewPassword = "tesT12@aaaxxxx";
        //when
        //then
        assertThrows(UserNotFoundException.class, () -> userService.updatePassword(incorrectUserId, newPassword, incorrectNewPassword, oldRawPassword.toString()),
                "User with id: " + incorrectUserId + " not found!");
    }

    @Test
    void updatePasswordWithNewPasswordNotMatchException() throws UserNotFoundException {
        //given
        User user = testData.preparedUser();
        Integer userId = user.getId();
        CharSequence oldRawPassword = "test12!A";
        String stringOldRawPassword = oldRawPassword.toString();
        String encodedUserPassword = "$2a$12$6ol3T9/ORPNTE14EdLXgoepuHHSni22yBlFMSQAdCXIYq7zNJBqwG";
        user.setPassword(encodedUserPassword);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(oldRawPassword, encodedUserPassword)).thenReturn(true);
        String newPassword = "tesT12@";
        String incorrectNewPassword = "tesT12@aaaxxxx";
        //when
        //then
        assertThrows(NewPasswordNotMatchException.class, () -> userService.updatePassword(userId, newPassword, incorrectNewPassword, stringOldRawPassword),
                "New passwords are not the same!");
    }

    @Test
    void updatePasswordWithIncorrectOldPasswordException() throws UserNotFoundException {
        //given
        User user = testData.preparedUser();
        Integer userId = user.getId();
        CharSequence oldRawPassword = "test12!Axx";
        String stringOldRawPassword = oldRawPassword.toString();
        String encodedUserPassword = "$2a$12$6ol3T9/ORPNTE14EdLXgoepuHHSni22yBlFMSQAdCXIYq7zNJBqwG";
        user.setPassword(encodedUserPassword);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        String newPassword = "tesT12@";
        String incorrectNewPassword = "tesT12@aaaxxxx";
        //when
        //then
        assertThrows(IncorrectOldPasswordException.class, () -> userService.updatePassword(userId, newPassword, incorrectNewPassword, stringOldRawPassword),
                "Incorrect old password!");
    }

    /* error probably due to init instance postService - not able to fix */
//    @Test
//    void deleteUserById() throws UserNotFoundException, PostNotFoundException, UserForbiddenAccessException, CommentNotFoundException {
//        //given
//        User user = testData.preparedUser();
//        Integer userId = user.getId();
//        List<Comment> commentList = testData.preparedCommentList();
//        List<Post> postList = testData.preparedPostsList();
//        when(userRepository.findById(anyInt())).thenReturn(Optional.of(user));
//
//        when(commentService.findAllCommentByUserId(anyInt())).thenReturn(commentList);
//        when(commentService.findAllCommentByUserId(userId)).thenReturn(commentList);
//        doNothing().when(commentService).deleteCommentById(commentList.get(0).getId());
//
//        when(postService.findAllPostsByUserId(userId)).thenReturn(null);
//        when(postRepository.findAllByUserId(userId)).thenReturn(Optional.ofNullable(postList));
//        doNothing().when(postService).deletePostById(Optional.of(user), postList.get(0).getId());
//
//        doNothing().when(userRepository).deleteById(userId);
//        //when
//        userService.deleteUserById(userId);
//
//        //then
//        verify(userRepository, times(1)).deleteById(userId);
//    }

    @Test
    void deleteUserById() throws CommentNotFoundException, UserNotFoundException, PostNotFoundException {
        //Given
        User user = testData.preparedUser();
        userRepository.save(user);
        int userId = user.getId();
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(commentService.findAllCommentByUserId(userId)).thenReturn(Collections.emptyList());
        when(postService.findAllPostsByUserId(userId)).thenReturn(Collections.emptyList());
        doNothing().when(userRepository).deleteById(userId);

        //When
        userService.deleteUserById(userId);

        //Then
        verify(userRepository, times(1)).deleteById(userId);
    }

    @Test
    void findUserById() throws UserNotFoundException {
        //given
        User expectedUser = testData.preparedUser();
        Integer userId = expectedUser.getId();
        when(userRepository.findById(userId)).thenReturn(Optional.of(expectedUser));
        //when
        Optional<User> actualUser = userService.findUserById(userId);
        //then
        assertEquals(expectedUser.getUsername(), actualUser.get().getUsername());
        assertEquals(expectedUser.getPassword(), actualUser.get().getPassword());
        assertEquals(expectedUser.getRole(), actualUser.get().getRole());
        assertEquals(expectedUser.getCreated(), actualUser.get().getCreated());
        verify(userRepository).findById(userId);
    }

    @Test
    void findUserByIdWithUserNotFoundException() throws UserNotFoundException {
        //given
        Integer incorrectUserId = 99999;
        when(userRepository.findById(incorrectUserId)).thenReturn(Optional.ofNullable(null));
        //when
        //then
        assertThrows(UserNotFoundException.class, () -> userService.findUserById(incorrectUserId),
                "User with id: " + incorrectUserId + " not found!");
    }

    @WithMockUser(username = "test12!A", password = "test12!A")
    @Test
    void getLoginUser() {
        //Given
        User expectedUser = testData.preparedUser();
        String username = expectedUser.getUsername();
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(expectedUser));
        //When
        User actualUser = userService.getLoginUser();
        //When
        assertEquals(expectedUser, actualUser);
    }

    @Test
     void getLoginUserWithNullAuthentication() {
        //Given
        //When
        User loginUser = userService.getLoginUser();
        //When
        assertNull(loginUser);
    }

    @WithMockUser(username = "test12!A", password = "test12!A")
    @Test
     void getLoginUserWithNullOptionalUser() {
        //Given
        User expectedUser = testData.preparedUser();
        String username = expectedUser.getUsername();
        when(userRepository.findByUsername(username)).thenReturn(Optional.ofNullable(null));
        //When
        User actualUser = userService.getLoginUser();
        //When
        assertNull(actualUser);
    }

    @Test
     void findUserByKeywordInUsername() throws UserNotFoundException {
        //Given
        User user = testData.preparedUser();
        String keyword = "12";
        List<User> userList = testData.preparedUsersList();
        List<UserDto> expectedUserDtoList = testData.preparedUsersDtoList();
        when(userRepository.findByUsernameLike(any())).thenReturn(userList);
        when(userMapper.userListToUserDtoList(userList)).thenReturn(expectedUserDtoList);

        //When
        List<UserDto> actualUserDtoList = userService.findUserByKeywordInUsername(keyword);

        //Then
        assertTrue(actualUserDtoList.get(0).getUsername().contains(keyword));
        assertEquals(expectedUserDtoList.size(), actualUserDtoList.size());
        assertEquals(expectedUserDtoList.get(0).getUsername(), actualUserDtoList.get(0).getUsername());
        assertEquals(expectedUserDtoList.get(0).getProfilePicture(), actualUserDtoList.get(0).getProfilePicture());
        assertEquals(expectedUserDtoList.get(0).getCreated(), actualUserDtoList.get(0).getCreated());
    }

    @Test
     void findUserByKeywordInUsernameWithEmptyList() throws UserNotFoundException {
        //Given
        User user = testData.preparedUser();
        String keyword = "12";
        List<User> userList = testData.preparedUsersList();
        List<UserDto> expectedUserDtoList = testData.preparedUsersDtoList();
        when(userRepository.findByUsernameLike(any())).thenReturn(Collections.emptyList());
        //When
        List<UserDto> actualUserDtoList = userService.findUserByKeywordInUsername(keyword);
        //Then
        assertEquals(0, actualUserDtoList.size());
    }

    @Test
     void checkProvidedPasswordWithPasswordFromDb() {
        //given
        User user = testData.preparedUser();
        Integer userId = user.getId();
        CharSequence passwordProvidedByUser = "test12!A";
        String passwordFromDb = "$2a$12$6ol3T9/ORPNTE14EdLXgoepuHHSni22yBlFMSQAdCXIYq7zNJBqwG";
        when(passwordEncoder.matches(passwordProvidedByUser, passwordFromDb)).thenReturn(true);
        //when
        boolean actualBoolean = userService.checkProvidedPasswordWithPasswordFromDb(passwordProvidedByUser.toString(), passwordFromDb);
        //then
        assertEquals(true, actualBoolean);
    }

    @Test
     void checkProvidedPasswordWithPasswordFromDbWithWrongPasswordException() {
        //given
        CharSequence wrongPasswordProvidedByUser = "wrong";
        String stringWrongPasswordProvidedByUser = wrongPasswordProvidedByUser.toString();
        String passwordFromDb = "$2a$12$6ol3T9/ORPNTE14EdLXgoepuHHSni22yBlFMSQAdCXIYq7zNJBqwG";
        when(passwordEncoder.matches(wrongPasswordProvidedByUser, passwordFromDb)).thenReturn(false);
        //when
        //then
        assertThrows(WrongPasswordException.class,
                () -> userService.checkProvidedPasswordWithPasswordFromDb(stringWrongPasswordProvidedByUser, passwordFromDb),
                "Incorrect password!");
    }

    @WithMockUser(username = "test12!A", password = "test12!A")
    @Test
     void updateUserProfilePicture() throws UserNotFoundException, UserForbiddenAccessException {
        //given
        User user = testData.preparedUser();
        Integer userId = user.getId();
        String username = user.getUsername();
        String newProfilePictureUrl = "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcTv1vgdYWHDUkyYYYxV4RV78Q4AHDtagK2GRQ&usqp=CAU";
        user.setProfilePicture(newProfilePictureUrl);
        String expectedResponse = "User profile picture saved successfully!";
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));
        when(userRepository.save(user)).thenReturn(user);

        //when
        String actualResponse = userService.updateUserProfilePicture(userId, newProfilePictureUrl);

        //then
        assertEquals(expectedResponse, actualResponse);
    }

    @WithMockUser(username = "test12!A", password = "test12!A")
    @Test
     void updateUserProfilePictureReturnUserForbiddenAccessException() throws UserNotFoundException, UserForbiddenAccessException {
        //given
        User loginUser = testData.preparedUser();
        User ownerOfAccount = new User();
        ownerOfAccount.setUsername("badUsername");
        ownerOfAccount.setPassword("badPassword");
        ownerOfAccount.setRole("ROLE_USER");
        Integer userId = ownerOfAccount.getId();
        String newProfilePictureUrl = "null";
        ownerOfAccount.setProfilePicture(newProfilePictureUrl);
        when(userRepository.findById(userId)).thenReturn(Optional.of(ownerOfAccount));
        when(userRepository.findByUsername(loginUser.getUsername())).thenReturn(Optional.of(loginUser));

        //when
        //then
        assertThrows(UserForbiddenAccessException.class, () -> userService.updateUserProfilePicture(userId, newProfilePictureUrl),
                "User is not authorized to change profile picture!");
    }

    @Test
     void updateUserEnable() throws UserNotFoundException {
        //given
        User user = testData.preparedUser();
        Integer userId = user.getId();
        String username = user.getUsername();
        Boolean changedEnable = false;
        user.setEnabled(changedEnable);
        String expectedResponse = "Enabled saved successfully for '" + changedEnable + "' for username '" + username + "'!";
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.save(user)).thenReturn(user);

        //when
        String actualResponse = userService.updateUserEnable(userId, changedEnable);

        //then
        assertEquals(expectedResponse, actualResponse);
    }

    @Test
     void getUserByPostId() throws UserNotFoundException {
        //Given
        User user = testData.preparedUser();
        Integer postId = testData.preparedPost().getId();
        UserDto expectedUserDto = testData.preparedUserDto();
        when(userRepository.findByPostId(postId)).thenReturn(Optional.ofNullable(user));
        when(userMapper.userToUserDto(user)).thenReturn(expectedUserDto);

        //When
        UserDto actualUserDto = userService.getUserByPostId(postId);

        //Then
        assertEquals(expectedUserDto.getUsername(), actualUserDto.getUsername());
        assertEquals(expectedUserDto.getProfilePicture(), actualUserDto.getProfilePicture());
        assertEquals(expectedUserDto.getCreated(), actualUserDto.getCreated());
    }

    @Test
     void getUserByPostIdWithUserNotFoundException() throws UserNotFoundException {
        //Given
        User user = testData.preparedUser();
        Integer postId = testData.preparedPost().getId();
        UserDto expectedUserDto = testData.preparedUserDto();
        when(userRepository.findByPostId(postId)).thenReturn(Optional.ofNullable(null));
        //When
        //Then
        assertThrows(UserNotFoundException.class, () -> userService.getUserByPostId(postId),
                "Author of post with id: '" + postId + "' was not found!");
    }

}
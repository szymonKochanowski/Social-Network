package com.serwisspolecznosciowy.Application.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.serwisspolecznosciowy.Application.dto.NewUserDto;
import com.serwisspolecznosciowy.Application.dto.UserDto;
import com.serwisspolecznosciowy.Application.entity.User;
import com.serwisspolecznosciowy.Application.exception.*;
import com.serwisspolecznosciowy.Application.service.UserService;
import com.serwisspolecznosciowy.Application.testData.TestData;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@WithMockUser(username = "admin12!A", password = "admin12!A", roles = {"ADMIN"})
class UserControllerTest {

    @Autowired
    MockMvc mockMvc;
    @Autowired
    ObjectMapper objectMapper;
    @MockBean
    UserService userService;
    @Autowired
    TestData testData;

    @Test
    void getUsers() throws Exception {
        //Given
        List<User> expectedUsersList = testData.preparedUsersList();
        when(userService.findAllUsers()).thenReturn(expectedUsersList);
        //When
        MvcResult mvcResult = mockMvc.perform(get("/user/all"))
                .andDo(print())
                .andExpect(status().is(200))
                .andReturn();
        //Then
        List<User> actualUserList = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), new TypeReference<List<User>>() {
        });
        assertEquals(expectedUsersList.get(0).getUsername(), actualUserList.get(0).getUsername());
        assertEquals(expectedUsersList.get(0).getId(), actualUserList.get(0).getId());
        assertEquals(expectedUsersList.get(0).getCreated(), actualUserList.get(0).getCreated());
        assertEquals(expectedUsersList.get(0).getRole(), actualUserList.get(0).getRole());
    }

    @Test
    void findUserById() throws Exception {
        //Given
        Integer userId = testData.preparedUser().getId();
        Optional<User> expectedOptionalUser = Optional.ofNullable(testData.preparedUser());
        when(userService.findUserById(userId)).thenReturn(expectedOptionalUser);
        //When
        MvcResult mvcResult = mockMvc.perform(get("/user/{id}", userId))
                .andDo(print())
                .andExpect(status().is(200))
                .andReturn();
        //Then
        Optional<User> actualOptionalUser = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), new TypeReference<Optional<User>>() {
        });
        assertEquals(expectedOptionalUser.get().getUsername(), actualOptionalUser.get().getUsername());
        assertEquals(expectedOptionalUser.get().getId(), actualOptionalUser.get().getId());
        assertEquals(expectedOptionalUser.get().getCreated(), actualOptionalUser.get().getCreated());
        assertEquals(expectedOptionalUser.get().getPassword(), actualOptionalUser.get().getPassword());
        assertEquals(expectedOptionalUser.get().getProfilePicture(), actualOptionalUser.get().getProfilePicture());
    }

    @Test
    void findUserByIdWithUserNotFoundException() throws Exception {
        //Given
        Integer incorrectUserId = 99999;
        when(userService.findUserById(incorrectUserId)).thenThrow(UserNotFoundException.class);
        //When
        mockMvc.perform(get("/user/{id}", incorrectUserId))
                .andDo(print())
                .andExpect(status().is(404))
                .andReturn();
    }

    @Test
    void getUserDtoListByKeywordInUsername() throws Exception {
        //Given
        String keyword = "małysz";
        User user = testData.preparedUser();
        user.setUsername("Adam MałysZ");
        List<UserDto> expectedUserDtoList = testData.preparedUsersDtoList();
        when(userService.findUserByKeywordInUsername(keyword)).thenReturn(expectedUserDtoList);
        //When
        MvcResult mvcResult = mockMvc.perform(get("/user/all/username/dto")
                        .param("keyword", keyword))
                .andDo(print())
                .andExpect(status().is(200))
                .andReturn();
        //Then
        List<UserDto> actualUserDtoList = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), new TypeReference<List<UserDto>>() {
        });
        assertEquals(expectedUserDtoList.get(0).getUsername(), actualUserDtoList.get(0).getUsername());
        assertEquals(expectedUserDtoList.get(0).getProfilePicture(), actualUserDtoList.get(0).getProfilePicture());
        assertEquals(expectedUserDtoList.get(0).getCreated(), actualUserDtoList.get(0).getCreated());
    }

    @Test
    void getUserDtoListByKeywordInUsernameWithUserNotFoundException() throws Exception {
        //Given
        String keyword = "małysz";
        when(userService.findUserByKeywordInUsername(keyword)).thenReturn(Collections.emptyList());
        //When
        mockMvc.perform(get("/user/all/username/dto")
                        .param("keyword", keyword))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andReturn();
    }

    @WithMockUser(username = "test12Az~!", password = "test12Az~!")
    @Test
    void addNewUser() throws Exception {
        //Given
        User newUser = testData.preparedUser();
        newUser.setPassword("test12Az~!");
        newUser.setUsername("test12Az~!");
        UserDto expectedUserDto = testData.preparedUserDto();
        NewUserDto newUserDto = testData.preparedNewUserDto();
        when(userService.addNewUser(newUserDto)).thenReturn(expectedUserDto);
        //When
        MvcResult mvcResult = mockMvc.perform(post("/user/add/dto")
                        .content(objectMapper.writeValueAsString(newUserDto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().is(201))
                .andReturn();
        //Then
        UserDto actualUserDto = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), UserDto.class);
        assertEquals(expectedUserDto.getUsername(), actualUserDto.getUsername());
        assertEquals(expectedUserDto.getProfilePicture(), actualUserDto.getProfilePicture());
        assertEquals(expectedUserDto.getCreated(), actualUserDto.getCreated());
    }

    @Test
    void addNewUserWithRoleAdmin() throws Exception {
        //Given
        String password = "test12Az~!";
        String username = "test12Az~!";
        String profilePicture = "https://cdn-icons-png.flaticon.com/512/149/149071.png";

        User newAdmin = testData.preparedAdmin();
        newAdmin.setPassword(password);
        newAdmin.setUsername(username);
        newAdmin.setProfilePicture(profilePicture);

        UserDto expectedUserDto = testData.preparedAdminDto();

        NewUserDto newUserDto = testData.preparedNewAdminDto();

        when(userService.addNewUser(newUserDto)).thenReturn(expectedUserDto);
        //When
        MvcResult mvcResult = mockMvc.perform(post("/user/add/dto")
                        .content(objectMapper.writeValueAsString(newUserDto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().is(201))
                .andReturn();
        //Then
        UserDto actualUserDto = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), UserDto.class);
        assertEquals(expectedUserDto.getUsername(), actualUserDto.getUsername());
        assertEquals(expectedUserDto.getProfilePicture(), actualUserDto.getProfilePicture());
        assertEquals(expectedUserDto.getCreated(), actualUserDto.getCreated());
    }

    @WithMockUser(username = "test12!A", password = "test12!A")
    @Test
    void addNewUserWithDuplicateUsernameException() throws Exception {
        //Given
        NewUserDto newUserDto = testData.preparedNewUserDto();
        when(userService.addNewUser(newUserDto)).thenThrow(DuplicateUsernameException.class);
        //When
        mockMvc.perform(post("/user/add/dto")
                        .content(objectMapper.writeValueAsString(newUserDto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().is(409))
                .andReturn();
    }

    @Test
    void addNewUserWithIncorrectNewPasswordException() throws Exception {
        //Given
        User newUser = testData.preparedUser();
        newUser.setPassword("test");
        UserDto expectedUserDto = testData.preparedUserDto();
        NewUserDto newUserDto = testData.preparedNewUserDto();
        when(userService.addNewUser(newUserDto)).thenThrow(IncorrectNewPasswordException.class);
        //When
        mockMvc.perform(post("/user/add/dto")
                        .content(objectMapper.writeValueAsString(newUserDto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().is(400))
                .andReturn();
    }

    @Test
    void shouldLogin() throws Exception {
        //Given
        User admin = testData.preparedAdmin();
        String username = admin.getUsername();
        String password = admin.getPassword();
        when(userService.findUserByUsername(username)).thenReturn(Optional.of(admin));
        when(userService.checkProvidedPasswordWithPasswordFromDb(password, password)).thenReturn(true);
        //When
        mockMvc.perform(post("/user/login")
                        .param("username", username)
                        .param("password", password))
                .andDo(print())
                .andExpect(status().is(204))
                .andReturn();
    }

    @Test
    void shouldLoginWithUserNotFoundException() throws Exception {
        //Given
        User admin = testData.preparedAdmin();
        String username = admin.getUsername();
        String password = admin.getPassword();
        when(userService.findUserByUsername(username)).thenThrow(UserNotFoundException.class);
        //When
        mockMvc.perform(post("/user/login")
                        .param("username", username)
                        .param("password", password))
                .andDo(print())
                .andExpect(status().is(404))
                .andReturn();
    }

    @Test
    void shouldLoginWithWrongPasswordException() throws Exception {
        //Given
        User admin = testData.preparedAdmin();
        String username = admin.getUsername();
        String password = admin.getPassword();
        when(userService.findUserByUsername(username)).thenReturn(Optional.of(admin));
        when(userService.checkProvidedPasswordWithPasswordFromDb(password, password)).thenThrow(WrongPasswordException.class);
        //When
        mockMvc.perform(post("/user/login")
                        .param("username", username)
                        .param("password", password))
                .andDo(print())
                .andExpect(status().is(401))
                .andReturn();
    }

    @Test
    void updatePasswordByUserId() throws Exception {
        //Given
        User user = testData.preparedUser();
        Integer userId = user.getId();
        String newPassword = "test1234@A!";
        String oldPassword = "test12!A";
        String response = "Password changed successfully!";
        when(userService.updatePassword(userId, newPassword, newPassword, oldPassword)).thenReturn(response);
        //When
        mockMvc.perform(patch("/user/password/dto/{userId}", userId)
                        .param("newPassword1", newPassword)
                        .param("newPassword2", newPassword)
                        .param("oldPassword", oldPassword))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();
    }

    @Test
    void updatePasswordByUserIdWithIncorrectOldPasswordException() throws Exception {
        //Given
        User user = testData.preparedUser();
        Integer userId = user.getId();
        String newPassword = "test1234@A!";
        String incorrectOldPassword = "test";
        when(userService.updatePassword(userId, newPassword, newPassword, incorrectOldPassword)).thenThrow(IncorrectOldPasswordException.class);
        //When
        mockMvc.perform(patch("/user/password/dto/{userId}", userId)
                        .param("newPassword1", newPassword)
                        .param("newPassword2", newPassword)
                        .param("oldPassword", incorrectOldPassword))
                .andDo(print())
                .andExpect(status().is(401))
                .andReturn();
    }

    @Test
    void updatePasswordByUserIdWithNewPasswordNotMatchException() throws Exception {
        //Given
        User user = testData.preparedUser();
        Integer userId = user.getId();
        String newPassword1 = "test1234@";
        String newPassword2 = "test1234@A!ds";
        String oldPassword = user.getPassword();
        when(userService.updatePassword(userId, newPassword1, newPassword2, oldPassword)).thenThrow(NewPasswordNotMatchException.class);
        //When
        mockMvc.perform(patch("/user/password/dto/{userId}", userId)
                        .param("newPassword1", newPassword1)
                        .param("newPassword2", newPassword2)
                        .param("oldPassword", oldPassword))
                .andDo(print())
                .andExpect(status().is(400))
                .andReturn();
    }

    @Test
    void updateUserProfilePictureByUserId() throws Exception, UserForbiddenAccessException {
        //Given
        User user = testData.preparedUser();
        Integer userId = user.getId();
        String userNewProfilePicture = "https://cdn-icons-png.flaticon.com/512/149/149071.png";
        String response = "User profile picture saved successfully!";
        when(userService.updateUserProfilePicture(userId, userNewProfilePicture)).thenReturn(response);
        //When
        mockMvc.perform(patch("/user/picture/dto/{userId}", userId)
                        .param("profilePictureUrl", userNewProfilePicture))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();
    }

    @Test
    void updateUserProfilePictureByUserIdWithUserNotFoundException() throws Exception, UserForbiddenAccessException {
        //Given
        User user = testData.preparedUser();
        Integer incorrectUserId = 999999999;
        String userNewProfilePicture = "https://cdn-icons-png.flaticon.com/512/149/149071.png";
        when(userService.updateUserProfilePicture(incorrectUserId, userNewProfilePicture)).thenThrow(UserNotFoundException.class);
        //When
        mockMvc.perform(patch("/user/picture/dto/{userId}", incorrectUserId)
                        .param("profilePictureUrl", userNewProfilePicture))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andReturn();
    }

    @Test
    void updateUserProfilePictureByUserIdWithUserForbiddenAccessException() throws Exception, UserForbiddenAccessException {
        //Given
        Integer userIdWithoutPermission = 99999;
        String userNewProfilePicture = "https://cdn-icons-png.flaticon.com/512/149/149071.png";
        String expectedErrorMessage = "User is not authorized to change profile picture!";
        when(userService.updateUserProfilePicture(userIdWithoutPermission, userNewProfilePicture)).thenThrow(UserForbiddenAccessException.class);
        //When
        mockMvc.perform(patch("/user/picture/dto/{userId}", userIdWithoutPermission)
                        .param("profilePictureUrl", userNewProfilePicture))
                .andDo(print())
                .andExpect(status().isForbidden())
                .andReturn();
    }

    @Test
    void updateUserEnabledByUserId() throws Exception, UserForbiddenAccessException {
        //Given
        User user = testData.preparedUser();
        Integer userId = user.getId();
        Boolean enabled = false;
        String expectedResponse = "Enabled saved successfully for '" + enabled + "' for username '" + user.getUsername() + "'!";
        when(userService.updateUserEnable(userId, enabled)).thenReturn(expectedResponse);
        //When
        mockMvc.perform(patch("/user/enable/{userId}", userId)
                        .param("enabled", String.valueOf(enabled)))
                .andDo(print())
                .andExpect(status().is(200))
                .andReturn();
    }

    @Test
    void updateUserEnabledByUserIdWithUserNotFoundException() throws Exception, UserForbiddenAccessException {
        //Given
        Integer incorrectUserId = 9999;
        Boolean enabled = false;
        when(userService.updateUserEnable(incorrectUserId, enabled)).thenThrow(UserNotFoundException.class);
        //When
        mockMvc.perform(patch("/user/enable/{userId}", incorrectUserId)
                        .param("enabled", String.valueOf(enabled)))
                .andDo(print())
                .andExpect(status().is(404))
                .andReturn();
    }

    @Test
    void deleteUserByIdByAdmin() throws Exception, PostNotFoundException, UserForbiddenAccessException, CommentNotFoundException {
        //Given
        User user = testData.preparedUser();
        Integer userId = user.getId();
        doNothing().when(userService).deleteUserById(userId);
        //When
        mockMvc.perform(delete("/user/delete/dto/{userId}", userId))
                .andDo(print())
                .andExpect(status().is(204))
                .andReturn();
    }

    @WithMockUser(username = "test12!A", password = "test12!A")
    @Test
    void deleteUserByIdByOwner() throws Exception, PostNotFoundException, UserForbiddenAccessException, CommentNotFoundException {
        //Given
        User user = testData.preparedUser();
        Integer userId = user.getId();
        doNothing().when(userService).deleteUserById(userId);
        //When
        mockMvc.perform(delete("/user/delete/dto/{userId}", userId))
                .andDo(print())
                .andExpect(status().is(204))
                .andReturn();
    }

    @Test
    void deleteUserByIdWithUserNotFoundException() throws Exception, PostNotFoundException, UserForbiddenAccessException, CommentNotFoundException {
        //Given
        Integer userId = testData.preparedUser().getId();
        doThrow(UserNotFoundException.class).when(userService).deleteUserById(userId);
        //When
        mockMvc.perform(delete("/user/delete/dto/{userId}", userId))
                .andDo(print())
                .andExpect(status().is(404))
                .andReturn();
    }

    @Test
    void deleteUserByIdWithPostNotFoundException() throws Exception, PostNotFoundException, UserForbiddenAccessException, CommentNotFoundException {
        //Given
        Integer userId = testData.preparedUser().getId();
        doThrow(PostNotFoundException.class).when(userService).deleteUserById(userId);
        //When
        mockMvc.perform(delete("/user/delete/dto/{userId}", userId))
                .andDo(print())
                .andExpect(status().is(404))
                .andReturn();
    }

    @Test
    void deleteUserByIdWithCommentNotFoundException() throws Exception, PostNotFoundException, UserForbiddenAccessException, CommentNotFoundException {
        //Given
        Integer userId = testData.preparedUser().getId();
        doThrow(CommentNotFoundException.class).when(userService).deleteUserById(userId);
        //When
        mockMvc.perform(delete("/user/delete/dto/{userId}", userId))
                .andDo(print())
                .andExpect(status().is(404))
                .andReturn();
    }

    @WithMockUser(username = "test", password = "test")
    @Test
    void deleteUserByIdWithUserForbiddenAccessException() throws Exception, PostNotFoundException, UserForbiddenAccessException, CommentNotFoundException {
        //Given
        Integer userId = testData.preparedUser().getId();
        doThrow(UserForbiddenAccessException.class).when(userService).deleteUserById(userId);
        //When
        mockMvc.perform(delete("/user/delete/dto/{userId}", userId))
                .andDo(print())
                .andExpect(status().is(403))
                .andReturn();
    }

    @Test
    void getUserByPostId() throws Exception {
        //Given
        Integer postId = testData.preparedPost().getId();
        UserDto expectedUserDto = testData.preparedUserDto();
        when(userService.getUserByPostId(postId)).thenReturn(expectedUserDto);
        //When
        MvcResult mvcResult = mockMvc.perform(get("/user/post/{postId}", postId))
                .andDo(print())
                .andExpect(status().is(200))
                .andReturn();
        //Then
        UserDto actualUserDto = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), UserDto.class);
        assertEquals(expectedUserDto.getUsername(), actualUserDto.getUsername());
        assertEquals(expectedUserDto.getUpdated(), actualUserDto.getUpdated());
        assertEquals(expectedUserDto.getCreated(), actualUserDto.getCreated());
        assertEquals(expectedUserDto.getProfilePicture(), actualUserDto.getProfilePicture());
    }

    @Test
    void getUserByPostIdWithUserNotFoundException() throws Exception {
        //Given
        Integer incorrectPostId = 99999;
        when(userService.getUserByPostId(incorrectPostId)).thenThrow(UserNotFoundException.class);
        //When
        mockMvc.perform(get("/user/post/{postId}", incorrectPostId))
                .andDo(print())
                .andExpect(status().is(404))
                .andReturn();
    }

}
package org.example.block2travelblog.service;

import org.example.block2travelblog.dto.SaveUserDto;
import org.example.block2travelblog.dto.UserDto;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service Interface for users operations.
 */
public interface UserService {

    List<UserDto> getAllUsers();

    UserDto addUser(SaveUserDto saveUserDto);

    UserDto updateUser(Long id, SaveUserDto saveUserDto);

    void deleteUser(Long id);

    UserDto getOrCreateOAuthUser(String email, String name);
}

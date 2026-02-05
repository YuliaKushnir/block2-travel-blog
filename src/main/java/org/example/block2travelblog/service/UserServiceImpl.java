package org.example.block2travelblog.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.example.block2travelblog.data.User;
import org.example.block2travelblog.dto.SaveUserDto;
import org.example.block2travelblog.dto.UserDto;
import org.example.block2travelblog.exception.CreationException;
import org.example.block2travelblog.exception.DuplicateEmailException;
import org.example.block2travelblog.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * Service implementation for posts operations.
 */
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    /**
     * Retrieves all users
     *
     * @return list of users data
     * @throws EntityNotFoundException when users not found
     */
    @Override
    public List<UserDto> getAllUsers() {
        List<User> usersList = userRepository.findAll();

        if (usersList.isEmpty()) {
            throw new EntityNotFoundException("User not found");
        }

        List<UserDto> userDtoList = new ArrayList<>();
        for (User user : usersList) {
            userDtoList.add(mapUserToUserDto(user));
        }

        return userDtoList;
    }

    /**
     * Create a new user
     *
     * @param saveUserDto new user data
     * @return created user
     */
    @Override
    public UserDto addUser(SaveUserDto saveUserDto) {
        checkIfUserExistsWithEmail(saveUserDto.getEmail(), null);

        User user = mapSaveUserDtoToUser(saveUserDto);
        User createdUser = userRepository.save(user);

        if (createdUser == null || createdUser.getId() == null) {
            throw new CreationException("Failed to create user");
        }

        return mapUserToUserDto(createdUser);
    }

    /**
     * Updates an existing user
     *
     * @param id user ID
     * @param saveUserDto user new data
     * @return updated user
     * @throws EntityNotFoundException if user not found by id
     * @throws DuplicateEmailException if user with same email exists in db
     */
    @Override
    public UserDto updateUser(Long id, SaveUserDto saveUserDto) {
        checkIfUserExistsWithEmail(saveUserDto.getEmail(), id);

        User user = userRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("User not found"));
        user.setName(saveUserDto.getName());
        user.setEmail(saveUserDto.getEmail());
        user.setPassword(saveUserDto.getPassword());

        User updatedUser = userRepository.save(user);

        return mapUserToUserDto(updatedUser);
    }

    /**
     * Deletes user
     *
     * @param id user ID
     * @throws EntityNotFoundException if user not found by id
     */
    @Override
    public void deleteUser(Long id) {
        if(!userRepository.existsById(id)) {
            throw new EntityNotFoundException("User not found");
        }
        userRepository.deleteById(id);
    }

    @Override
    @Transactional
    public UserDto getOrCreateOAuthUser(String email, String name){
        return mapUserToUserDto(userRepository.findByEmail(email)
                .orElseGet(() -> {
                    User user = new User();
                    user.setEmail(email);
                    user.setName(name);
                    user.setPassword(null);
                    return userRepository.save(user);
                }));
    }

    private void checkIfUserExistsWithEmail(String email, Long userId) {
        userRepository.findByEmail(email)
                .ifPresent(user -> {
                    if (!user.getId().equals(userId)) {
                        throw new DuplicateEmailException("User with email " + email + " already exists");
                    }
                });
    }

    private UserDto mapUserToUserDto(User user) {
        UserDto userDto = new UserDto();
        userDto.setId(user.getId());
        userDto.setName(user.getName());
        userDto.setEmail(user.getEmail());
        return userDto;
    }

    private User mapSaveUserDtoToUser(SaveUserDto saveUserDto) {
        User user = new User();
        user.setName(saveUserDto.getName());
        user.setEmail(saveUserDto.getEmail());
        user.setPassword(saveUserDto.getPassword());
        return user;
    }
}

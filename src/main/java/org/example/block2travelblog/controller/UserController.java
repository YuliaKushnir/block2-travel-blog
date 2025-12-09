package org.example.block2travelblog.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.example.block2travelblog.dto.SaveUserDto;
import org.example.block2travelblog.dto.UserDto;
import org.example.block2travelblog.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for User operations.
 */

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /**
     * Retrieves all users
     *
     * @return list of users dto
     */
    @GetMapping()
    @ResponseStatus(HttpStatus.OK)
    public List<UserDto> getAllUsers() {
        return userService.getAllUsers();
    }

    /**
     * Creates a new user
     *
     * @param saveUserDto user data
     * @return created user data
     */
    @PostMapping()
    @ResponseStatus(HttpStatus.CREATED)
    public UserDto addUser(@Valid @RequestBody SaveUserDto saveUserDto) {
        return userService.addUser(saveUserDto);
    }

    /**
     * Updates existing user by id
     *
     * @param id user id
     * @param saveUserDto user data
     * @return
     */
    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public UserDto updateUser(@PathVariable @NotNull @Min(1) Long id,
                              @Valid @RequestBody SaveUserDto saveUserDto) {
        return userService.updateUser(id, saveUserDto);
    }

    /**
     * Deletes a user by id
     *
     * @param id user id
     */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteUser(@PathVariable @NotNull @Min(1) Long id) {
        userService.deleteUser(id);
    }

}

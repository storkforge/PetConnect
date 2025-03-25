package se.storkforge.petconnect.controller;

import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;
import se.storkforge.petconnect.entity.User;
import se.storkforge.petconnect.exception.UserNotFoundException;
import se.storkforge.petconnect.service.UserService;

import java.util.List;

@Controller
public class UserGraphQLController {

    private final UserService userService;

    public UserGraphQLController(UserService userService) {
        this.userService = userService;
    }

    @QueryMapping
    public List<User> getAllUsers() {
        return userService.getAllUsers();
    }

    @QueryMapping
    public User getUserById(@Argument Long id) {
        return userService.getUserById(id);
    }

    @QueryMapping
    public User getUserByUsername(@Argument String username) {
        return userService.getUserByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("User not found"));
    }

    @MutationMapping
    public User createUser(@Argument("user") User userInput) {
        return userService.createUser(userInput);
    }

    @MutationMapping
    public User updateUser(@Argument Long id, @Argument("user") User userInput) {
        return userService.updateUser(id, userInput);
    }

    @MutationMapping
    public Boolean deleteUser(@Argument Long id) {
        userService.deleteUser(id);
        return true;
    }
}
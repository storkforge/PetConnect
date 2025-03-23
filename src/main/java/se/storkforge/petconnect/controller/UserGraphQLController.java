package se.storkforge.petconnect.controller;

import entity.User;
import se.storkforge.petconnect.service.UserService;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

@Controller
public class UserGraphQLController {
    private final UserService userService;

    public UserGraphQLController(UserService userService) {
        this.userService = userService;
    }

    @QueryMapping
    public Iterable<User> getAllUsers() {
        return userService.findAll();
    }

    @QueryMapping
    public User getUser(@Argument Long id) {
        return userService.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));
    }

    @MutationMapping
    public User createUser(@Argument String username,
                           @Argument String email,
                           @Argument String password) { // Password is now stored as plain text
        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(password); // Storing plain text password
        return userService.save(user);
    }

    @MutationMapping
    public User updateUser(@Argument Long id,
                           @Argument String username,
                           @Argument String email) {
        User user = userService.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));
        if (username != null) user.setUsername(username);
        if (email != null) user.setEmail(email);
        return userService.save(user);
    }

    @MutationMapping
    public Boolean deleteUser(@Argument Long id) {
        userService.delete(id);
        return true;
    }
}
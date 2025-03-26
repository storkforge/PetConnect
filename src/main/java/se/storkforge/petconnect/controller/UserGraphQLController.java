package se.storkforge.petconnect.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;
import se.storkforge.petconnect.dto.PaginationInfo;
import se.storkforge.petconnect.dto.UserPage;
import se.storkforge.petconnect.dto.UserInputDTO;
import se.storkforge.petconnect.dto.UserUpdateInputDTO;
import se.storkforge.petconnect.entity.User;
import se.storkforge.petconnect.exception.UserNotFoundException;
import se.storkforge.petconnect.service.UserService;

@Controller
public class UserGraphQLController {

    private final UserService userService;

    public UserGraphQLController(UserService userService) {
        this.userService = userService;
    }

    @QueryMapping
    public UserPage getAllUsers(@Argument Integer page, @Argument Integer size) {
        PageRequest pageRequest = PageRequest.of(page != null ? page : 0, size != null ? size : 10);
        Page<User> userPage = userService.getAllUsers(pageRequest);

        return new UserPage(
                userPage.getContent(),
                new PaginationInfo(
                        (int) userPage.getTotalElements(),
                        userPage.getTotalPages(),
                        userPage.getNumber(),
                        userPage.getSize()
                )
        );
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
    public User createUser(@Argument("user") UserInputDTO userInput) {
        User user = new User();
        user.setUsername(userInput.username());
        user.setEmail(userInput.email());
        user.setPassword(userInput.password());
        return userService.createUser(user);
    }

    @MutationMapping
    public User updateUser(@Argument Long id, @Argument("user") UserUpdateInputDTO userInput) {
        User existingUser = userService.getUserById(id);

        if (userInput.username() != null) {
            existingUser.setUsername(userInput.username());
        }
        if (userInput.email() != null) {
            existingUser.setEmail(userInput.email());
        }
        if (userInput.password() != null) {
            existingUser.setPassword(userInput.password());
        }

        return userService.updateUser(id, existingUser);
    }

    @MutationMapping
    public Boolean deleteUser(@Argument Long id) {
        userService.deleteUser(id);
        return true;
    }
}
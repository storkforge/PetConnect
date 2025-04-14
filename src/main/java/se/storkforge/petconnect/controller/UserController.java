package se.storkforge.petconnect.controller;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import se.storkforge.petconnect.dto.UserRequestDTO;
import se.storkforge.petconnect.dto.UserResponseDTO;
import se.storkforge.petconnect.entity.User;
import se.storkforge.petconnect.exception.UserNotFoundException;
import se.storkforge.petconnect.service.UserService;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {
    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public List<UserResponseDTO> getAllUsers() {
        return userService.getAllUsers().stream()
                .map(UserResponseDTO::fromEntity)
                .toList();
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponseDTO> getUserById(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(
                    UserResponseDTO.fromEntity(userService.getUserById(id))
            );
        } catch (UserNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping
    public ResponseEntity<UserResponseDTO> createUser(@Valid @RequestBody UserRequestDTO userRequest) {
        try {
            User user = new User();
            user.setUsername(userRequest.username());
            user.setEmail(userRequest.email());
            user.setPassword(userRequest.password());

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(UserResponseDTO.fromEntity(userService.createUser(user)));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserResponseDTO> updateUser(
            @PathVariable Long id,
            @Valid @RequestBody UserRequestDTO userRequest) {
        try {
            User user = new User();
            user.setUsername(userRequest.username());
            user.setEmail(userRequest.email());
            user.setPassword(userRequest.password());

            return ResponseEntity.ok(
                    UserResponseDTO.fromEntity(userService.updateUser(id, user))
            );
        } catch (UserNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        try {
            userService.deleteUser(id);
            return ResponseEntity.noContent().build();
        } catch (UserNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/{id}/PFP")
    public ResponseEntity<String> uploadPetProfilePicture(
            @PathVariable Long id, @RequestParam("file") MultipartFile file) {
        userService.uploadProfilePicture(id, file);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{id}/PFP")
    public ResponseEntity<Resource> getPetProfilePicture(
            @PathVariable Long id) {
        Resource resource = userService.getProfilePicture(id);
        return new ResponseEntity<>(resource, HttpStatus.OK);
    }
}
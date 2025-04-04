package se.storkforge.petconnect.service;

import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import se.storkforge.petconnect.entity.User;
import se.storkforge.petconnect.exception.UserNotFoundException;
import se.storkforge.petconnect.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@Transactional
public class UserService {
    private final FileStorageService fileStorageService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private static final String EMAIL_REGEX = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";
    private final Pattern emailPattern = Pattern.compile(EMAIL_REGEX);

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, FileStorageService fileStorageService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.fileStorageService = fileStorageService;
    }

    public User createUser(User user) {
        validateNewUser(user);
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userRepository.save(user);
    }

    private void validateNewUser(User user) {
        if (userRepository.findByUsername(user.getUsername()).isPresent()) {
            throw new IllegalArgumentException("Username already exists.");
        }
        if (userRepository.findByEmail(user.getEmail()).isPresent()) {
            throw new IllegalArgumentException("Email already exists.");
        }
        if (isInvalidEmail(user.getEmail())) {
            throw new IllegalArgumentException("Invalid email format.");
        }
    }

    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User with id " + id + " not found"));
    }

    public Optional<User> getUserByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public Optional<User> getUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public Page<User> getAllUsers(Pageable pageable) {
        return userRepository.findAll(pageable);
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public User updateUser(Long id, User updatedUser) {
        User existingUser = getUserById(id);
        updateUserFields(existingUser, updatedUser);
        return userRepository.save(existingUser);
    }

    private void updateUserFields(User existingUser, User updatedUser) {
        if (updatedUser.getUsername() != null && !updatedUser.getUsername().isEmpty()) {
            validateUsernameUpdate(existingUser, updatedUser.getUsername());
            existingUser.setUsername(updatedUser.getUsername());
        }

        if (updatedUser.getEmail() != null && !updatedUser.getEmail().isEmpty()) {
            validateEmailUpdate(existingUser, updatedUser.getEmail());
            existingUser.setEmail(updatedUser.getEmail());
        }

        if (updatedUser.getPassword() != null && !updatedUser.getPassword().isEmpty()) {
            existingUser.setPassword(passwordEncoder.encode(updatedUser.getPassword()));
        }
    }

    private void validateUsernameUpdate(User existingUser, String newUsername) {
        if (!existingUser.getUsername().equals(newUsername) &&
                userRepository.findByUsername(newUsername).isPresent()) {
            throw new IllegalArgumentException("Username already exists.");
        }
    }

    private void validateEmailUpdate(User existingUser, String newEmail) {
        if (isInvalidEmail(newEmail)) {
            throw new IllegalArgumentException("Invalid email format.");
        }
        if (!existingUser.getEmail().equals(newEmail) &&
                userRepository.findByEmail(newEmail).isPresent()) {
            throw new IllegalArgumentException("Email already exists.");
        }
    }

    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new UserNotFoundException("User with id " + id + " not found");
        }
        userRepository.deleteById(id);
    }

    public boolean existsById(Long id) {
        return userRepository.existsById(id);
    }

    private boolean isInvalidEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return true;
        }
        Matcher matcher = emailPattern.matcher(email);
        return !matcher.matches();
    }



    public void uploadProfilePicture(Long id, MultipartFile file) {
        if (file == null) {
            throw new IllegalArgumentException("File cannot be null");
        }
        Optional<User> user = userRepository.findById(id);
        if (user.isEmpty()) {
            throw new UserNotFoundException("User with id " + id + " not found");
        }

        if (user.get().getProfilePicturePath() != null) {
            fileStorageService.delete(user.get().getProfilePicturePath());
        }

        String filename = fileStorageService.store(file);
        user.get().setProfilePicturePath(filename);
        userRepository.save(user.get());
    }

    @Transactional(readOnly = true)
    public Resource getProfilePicture(Long id) {
        Optional<User> user = userRepository.findById(id);
        if (user.isEmpty()) {
            throw new UserNotFoundException("User with id " + id + " not found");
        }
        String filename = user.get().getProfilePicturePath();
        if (filename == null) {
            throw new RuntimeException("User does not have a profile picture");
        }
        return fileStorageService.loadFile(filename);
    }
}

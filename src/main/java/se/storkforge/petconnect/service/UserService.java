package se.storkforge.petconnect.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import se.storkforge.petconnect.entity.Role;
import se.storkforge.petconnect.entity.User;
import se.storkforge.petconnect.exception.UserNotFoundException;
import se.storkforge.petconnect.repository.RoleRepository;
import se.storkforge.petconnect.repository.UserRepository;
import se.storkforge.petconnect.service.storageService.RestrictedFileStorageService;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import se.storkforge.petconnect.security.Roles;

@Service
@Transactional
public class UserService {
    private final RestrictedFileStorageService storageService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    @Autowired
    private RoleRepository roleRepository;
    private static final String EMAIL_REGEX = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";
    private final Pattern emailPattern = Pattern.compile(EMAIL_REGEX);

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, RestrictedFileStorageService fileStorageService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.storageService = fileStorageService;
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
        if (!isStrongPassword(user.getPassword())) {
            throw new IllegalArgumentException("Password must contain at least 8 characters, one number, and one special character.");
        }
    }

    @Cacheable(value = "userCache", key = "#id")
    public User getUserById(Long id) {
        return getOrElseThrow(id);
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

    @CacheEvict(value = "userCache", key = "#id")
    public User updateUser(Long id, User updatedUser) {
        User existingUser = getUserById(id);
        updateUserFields(existingUser, updatedUser);
        return userRepository.save(existingUser);
    }

    public User getOrCreateOAuthUser(String email, String name) {
        return userRepository.findByEmail(email)
                .orElseGet(() -> {
                    User newUser = new User();
                    newUser.setUsername(name);
                    newUser.setEmail(email);
                    newUser.setPassword("oauth_dummy"); // Will not be used for login
                    newUser.setPhoneNumber("0000000000");

                    Role userRole = roleRepository.findByName(Roles.USER)
                            .orElseThrow(() -> new IllegalStateException("Role USER not found"));
                    newUser.setRoles(Set.of(userRole));

                    return userRepository.save(newUser);
                });
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
    @CacheEvict(value = "userCache", key = "#id")
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

    @CacheEvict(value = "userCache", key = "#id")
    public void uploadProfilePicture(Long id, MultipartFile file) {
        String dir = "users/"+ id +"/profilePictures";

        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File cannot be null or empty");
        }

        User user = getOrElseThrow(id);

        if (user.getProfilePicturePath() != null) {
            try {
                storageService.delete(user.getProfilePicturePath());
            } catch (RuntimeException e) {

            }
        }

        String filename = storageService.storeImage(file, dir);
        user.setProfilePicturePath(filename);
        userRepository.save(user);
    }

    @Transactional(readOnly = true)
    public Resource getProfilePicture(Long id) {
        User user = getOrElseThrow(id);
        String filename = user.getProfilePicturePath();
        if (filename == null) {
            throw new RuntimeException("User does not have a profile picture");
        }
        return storageService.loadFile(filename);
    }

    @CacheEvict(value = "userCache", key = "#id")
    public void deleteProfilePicture(Long id) {
        User user = getOrElseThrow(id);
        storageService.delete(user.getProfilePicturePath());
    }

    private User getOrElseThrow(Long id) {
        return userRepository.findById(id).orElseThrow(() -> new UserNotFoundException("User with id " + id + " not found"));
    }

    private boolean isStrongPassword(String password) {
        return password != null && password.matches(
                "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$"
        );
    }

    @CacheEvict(value = "userCache", key = "#user.id")
    public void save(User user) {
        userRepository.save(user);
    }

    public User getUserByUsernameOrThrow(String username) {
        return getUserByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("User with username " + username + " not found"));
    }
}
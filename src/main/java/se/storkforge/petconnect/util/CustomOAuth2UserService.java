package se.storkforge.petconnect.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.userinfo.*;
import org.springframework.security.oauth2.client.*;
import org.springframework.security.oauth2.core.user.*;
import org.springframework.security.oauth2.core.*;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;
import se.storkforge.petconnect.entity.User;
import se.storkforge.petconnect.repository.UserRepository;
import se.storkforge.petconnect.service.UserService;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class CustomOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

    private final UserService userService;
    private final UserRepository userRepository;
    private final OAuth2UserService<OAuth2UserRequest, OAuth2User> delegate = new DefaultOAuth2UserService();

    public CustomOAuth2UserService(UserService userService, UserRepository userRepository) {
        this.userService = userService;
        this.userRepository = userRepository;
    }

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oauthUser = delegate.loadUser(userRequest);

        String email = getRequiredAttribute(oauthUser, "email");
        String name = oauthUser.getAttribute("name");

        User user = userService.getOrCreateOAuthUser(email, name);

        List<SimpleGrantedAuthority> authorities = user.getRoles().stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role.getName()))
                .collect(Collectors.toList());

        // Debug log (optional)
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null) {
            System.out.println(" Authenticated user: " + auth.getName());
            System.out.println(" Authorities: " + auth.getAuthorities());
        }

        return new DefaultOAuth2User(
                authorities,
                enrichAttributes(oauthUser, user),
                "username" // This must match a key in the attributes map
        );
    }

    private Map<String, Object> enrichAttributes(OAuth2User oauthUser, User user) {
        Map<String, Object> attributes = new HashMap<>(oauthUser.getAttributes());

        String username = user.getUsername();
        if (username == null || username.isBlank()) {
            username = UUID.randomUUID().toString();
            user.setUsername(username);
            userRepository.save(user);
        }

        attributes.put("username", username); // used as the principal name
        return attributes;
    }

    private String getRequiredAttribute(OAuth2User user, String key) {
        String value = user.getAttribute(key);
        if (value == null || value.isBlank()) {
            throw new OAuth2AuthenticationException(new OAuth2Error("missing_attribute"), "Missing required attribute: " + key);
        }
        return value;
    }
}

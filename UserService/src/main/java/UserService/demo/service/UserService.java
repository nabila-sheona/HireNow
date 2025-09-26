package UserService.demo.service;

import UserService.demo.model.User;
import UserService.demo.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import UserService.demo.service.PasswordStrengthException;
import java.util.Set;
import java.util.HashSet;

@Service
public class UserService {

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Autowired
    private UserRepository userRepository;

    private void validatePasswordStrength(String password) {
        if (password == null || password.length() < 8) {
            throw new PasswordStrengthException("Password is too weak: must be at least 8 characters");
        }
        if (password.length() > 64) {
            throw new PasswordStrengthException("Password is too strong: must be at most 64 characters");
        }
        // Require at least one uppercase, one lowercase, one digit, one special character
        if (!password.matches(".*[A-Z].*")) {
            throw new PasswordStrengthException("Password must contain at least one uppercase letter");
        }
        if (!password.matches(".*[a-z].*")) {
            throw new PasswordStrengthException("Password must contain at least one lowercase letter");
        }
        if (!password.matches(".*[0-9].*")) {
            throw new PasswordStrengthException("Password must contain at least one digit");
        }
        if (!password.matches(".*[^a-zA-Z0-9].*")) {
            throw new PasswordStrengthException("Password must contain at least one special character");
        }
    }

    public User createUser(User user) {
        if (userRepository.existsByUsername(user.getUsername())) {
            throw new DuplicateKeyException("Username already exists");
        }
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new EmailAlreadyExistsException("Email already exists");
        }
        validatePasswordStrength(user.getPassword());
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        // Assign default role if none provided
        if (user.getRoles() == null || user.getRoles().isEmpty()) {
            Set<String> defaultRoles = new HashSet<>();
            defaultRoles.add("JOB_SEEKER");
            user.setRoles(defaultRoles);
        }
        return userRepository.save(user);
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public Optional<User> getUserById(String id) {
        return userRepository.findById(id);
    }

    public Optional<User> getUserByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public User updateUser(String id, User userDetails) {
        User user = userRepository.findById(id).orElseThrow(() -> new RuntimeException("User not found"));
        if (!user.getUsername().equals(userDetails.getUsername()) && userRepository.existsByUsername(userDetails.getUsername())) {
            throw new DuplicateKeyException("Username already exists");
        }
        if (!user.getEmail().equals(userDetails.getEmail()) && userRepository.existsByEmail(userDetails.getEmail())) {
            throw new EmailAlreadyExistsException("Email already exists");
        }
        user.setUsername(userDetails.getUsername());
        user.setEmail(userDetails.getEmail());
        user.setPhone(userDetails.getPhone());
        user.setCompanyName(userDetails.getCompanyName());
        // Only update password if a new one is provided
        if (userDetails.getPassword() != null && !userDetails.getPassword().isEmpty()) {
            validatePasswordStrength(userDetails.getPassword());
            user.setPassword(passwordEncoder.encode(userDetails.getPassword()));
        }
        // Update roles if provided
        if (userDetails.getRoles() != null && !userDetails.getRoles().isEmpty()) {
            user.setRoles(userDetails.getRoles());
        }
        return userRepository.save(user);
    }

    public void deleteUser(String id) {
        userRepository.deleteById(id);
    }

    public void assignRole(String id, String role) {
        User user = userRepository.findById(id).orElseThrow(() -> new RuntimeException("User not found"));
        user.getRoles().add(role);
        userRepository.save(user);
    }

    public void removeRole(String id, String role) {
        User user = userRepository.findById(id).orElseThrow(() -> new RuntimeException("User not found"));
        user.getRoles().remove(role);
        userRepository.save(user);
    }

    public Set<String> getUserRoles(String id) {
        User user = userRepository.findById(id).orElseThrow(() -> new RuntimeException("User not found"));
        return user.getRoles();
    }
}


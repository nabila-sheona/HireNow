package UserService.demo.controller;

import UserService.demo.model.User;
import UserService.demo.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Optional;
import jakarta.validation.Valid;
import org.springframework.validation.BindingResult;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import java.util.HashMap;
import java.util.Map;
import UserService.demo.service.EmailAlreadyExistsException;
import org.springframework.dao.DuplicateKeyException;
import UserService.demo.service.PasswordStrengthException;
import org.springframework.security.access.prepost.PreAuthorize;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;

    @PostMapping
    public ResponseEntity<?> createUser(@Valid @RequestBody User user, BindingResult bindingResult) {
        Map<String, String> errors = new HashMap<>();
        if (bindingResult.hasErrors()) {
            bindingResult.getFieldErrors().forEach(error ->
                errors.put(error.getField(), error.getDefaultMessage())
            );
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errors);
        }
        // Business validation
        if (user.getRoles() == null || user.getRoles().isEmpty()) {
            errors.put("roles", "At least one role must be assigned (e.g., JOB_SEEKER or JOB_HIRER)");
        } else if (!user.getRoles().contains("JOB_SEEKER") && !user.getRoles().contains("JOB_HIRER")) {
            errors.put("roles", "Role must be either JOB_SEEKER or JOB_HIRER");
        }
        if (user.getRoles() != null && user.getRoles().contains("JOB_HIRER") && (user.getCompanyName() == null || user.getCompanyName().isEmpty())) {
            errors.put("companyName", "Company name is required for JOB_HIRER");
        }
        if (!errors.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errors);
        }
        try {
            return ResponseEntity.ok(userService.createUser(user));
        } catch (PasswordStrengthException e) {
            errors.put("password", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errors);
        } catch (EmailAlreadyExistsException e) {
            errors.put("email", e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT).body(errors);
        } catch (DuplicateKeyException e) {
            errors.put("username", e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT).body(errors);
        }
    }

    @GetMapping
    public List<User> getAllUsers() {
        return userService.getAllUsers();
    }

    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(@PathVariable String id) {
        Optional<User> user = userService.getUserById(id);
        return user.map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/username/{username}")
    public ResponseEntity<User> getUserByUsername(@PathVariable String username) {
        Optional<User> user = userService.getUserByUsername(username);
        return user.map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateUser(@PathVariable String id, @Valid @RequestBody User userDetails, BindingResult bindingResult) {
        Map<String, String> errors = new HashMap<>();
        if (bindingResult.hasErrors()) {
            bindingResult.getFieldErrors().forEach(error ->
                errors.put(error.getField(), error.getDefaultMessage())
            );
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errors);
        }
        if (userDetails.getRoles() == null || userDetails.getRoles().isEmpty()) {
            errors.put("roles", "At least one role must be assigned (e.g., JOB_SEEKER or JOB_HIRER)");
        } else if (!userDetails.getRoles().contains("JOB_SEEKER") && !userDetails.getRoles().contains("JOB_HIRER")) {
            errors.put("roles", "Role must be either JOB_SEEKER or JOB_HIRER");
        }
        if (userDetails.getRoles() != null && userDetails.getRoles().contains("JOB_HIRER") && (userDetails.getCompanyName() == null || userDetails.getCompanyName().isEmpty())) {
            errors.put("companyName", "Company name is required for JOB_HIRER");
        }
        if (!errors.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errors);
        }
        try {
            User updatedUser = userService.updateUser(id, userDetails);
            return ResponseEntity.ok(updatedUser);
        } catch (PasswordStrengthException e) {
            errors.put("password", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errors);
        } catch (EmailAlreadyExistsException e) {
            errors.put("email", e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT).body(errors);
        } catch (DuplicateKeyException e) {
            errors.put("username", e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT).body(errors);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/{id}/roles")
    public ResponseEntity<?> assignRole(@PathVariable String id, @RequestBody String role) {
        userService.assignRole(id, role);
        return ResponseEntity.ok().build();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}/roles/{role}")
    public ResponseEntity<?> removeRole(@PathVariable String id, @PathVariable String role) {
        userService.removeRole(id, role);
        return ResponseEntity.ok().build();
    }

    @PreAuthorize("hasRole('ADMIN') or #id == authentication.principal.username")
    @GetMapping("/{id}/roles")
    public ResponseEntity<?> getUserRoles(@PathVariable String id) {
        return ResponseEntity.ok(userService.getUserRoles(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable String id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }
}

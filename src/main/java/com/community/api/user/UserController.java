
package com.community.api.user;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserRepository userRepository;

    public UserController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> getAllUsers() {
        List<Map<String, Object>> users = userRepository.findAll()
                .stream()
                .map(user -> {
                    Map<String, Object> result = new java.util.LinkedHashMap<>();
                    result.put("id", user.getId());
                    result.put("uuid", user.getUuid());
                    result.put("name", user.getName());
                    result.put("username", user.getUsername());
                    result.put("email", user.getEmail());
                    result.put("status", user.getStatus());
                    return result;
                })
                .toList();

        return ResponseEntity.ok(users);
    }
}

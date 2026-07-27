package tr.teklifos.identity.api;

import java.util.List;
import java.util.UUID;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tr.teklifos.identity.application.UserService;
import tr.teklifos.identity.domain.AppUserEntity;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('settings:manage')")
    public List<UserDto> list() {
        return userService.listUsers().stream().map(UserDto::from).toList();
    }

    public record UserDto(UUID id, String email, String fullName, String status) {
        static UserDto from(AppUserEntity u) {
            return new UserDto(u.getId(), u.getEmail(), u.getFullName(), u.getStatus());
        }
    }
}

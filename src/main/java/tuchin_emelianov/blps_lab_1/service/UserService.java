package tuchin_emelianov.blps_lab_1.service;

import lombok.AllArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import tuchin_emelianov.blps_lab_1.exceptions.UserAlreadyExistsException;
import tuchin_emelianov.blps_lab_1.jpa.entity.Role;
import tuchin_emelianov.blps_lab_1.jpa.entity.User;
import tuchin_emelianov.blps_lab_1.jpa.repository.RoleRepository;
import tuchin_emelianov.blps_lab_1.jpa.repository.UserRepository;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@AllArgsConstructor
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        System.out.println(username);
        return userRepository.findByUsername(username);
    }
    
    public List<User> getUsersByRole(String name) {
        Role role = roleRepository.findByName(name);
        return userRepository.findUsersByRoles(role);
    }

    public User getUser(String username) {return userRepository.findByUsername(username);}

    public void existsUser(String username) {
        if (userRepository.existsByUsername(username)) {
            throw new UserAlreadyExistsException("Пользователь с логином %s уже существует".formatted(username));
        }
    }

    public User createUser(String login, String password, String role) {
        User user = new User();
        user.setUsername(login);
        user.setPassword("{noop}"+password);
        Set<Role> roles = new HashSet<>();
        roles.add(roleRepository.findByName(role));
        user.setRoles(roles);
        return userRepository.save(user);
    }
}

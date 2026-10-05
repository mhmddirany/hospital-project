package lab.java.demo.repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Repository;

import lab.java.demo.Models.User;
import lab.java.demo.exception.UserConflictException;

/**
 * Repository for User (login account) objects. Same
 * ConcurrentHashMap-backed pattern as every other repository in this
 * project (see Issues 6 and 17) -- accounts are not persisted to a real
 * database. Indexed by both id and username, since authentication always
 * looks a user up by username while the admin-facing endpoints look one
 * up by id.
 */
@Repository
public class UserRepository {

    private final Map<Integer, User> usersById = new ConcurrentHashMap<>();
    private final Map<String, User> usersByUsername = new ConcurrentHashMap<>();

    public List<User> findAll() {
        return new ArrayList<>(usersById.values());
    }

    public Optional<User> findById(int id) {
        return Optional.ofNullable(usersById.get(id));
    }

    public Optional<User> findByUsername(String username) {
        return Optional.ofNullable(usersByUsername.get(username));
    }

    public User save(User user) {
        if (usersById.putIfAbsent(user.getId(), user) != null) {
            throw new UserConflictException("User with id " + user.getId() + " already exists");
        }
        if (usersByUsername.putIfAbsent(user.getUsername(), user) != null) {
            // Roll back the id-keyed insert so a failed save doesn't leave
            // the two indexes out of sync.
            usersById.remove(user.getId());
            throw new UserConflictException("Username '" + user.getUsername() + "' is already taken");
        }
        return user;
    }

    public boolean deleteById(int id) {
        User removed = usersById.remove(id);
        if (removed == null) {
            return false;
        }
        usersByUsername.remove(removed.getUsername());
        return true;
    }
}

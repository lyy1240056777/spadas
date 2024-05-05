package web.Repo;

import org.springframework.data.jpa.repository.JpaRepository;
import web.entity.User;

public interface UserRepo extends JpaRepository<User, Integer> {
    int countByUsername(String name);

    User findByUsername(String name);
}

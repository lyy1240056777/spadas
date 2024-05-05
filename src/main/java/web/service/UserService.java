package web.service;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import web.Repo.UserRepo;
import web.Utils.UserHolder;
import web.VO.Result;
import web.entity.User;
import web.global.GlobalVariables;
import web.param.OrderParams;
import web.param.UserDTO;

@Slf4j
@Service
public class UserService {
    @Autowired
    private UserRepo userRepo;

    @Autowired
    private GlobalVariables variables;

    public Result register(String userName, String password) {
        int cnt = userRepo.countByUsername(userName);
        if (cnt > 0) {
            return Result.fail("Registration failed. Username already exists.");
        }
        User user = new User();
        user.setUsername(userName);
        user.setPassword(password);
        userRepo.save(user);
        User uu = userRepo.findByUsername(userName);
//        UserHolder.removeUser();
//        UserHolder.saveUser(new UserDTO(uu.getUserId(), uu.getUsername(), uu.getPassword()));
        variables.setUserId(uu.getUserId());
        return Result.ok(uu.getUserId());
    }

    public Result login(String userName, String password) {
        int cnt = userRepo.countByUsername(userName);
        if (cnt == 0) {
            return Result.fail("Login failed. Username is wrong.");
        }
        User user = userRepo.findByUsername(userName);
        if (!user.getPassword().equals(password)) {
            return Result.fail("Login failed. Password is wrong.");
        }
        variables.setUserId(user.getUserId());
        return Result.ok(user.getUserId());
    }

    public Result logout() {
        variables.setUserId(0);
        return Result.ok();
    }
}

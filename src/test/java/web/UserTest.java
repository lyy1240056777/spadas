package web;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import web.VO.Result;
import web.entity.User;
import web.service.UserService;

@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest(classes = SpadasWebApplication.class)
public class UserTest {
    @Autowired
    UserService userService;
    @Test
    public void registerTest() {
//        Result res = userService.register("cza", "0738");
        System.out.println();
    }
}

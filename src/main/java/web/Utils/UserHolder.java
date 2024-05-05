package web.Utils;

import web.param.UserDTO;

public class UserHolder {
    private static ThreadLocal<UserDTO> tl =new ThreadLocal<>();

    public static void saveUser(UserDTO userDTO) {
        tl.set(userDTO);
    }

    public static UserDTO getUser() {
        return tl.get();
    }

    public static void removeUser() {
        tl.remove();
    }
}

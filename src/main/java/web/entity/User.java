package web.entity;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity(name = "user")
public class User {
    @Id
    @Column(name = "user_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer userId;

    private String username;

    private String password;
}

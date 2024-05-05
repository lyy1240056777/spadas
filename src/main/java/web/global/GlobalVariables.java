package web.global;

import lombok.Data;
import org.springframework.stereotype.Component;

@Component
@Data
public class GlobalVariables {
    private int userId = 0;
}

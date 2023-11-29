package web.param;

import lombok.Data;
import org.springframework.web.bind.annotation.RequestParam;

@Data
public class KeywordsParams {
    String kws;

    int limit = 5;
}

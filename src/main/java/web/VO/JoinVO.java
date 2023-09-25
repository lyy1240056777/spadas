package web.VO;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class JoinVO {
    @JsonProperty("header")
    private List<String> header;
    @JsonProperty("joinData")
    private List<List<String>> body;
}

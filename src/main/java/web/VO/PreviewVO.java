package web.VO;

import lombok.Data;

import java.util.List;

@Data
public class PreviewVO {
    private String type;
    private List<String> headers;
    private List<List<double[]>> bodies;

    public PreviewVO(String type, List<String> headers, List<List<double[]>> bodies) {
        this.type = type;
        this.headers = headers;
        this.bodies = bodies;
    }
}

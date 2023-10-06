package web.param;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class PreviewParams {
    List<Integer> ids = new ArrayList<>();
    int rows;
}

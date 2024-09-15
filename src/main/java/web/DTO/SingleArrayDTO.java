package web.DTO;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Tian Qi Qing
 * @version 1.0
 * @date 2022/03/16/18:21
 **/
@Data
public class SingleArrayDTO<T> {
    List<T> list = new ArrayList<>();
}

package edu.whu.schema;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;

@Data
public class RootSchema extends Schema {
    @JSONField(name = "@context")
    private Context context;
}

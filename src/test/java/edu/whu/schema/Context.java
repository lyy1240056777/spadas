package edu.whu.schema;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;

@Data
public class Context implements RdfsSchema {
    @JSONField(name = "rdfs")
    private String rdfs;

    @JSONField(name = "schema")
    private String schema;

    @JSONField(name = "description")
    private String description;

    // children
    
}

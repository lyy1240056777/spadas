package edu.whu.schema;


import com.alibaba.fastjson.annotation.JSONField;
import com.alibaba.fastjson.annotation.JSONType;
import lombok.Data;

import java.util.List;

@Data
@JSONType
public class Schema implements RdfsSchema {
    @JSONField(name = "@type")
    private String type;

    @JSONField(name = "@id")
    private String id;

    @JSONField(name = "name")
    private String name;
    
    @JSONField(name = "description")
    private String description;


    @JSONField(name = "pending")
    private boolean pending;

    @JSONField(name = "children")
    private List<Schema> children;

}

package edu.whu.schema;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;

public interface RdfsSchema {
    @JSONField(name="rdfs:subClassOf")
    String subClassOf = null;

}

package edu.whu.schema;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.parser.ParserConfig;
import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.util.ResourceUtils;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class SchemaReaderTest {

    @Test
    public void readJsonFile() throws IOException {
        ParserConfig.getGlobalInstance().setAutoTypeSupport(true);
        InputStream inputStream = new FileInputStream(ResourceUtils.getFile("classpath:schema.org.tree.jsonld"));
        String text = IOUtils.toString(inputStream, "utf8");
        RootSchema schemaTree = JSON.parseObject(text, RootSchema.class);
        Assert.assertNotNull(schemaTree);
        Schema target = null;
        String level1 = "Organization";
        for (Schema schema : schemaTree.getChildren()) {
            if (schema.getName().equals(level1)) {
                target = schema;
                break;
            }
        }
        String level2 = "LocalBusiness";
        for (Schema schema : target.getChildren()) {
            if (schema.getName().equals(level2)) {
                target = schema;
                break;
            }
        }
        for (Schema schema : target.getChildren()) {
            System.out.print(schema.getName() + ", ");
        }
    }
}

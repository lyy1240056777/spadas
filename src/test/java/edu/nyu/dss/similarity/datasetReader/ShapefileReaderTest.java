package edu.nyu.dss.similarity.datasetReader;

import edu.nyu.dss.similarity.CityNode;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.ResourceUtils;
import web.SpadasWebApplication;

import java.io.File;
import java.io.IOException;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = SpadasWebApplication.class)
public class ShapefileReaderTest {

    @Autowired
    private ShapefileReader shapefileReader;

    @Test
    public void readTest() throws IOException {
        File file = ResourceUtils.getFile("classpath:IhChina_2006-2021/IhChina_2006-2021.shp");
        shapefileReader.read(file, 1, new CityNode("IhChina", 2));
    }
}

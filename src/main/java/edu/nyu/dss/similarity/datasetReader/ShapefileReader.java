package edu.nyu.dss.similarity.datasetReader;

import edu.nyu.dss.similarity.CityNode;
import edu.nyu.dss.similarity.config.SpadasConfig;
import edu.nyu.dss.similarity.index.DataMapPorto;
import edu.nyu.dss.similarity.index.DatasetIDMapping;
import edu.nyu.dss.similarity.index.FileIDMap;
import edu.nyu.dss.similarity.index.IndexBuilder;
import edu.nyu.dss.similarity.statistics.DatasetSizeCounter;
import edu.nyu.dss.similarity.statistics.PointCounter;
import edu.rmit.trajectory.clustering.kmeans.indexNode;
import lombok.extern.slf4j.Slf4j;
import org.geotools.data.FileDataStore;
import org.geotools.data.FileDataStoreFinder;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.data.simple.SimpleFeatureSource;
import org.opengis.feature.Property;
import org.opengis.feature.simple.SimpleFeature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Component
@Slf4j
public class ShapefileReader {
    @Autowired
    private SpadasConfig config;

    @Autowired
    private DatasetSizeCounter datasetSizeCounter;

    @Autowired
    private PointCounter pointCounter;

    @Autowired
    private FileIDMap fileIDMap;

    @Autowired
    private DataMapPorto dataMapPorto;

    @Autowired
    private DatasetIDMapping datasetIDMapping;

    @Autowired
    private IndexBuilder indexBuilder;

//    @Value("${spadas.file.baseUri}")
//    private String datasetDir;

    public void read(File file, int id, CityNode cityNode) throws IOException {
        if (!file.getName().endsWith("shp")) {
            return;
        }
        file.setReadOnly();
        FileDataStore store = FileDataStoreFinder.getDataStore(file);
        SimpleFeatureSource featureSource = store.getFeatureSource();
        try (SimpleFeatureIterator i = featureSource.getFeatures().features()) {
            SimpleFeature feature;
            List<double[]> locations = new ArrayList<>();
            while (i.hasNext()) {
                feature = i.next();

                String latKey = "Y";
                String lonKey = "X";
                try {
                    double lat = (double) feature.getAttribute(latKey);
                    double lon = (double) feature.getAttribute(lonKey);
                    locations.add(new double[]{lat, lon});
                } catch (NumberFormatException e) {
                    log.warn("Cannot parse location with ({}, {})", feature.getAttribute(latKey), feature.getAttribute(lonKey));
                } catch (NullPointerException e) {
                    log.warn("No attribute for location");
                }
            }

            pointCounter.put(locations.size());
            datasetSizeCounter.put(locations.size());

            writeIndex(locations, id, file, cityNode);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            store.dispose();
        }
    }

    public void writeIndex(List<double[]> locations, int id, File file, CityNode cityNode) {
        double[][] spatialData = locations.toArray(new double[locations.size()][]);
        if (config.isCacheDataset()) {
            dataMapPorto.put(id, spatialData);
        }
        if (config.isCacheIndex()) {
            indexNode node = indexBuilder.createDatasetIndex(id, spatialData, 1, cityNode);
            indexBuilder.samplingDataByGrid(spatialData, id, node);
            node.setFileName(file.getParent());
        }
        datasetIDMapping.put(id, file.getName());
        fileIDMap.put(id, file);
    }
}

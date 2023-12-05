package edu.nyu.dss.similarity.datasetReader;

import edu.nyu.dss.similarity.CityNode;
import org.apache.commons.lang3.NotImplementedException;

import java.io.File;
import java.io.IOException;
import java.util.Map;

public abstract class AbsReader {
    public Map<Integer, double[][]> read(File file, int fileNo, CityNode cityNode, int datasetIDForOneDir) throws IOException {
        throw new NotImplementedException("Not implemented read method");
    }
}

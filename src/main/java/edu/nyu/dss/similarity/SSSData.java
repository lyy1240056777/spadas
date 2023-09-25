package edu.nyu.dss.similarity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.stereotype.Component;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

@Component
@Data
@NoArgsConstructor
public class SSSData {
    @JsonProperty("id")
    int id;
    @JsonProperty("name")
    String name;

    @JsonIgnore
    Type type;
    @JsonProperty("previewData")
    List<List<String>> previewData;

    SSSData(int id, String filePath) throws IOException {
        File file = new File(filePath);
        if (file.exists() && file.isFile()) {
            name = file.getName();
            this.id = id;
//            for (Type t : Type.values()) {
//                if (name.contains(t.description)) {
//                    type = t;
//                }
//            }
            previewData = new ArrayList<>();
            try (Reader reader = new FileReader(filePath);
                 CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT)) {
                int rowCounter = 0;
                for (CSVRecord record : csvParser) {
                    List<String> rowData = new ArrayList<>();
                    for (String value : record) {
                        rowData.add(value);
                    }
                    previewData.add(rowData);
                    rowCounter++;
                    if (rowCounter > 10) {
                        break;
                    }
                }
            }
        } else {
            System.out.println("Failed: " + filePath);
        }
    }

    enum Type {
        Fish("鱼类数据"),
        Phytoplankton("浮游植物"),
        Zooplankton("浮游动物"),
        Zoobenthos("底栖动物");

        String description;

        Type(String description) {
            this.description = description;
        }
    }
}

package edu.nyu.dss.similarity;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SSSOperate {
//    这里写死了，不好，每次都要改
    public static String rootDir = "/home/lyy/spadas/水生所/";
    public static List<SSSData> sssDataList = new ArrayList<>();
    public static Map<Integer, Double[]> placeIDMap = new HashMap<>();

    public static List<SSSData> selectSSSData(int placeID) {
        if (placeID == 0) {
            return sssDataList;
        }
        String placeIDStr = Integer.toString(placeID);
        List<SSSData> res = new ArrayList<>();
        for (SSSData data : sssDataList) {
            if (data.name.contains(placeIDStr)) {
                res.add(data);
            }
        }
        return res;
    }

    //    暂时写死，因为就八个采样点
    public static void initSSS() throws IOException {
//        初始化placeIDMap
        placeIDMap.put(1, new Double[]{30.7735, 114.496});
        placeIDMap.put(2, new Double[]{30.7866, 114.4952});
        placeIDMap.put(3, new Double[]{30.8069, 114.4804});
        placeIDMap.put(4, new Double[]{30.8238, 114.4783});
        placeIDMap.put(5, new Double[]{30.7971, 114.4993});
        placeIDMap.put(6, new Double[]{30.8126, 114.5076});
        placeIDMap.put(7, new Double[]{30.7996, 114.5208});
        placeIDMap.put(8, new Double[]{30.8076, 114.5306});

//        初始化sssDataList
        File root = new File(rootDir);
        if (root.exists() && root.isDirectory()) {
            File[] files = root.listFiles();
            if (files != null) {
                int count = 0;
                for (File file: files) {
                    if (file.isFile() && file.getName().endsWith(".csv")) {
                        sssDataList.add(new SSSData(count++, file.getAbsolutePath()));
                    }
                }
            }
        }
    }
}

package edu.nyu.dss.similarity.consts;

import java.util.Arrays;
import java.util.Comparator;
import java.util.function.Function;

public enum DataLakeType {
    // MOVE BANK
    MOVE_BANK(9, false, (str) -> str.contains("movebank")), // BUS LINES
    BUS_LINE(8, false, "Bus_lines"::equals), // USA Datasets
    USA(8, true, (str) -> !str.isEmpty() && Character.isUpperCase(str.charAt(0))), // POIs
    POI(10, false, "poi"::equals), // BAIDU POI
    BAIDU_POI(7, true, (str) -> true),
    OPEN_NYC(11, true, (str) -> str.contains("nyc") && str.contains("open"));
    public final int id;
    public final boolean active;
    public final Function<String, Boolean> func;

    private static final DataLakeType DEFAULT = BAIDU_POI;

    DataLakeType(int id, boolean active, Function<String, Boolean> func) {
        this.id = id;
        this.active = active;
        this.func = func;
    }


    public static DataLakeType matchType(String filename) {
        Object[] types = Arrays.stream(DataLakeType.values()).sorted(Comparator.comparingInt(a -> a.id)).toArray();
        for (int i = types.length - 1; i >= 0; i--) {
            if ((((DataLakeType)types[i]).func.apply(filename))) {
                return (DataLakeType) types[i];
            }
        }
        return DEFAULT;
    }
}

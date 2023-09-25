# About the indexes

This directory contains all the index structure used in the spadas dataset query framework.

| Index Name       | Type                          | Key      | Value                                                         |
|------------------|-------------------------------|----------|---------------------------------------------------------------|
| DatasetIDMapping | Map<Integer, String>          | DatasetID | fileName                                                      |
| DataSamplingMap  | Map<Integer, List<Double[3]>> | DatasetID | Sampled Points Location with current Grid weight percentage * |
| DataMapPorto     | Map<Integer, double[size][2]  | DatasetID | Points Location                                               |
| FileIDMap        | Map<Integer, File>            | DatasetID | Dataset File                                                  |
| IndexMap         | Map<Integer, IndexNode>       | DatasetID | IndexNode                                                     |
| IndexNodes       | List<IndexNode>               |          | {IndexNode Structure}                                         |
| ZCodeMap         | Map<Integer, List<Integer>>   |||

> *For example, dataSamplingMap has an entry with key=1, value=List<100>,value[0]=[40.73321, -74.00557, 0.00396].
> It means that dataset 1 has 100 sampled points(origin dataset may contain more), and one point's location is [40.73321, -74.00557],
> and there are 0.396% points are in the same grid. The grid range depends on the resolution parameter, the dataset center location and radius.
> 
> (Find more details about grid in samplingDataByGrid() Method, IndexBuilder class.)
 

``` java
class IndexNode {
    int type;
    List<Integer> pointIDList;
    List<> nodeList;
    ? nodeIDList;
    double[2] pivot;
    double radius;
    double distanceToFarther;
    double[2] sum;
    ...
}
```
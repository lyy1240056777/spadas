# Data Lake Type 的说明


|类型 CODE| 优先级 |      判断条件      |
|:-----:|:---:|:--------------:|
|PURE_LOCATION| 13  | 文件名或者文件夹名包含wyz |
|ARGOVERSE| 13  |   文件路径包括argo   |
|SHAPE_FILE| 12  |   文件名以shp结尾    |
|OPEN_NYC| 11  | 文件名包含nyc和open  |
|POI| 10  |    文件名=poi     |
|MOVE_BANK|  9  | 文件名包含movebank  |
|BUS_LINE|  8  | 文件名=Bus_lines  |
|USA|  8  |   首字母大写的数据集    |
|BAIDU_POI|  7  |    其他所有数据集     |

> 这里存在非常明显的问题：用文件名来判断非常的不合理，同时兜底的方法也不够兼容。
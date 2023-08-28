package web.DTO;

import lombok.Data;

@Data
public class UnionDTO {
    int queryId;
    int[] unionIds;
    int preRows;
}

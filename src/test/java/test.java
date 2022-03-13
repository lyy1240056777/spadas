package test.java;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;
//import web.DTO.dsqueryDTO;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Tian Qi Qing
 * @version 1.0
 * @date 2022/03/03/12:44
 **/
@RunWith(SpringRunner.class)
public class test {

    @Test
    public void MapSortTeset(){
        HashMap<Integer,Double> map  = new HashMap(){{put(2,3.0);put(1,5.0); put(3,2.0);}};
        List<Integer> list=  map.entrySet().stream().sorted((o1,o2)->o2.getValue()-o1.getValue()<0.0? 1:-1).map(item-> item.getKey()).collect(Collectors.toList());
        for(Integer i:list)
            System.out.println(i);
    }
    @Test
    public void intTest(){
        //dsqueryDTO  dto = new dsqueryDTO();
        //System.out.println(dto.getDatasetId()); //O
    }


}


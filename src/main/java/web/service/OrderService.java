package web.service;

import edu.nyu.dss.similarity.index.*;
import edu.rmit.trajectory.clustering.kmeans.IndexNode;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import web.Repo.OrderRepo;
import web.Utils.FileU;
import web.Utils.FileUtil;
import web.Utils.ListUtil;
import web.VO.DatasetVo;
import web.VO.OrderVO;
import web.VO.Result;
import web.entity.Order;
import web.global.GlobalVariables;
import web.param.OrderParams;

import java.io.File;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Slf4j
@Service
public class OrderService {
    @Autowired
    private GlobalVariables variables;

    @Autowired
    private OrderRepo orderRepo;

    @Autowired
    private DatasetIDMapping datasetIdMapping;

    @Autowired
    private DatasetPriceMap datasetPriceMap;

    @Autowired
    private DataMapPorto dataMapPorto;

    @Autowired
    private FileIDMap fileIDMap;

    public Result buy(OrderParams qo) {
        if (variables.getUserId() == 0) {
            return Result.fail("Login first.");
        }
        Order order = new Order();
        order.setUserId(variables.getUserId());
        List<Integer> list = qo.getDatasets();
        order.setDatasetIds(ListUtil.listToString(list));
        order.setDatasetCnt(list.size());
        order.setTotalPrice(BigDecimal.valueOf(qo.getTotalPrice()).setScale(2, RoundingMode.HALF_UP));
        order.setIsPaid(false);
        order.setCreateTime(new Timestamp(System.currentTimeMillis()));
        orderRepo.save(order);
        return Result.ok();
    }

    public Result showAllOrders() {
        if (variables.getUserId() == 0) {
            return Result.fail("Login first.");
        }
        int id = variables.getUserId();
        List<Order> orders = orderRepo.findAllByUserId(id)
                .stream()
                .sorted(((o1, o2) -> o2.getCreateTime().compareTo(o1.getCreateTime())))
                .toList();
        List<OrderVO> vos = new ArrayList<>();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        for (Order o : orders) {
            List<DatasetVo> datasets = new ArrayList<>();
            List<Integer> ids = ListUtil.stringToList(o.getDatasetIds());
            int cnt = ids.size();
            for (int i : ids) {
                DatasetVo vo = DatasetVo.builder()
                        .id(i)
                        .filename(datasetIdMapping.get(i))
                        .price(datasetPriceMap.get(i))
                        .count(dataMapPorto.get(i).length)
                        .build();
                datasets.add(vo);
            }
            vos.add(new OrderVO(o.getOrderId(), sdf.format(o.getCreateTime()), datasets, o.getTotalPrice(), o.getIsPaid(), datasets.size()));
        }
        return Result.ok(vos);
    }

    public Result pay(int orderId) {
        Order order = orderRepo.findByOrderId(orderId);
        order.setIsPaid(true);
        orderRepo.save(order);
        return Result.ok();
    }

    public void downloadFiles(int orderId, HttpServletResponse response) {
        Order order = orderRepo.findByOrderId(orderId);
        List<Integer> datasetList = ListUtil.stringToList(order.getDatasetIds());
        List<File> files = datasetList.stream().map(id -> fileIDMap.get(id)).toList();
        FileUtil.downloadFiles(files, response);
    }

    @Transactional
    public Result deleteOrder(int orderId) {
        orderRepo.deleteByOrderId(orderId);
        return Result.ok();
    }
}

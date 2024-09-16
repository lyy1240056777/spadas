-- spadas.data_order definition

CREATE TABLE `data_order` (
                              `order_id` int NOT NULL AUTO_INCREMENT,
                              `user_id` int NOT NULL,
                              `dataset_ids` varchar(1024) DEFAULT '',
                              `dataset_cnt` int DEFAULT '0',
                              `total_price` decimal(10,2) DEFAULT '0.00',
                              `is_paid` tinyint(1) NOT NULL DEFAULT '0',
                              `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
                              PRIMARY KEY (`order_id`)
) ENGINE=InnoDB AUTO_INCREMENT=14 DEFAULT CHARSET=utf8mb3;
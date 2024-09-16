-- spadas.`user` definition

CREATE TABLE `user` (
                        `user_id` int NOT NULL AUTO_INCREMENT COMMENT '用户id',
                        `username` varchar(30) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL DEFAULT '' COMMENT '用户姓名',
                        `password` varchar(30) NOT NULL DEFAULT '' COMMENT '密码',
                        PRIMARY KEY (`user_id`)
) ENGINE=InnoDB AUTO_INCREMENT=13 DEFAULT CHARSET=utf8mb3 COMMENT='用户表';
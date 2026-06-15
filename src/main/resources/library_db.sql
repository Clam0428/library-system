-- 创建数据库
CREATE DATABASE IF NOT EXISTS library_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE library_db;

-- 管理员表
CREATE TABLE IF NOT EXISTS admin (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    username VARCHAR(100) UNIQUE NOT NULL COMMENT '用户名',
    password VARCHAR(255) NOT NULL COMMENT '密码（MD5加密）',
    admin_type VARCHAR(50) COMMENT '管理员类型',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_username (username)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='管理员表';

-- 图书分类表
CREATE TABLE IF NOT EXISTS type_info (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    name VARCHAR(100) UNIQUE NOT NULL COMMENT '分类名称',
    remarks VARCHAR(500) COMMENT '备注',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_name (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='图书分类表';

-- 图书信息表
CREATE TABLE IF NOT EXISTS book_info (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    name VARCHAR(200) NOT NULL COMMENT '图书名称',
    author VARCHAR(100) COMMENT '作者',
    publish VARCHAR(100) COMMENT '出版社',
    isbn VARCHAR(100) COMMENT '存放位置',
    introduction LONGTEXT COMMENT '简介',
    language VARCHAR(50) COMMENT '语言',
    price DECIMAL(10,2) COMMENT '价格',
    publish_date DATE COMMENT '出版日期',
    type_id BIGINT COMMENT '分类ID',
    status INT DEFAULT 0 COMMENT '状态：0=正常可用，1=已借出，2=禁用',
    stock INT COMMENT '库存数量',
    borrow_count INT DEFAULT 0 COMMENT '已借出数量',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_name (name),
    INDEX idx_type_id (type_id),
    INDEX idx_status (status),
    FOREIGN KEY (type_id) REFERENCES type_info(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='图书信息表';

-- 读者信息表
CREATE TABLE IF NOT EXISTS reader_info (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    username VARCHAR(100) UNIQUE NOT NULL COMMENT '用户名',
    password VARCHAR(255) NOT NULL COMMENT '密码（MD5加密）',
    real_name VARCHAR(100) COMMENT '真实姓名',
    sex INT COMMENT '性别：1=男，2=女',
    birthday DATE COMMENT '出生日期',
    address VARCHAR(200) COMMENT '地址',
    tel VARCHAR(20) COMMENT '电话',
    email VARCHAR(100) COMMENT '邮箱',
    register_date DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '注册日期',
    reader_number VARCHAR(50) UNIQUE COMMENT '读者号',
    status INT DEFAULT 0 COMMENT '状态：0=正常，1=禁用',
    borrow_count INT DEFAULT 0 COMMENT '已借图书数',
    fine_amount DOUBLE DEFAULT 0 COMMENT '罚款金额',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_username (username),
    INDEX idx_reader_number (reader_number),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='读者信息表';

-- 借阅记录表
CREATE TABLE IF NOT EXISTS lend_list (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    book_id BIGINT NOT NULL COMMENT '图书ID',
    reader_id BIGINT NOT NULL COMMENT '读者ID',
    lend_date DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '借书日期',
    should_back_date DATETIME COMMENT '应还日期',
    back_date DATETIME COMMENT '实际还书日期',
    back_type INT DEFAULT 0 COMMENT '还书类型：0=未还，1=已还，2=逾期，3=损坏',
    except_remarks LONGTEXT COMMENT '异常备注（如损坏情况）',
    fine_amount DOUBLE DEFAULT 0 COMMENT '罚款金额',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_book_id (book_id),
    INDEX idx_reader_id (reader_id),
    INDEX idx_back_type (back_type),
    FOREIGN KEY (book_id) REFERENCES book_info(id) ON DELETE CASCADE,
    FOREIGN KEY (reader_id) REFERENCES reader_info(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='借阅记录表';

-- 公告表
CREATE TABLE IF NOT EXISTS notice (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    title VARCHAR(200) NOT NULL COMMENT '公告标题',
    content LONGTEXT COMMENT '公告内容',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    admin_id BIGINT COMMENT '管理员ID',
    status INT DEFAULT 1 COMMENT '状态：0=草稿，1=已发布',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_status (status),
    FOREIGN KEY (admin_id) REFERENCES admin(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='公告表';

-- 插入初始数据
INSERT INTO admin (username, password, admin_type) VALUES
('admin', '21232f297a57a5a743894a0e4a801fc3', 'super');

INSERT INTO type_info (name, remarks) VALUES
('文学', '文学类书籍'),
('历史', '历史类书籍'),
('科技', '科技类书籍'),
('生活', '生活类书籍'),
('教育', '教育类书籍');

INSERT INTO book_info (name, author, publish, isbn, introduction, language, price, type_id, stock, status) VALUES
('活着', '余华', '南海出版公司', '978-7-5440-5222-4', '一部杰出的文学作品', '中文', 23.00, 1, 10, 0),
('三体', '刘慈欣', '重庆出版社', '978-7-5624-5057-5', '科幻经典之作', '中文', 39.50, 3, 8, 0),
('人类简史', '尤瓦尔', '中信出版社', '978-7-5086-9895-2', '从动物到上帝的全球发展史', '中文', 68.00, 2, 5, 0),
('解忧杂货店', '东野圭吾', '南海出版公司', '978-7-5346-9286-9', '温暖的推理小说', '中文', 38.00, 1, 12, 0),
('Python从入门到精通', '金角大王', '机械工业出版社', '978-7-1118-6482-2', 'Python编程完全指南', '中文', 99.00, 3, 7, 0),
('百年孤独', '加西亚·马尔克斯', '南海出版公司', 'A区-01架-1层', '魔幻现实主义的代表作', '中文', 55.00, 1, 5, 0),
('围城', '钱钟书', '人民文学出版社', 'A区-01架-2层', '生活在围城内外的人们', '中文', 35.00, 1, 8, 0),
('红楼梦', '曹雪芹', '人民文学出版社', 'A区-02架-1层', '中国古典小说的巅峰', '中文', 120.00, 1, 3, 0),
('西游记', '吴承恩', '人民文学出版社', 'A区-02架-2层', '中国古典四大名著之一', '中文', 98.00, 1, 6, 0),
('万历十五年', '黄仁宇', '中华书局', 'B区-01架-1层', '大明帝国的一年', '中文', 48.00, 2, 4, 0),
('明朝那些事儿', '当年明月', '中国海关出版社', 'B区-01架-2层', '通俗易懂的明朝史', '中文', 158.00, 2, 10, 0),
('史记', '司马迁', '中华书局', 'B区-02架-1层', '史家之绝唱，无韵之离骚', '中文', 260.00, 2, 2, 0),
('算法导论', 'Thomas H. Cormen', '机械工业出版社', 'C区-01架-1层', '计算机算法领域的圣经', '中文', 128.00, 3, 5, 0),
('深度学习', 'Ian Goodfellow', '人民邮电出版社', 'C区-01架-2层', 'AI领域的花书', '中文', 168.00, 3, 4, 0),
('Clean Code', 'Robert C. Martin', '电子工业出版社', 'C区-02架-1层', '编写高质量代码的指南', '中文', 79.00, 3, 6, 0),
('烹饪基础', '李小龙', '生活出版社', 'D区-01架-1层', '零基础学做菜', '中文', 25.00, 4, 15, 0),
('家庭收纳术', '近藤麻理惠', '中信出版社', 'D区-01架-2层', '整理生活的艺术', '中文', 39.00, 4, 10, 0),
('园艺手册', '张三', '农业出版社', 'D区-02架-1层', '阳台种菜指南', '中文', 32.00, 4, 5, 0),
('儿童心理学', '皮亚杰', '教育科学出版社', 'E区-01架-1层', '了解孩子的成长', '中文', 58.00, 5, 4, 0),
('有效教学', '库宁', '华东师范大学出版社', 'E区-01架-2层', '提升课堂教学效率', '中文', 45.00, 5, 3, 0),
('苏菲的世界', '乔斯坦·贾德', '作家出版社', 'A区-03架-1层', '哲学启蒙读物', '中文', 42.00, 5, 7, 0),
('白夜行', '东野圭吾', '南海出版公司', 'A区-03架-2层', '人性深处的黑暗与救赎', '中文', 49.50, 1, 9, 0),
('平凡的世界', '路遥', '北京十月文艺出版社', 'A区-04架-1层', '奋斗者的史诗', '中文', 99.00, 1, 11, 0),
('自私的基因', '理查德·道金斯', '中信出版社', 'C区-03架-1层', '生命演化的新视角', '中文', 68.00, 3, 6, 0),
('心理学与生活', '津巴多', '人民邮电出版社', 'E区-02架-1层', '心理学经典入门教材', '中文', 88.00, 5, 5, 0);

INSERT INTO reader_info (username, password, real_name, sex, tel, email, reader_number, status) VALUES
('reader001', '202cb962ac59075b964b07152d234b70', '张三', 1, '13800138000', 'zhangsan@example.com', 'R20240001', 0),
('reader002', '202cb962ac59075b964b07152d234b70', '李四', 2, '13800138001', 'lisi@example.com', 'R20240002', 0);

INSERT INTO notice (title, content, admin_id, status) VALUES
('欢迎使用图书管理系统', '这是一个现代化的图书管理系统，支持在线查找、借阅、归还等功能。', 1, 1),
('系统维护公告', '系统每周采用开始维护，维护时间为凌晨2:00-4:00，请合理安排借阅计划。', 1, 1);



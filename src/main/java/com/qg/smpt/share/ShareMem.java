package com.qg.smpt.share;

import com.qg.smpt.printer.OrdersDispatcher;
import com.qg.smpt.printer.PrinterProcessor;
import com.qg.smpt.printer.model.BPrinterStatus;
import com.qg.smpt.web.model.*;
import com.qg.smpt.web.repository.OrderMapper;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.javassist.bytecode.annotation.ShortMemberValue;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.io.Reader;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.CountDownLatch;

/**
 * 全局生命周期的共享对象
 * // TODO 何时注销这些对象呢?
 */
public final class ShareMem {



    public static Integer currentOrderNum = 0;

    /**
     * 关于userId - user 对象共享问题
     * 登录 打印机连接 订单发送均会查询该对象
     * 1. 检测 userIdMap.get(userId) == null ? 进行第二行 : 结束
     * 2. synchronized(Sha reMem.userIdMap) {  some code }
     * 3. 去数据库中查询 user 信息(打印机和用户多表查询 selectPrinterUser)并放入 userIdMap 对象中
     */
    public static Map<Integer, User> userIdMap = null;                      // 用户id-用户, 用户登陆, 打印机初次发送请求, 订单初次生成
    // 都要向该变量中添加 id - user
    // TODO 编码严重漏洞，共享变量未实现共享
    public static Map<Integer, Printer> printerIdMap = null;                // 打印机id-打印机, 只有当打印机发送请求连接时才建立该对象

    public static Map<Integer, List<Printer>> userListMap = null;           // 用户-打印机 废弃该共享变量，使用 user 对象中包含的变量
    // TODO 注意对象的生命周期, 而没有保持同步
    /**
     * 关于userId - BulkOrder集合问题
     * 当用户打印机均未连接到服务器时, 将订单数据先保存在该对象中, 倘若任意一台打印机设备已连接服务器, 则忽略该对象
     */
    public static Map<Integer, List<Order>> userOrderBufferMap = null;     // 用户id-缓冲队列

    public static Map<Printer, List<BulkOrder>> priSentQueueMap = null;     // 打印机-已发批次队列

    public static Map<Printer, List<BulkOrder>> priExceQueueMap = null;     // 打印机-异常队列

    public static Map<Printer, List<BulkOrder>> priBufferMapList = null;    // 打印机-待发送批次队列

    public static Map<Printer, PrinterProcessor> priPriProcessMap = null;   // 打印机对应的处理线程, 做成动态效果。

    // 当触发一个读事件时，进行一次线程绑定，触发完毕，解除绑定

    public static Map<Printer, SocketChannel> priSocketMap = null;          // 打印机-socket

    /***
     * 合同网下新增共享变量
     */

    public static Map<Short, List<Order>> compactBulkMap = null;                // 合同网-对应订单列表

    public static Map<Short, List<Printer>> compactPrinter = null;                 //合同号-进行投标了的主控板

    public static Map<Short, List<Printer>> compactOfPrinter = null;                 //合同号-已经进行筛选后参与此合同网的主控板

    public static Map<Integer, OrdersDispatcher> userIdOrdersDispatcher = null;   //用户id和对应的订单委派调度线程

    /**
     * 前端所需展示状态的各种共享变量信息
     */

    /**
     * 主控板状态：主控板id-主控板信息
     */
    public static Map<Integer,Printer> printerStatusMap;
    /**
     * 打印机单元状态：打印机单元id-打印机单元信息
     */
    public static Map<Integer,PrinterUnitDetail> printerUnitStatusMap;
    /**
     * 订单展示：订单Id-正在打印的订单
     */
    public static Map<Integer,List<Item>> itemToShow;

    /**
     * 批次展示：批次Id-批次内信息
     */
    public static Map<Integer,BulkOrder> bulkOrderToShow;

    /**
     * 系统状态
     */
    public static Map<String,Object> systemStatus;

    /**
     * 批次吞吐量
     */
    public static Map<String,Object> typeSpeed;


    static {
        // 初始化订单ID
        initOrderId();

        userIdMap = new HashMap<Integer, User>();

        printerIdMap = new HashMap<Integer, Printer>();

        userListMap = new HashMap<Integer, List<Printer>>();

        userOrderBufferMap = new HashMap<Integer, List<Order>>();

        priSentQueueMap = new HashMap<Printer, List<BulkOrder>>();

        priExceQueueMap = new HashMap<Printer, List<BulkOrder>>();

        priBufferMapList = new HashMap<Printer, List<BulkOrder>>();

        priPriProcessMap = new HashMap<Printer, PrinterProcessor>();

        priSocketMap = new HashMap<Printer, SocketChannel>();

        compactBulkMap = new HashMap<Short, List<Order>>();

        compactPrinter = new HashMap<Short, List<Printer>>();

        userIdOrdersDispatcher = new HashMap<Integer,OrdersDispatcher>();

        compactOfPrinter = new HashMap<Short, List<Printer>>();

        itemToShow = new HashMap<>();

        bulkOrderToShow = new HashMap<>();

        printerStatusMap = new HashMap<>();

        printerUnitStatusMap = new HashMap<>();

        systemStatus = new HashMap<>();

        typeSpeed = new HashMap<>();
    }

    private static void initOrderId() {
        String resource = "mybatis/mybatis-config.xml";
        Reader reader = null;
        try {
            reader = Resources.getResourceAsReader(resource);
        } catch (IOException e) {
            System.out.println(e.getMessage());

        }
        SqlSessionFactory sqlSessionFactory = new SqlSessionFactoryBuilder().build(reader);
        SqlSession sqlSession = sqlSessionFactory.openSession();
        OrderMapper orderMapper = sqlSession.getMapper(OrderMapper.class);
        try {
            currentOrderNum = orderMapper.selectMaxOrderId();
        }finally {
            sqlSession.close();
        }
    }
}

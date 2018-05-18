package com.qg.smpt.web.processor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import com.qg.smpt.printer.Compact;
import com.qg.smpt.util.OrderBuilder;
import com.qg.smpt.web.model.*;
import com.qg.smpt.web.model.Json.PrinterDetail;
import com.qg.smpt.web.repository.PrinterMapper;
import com.qg.smpt.web.repository.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.qg.smpt.share.ShareMem;
import com.qg.smpt.util.JsonUtil;
import com.qg.smpt.util.Level;
import com.qg.smpt.util.Logger;
import com.qg.smpt.web.service.UserService;

@Controller
public class PrinterController {
    private static final Logger LOGGER = Logger.getLogger(PrinterController.class);

    @Autowired
    private UserService userService;

    @Autowired
    private PrinterMapper printerMapper;

    @Autowired
    private UserMapper userMapper;

    @RequestMapping(value="/printer/{userId}", method=RequestMethod.GET, produces="application/json;charset=UTF-8")
    @ResponseBody
    public String seePrinterStatus(@PathVariable int userId) {

        // 从session中获取用户
//		HttpSession session = request.getSession();
//		User user = (User) session.getAttribute("user");
//		int userId = ((user != null) ? user.getId() : 0);
        LOGGER.log(Level.DEBUG, "查看用户[{0}]的打印机状态 ", userId);

        // 根据用户id获取打印机
        User user = ShareMem.userIdMap.get(userId);
        List<Printer> printers = null;
        // 若内存中没有用户，则去数据库中获取,并放进内存
        if(user == null) {
            user = userService.queryUserPrinter(userId);

            if(user != null && user.getPrinters() != null){
                synchronized (ShareMem.userIdMap) {
                    ShareMem.userIdMap.put(user.getId(), user);
                }
            }
        }

        printers = user.getPrinters();
        String json = JsonUtil.jsonToMap(new String[]{"retcode","data"},
                new Object[]{1 ,printers});

        LOGGER.log(Level.DEBUG, "当前转化的信息为 [{0}]", json);

        return json;
    }

    @RequestMapping(value="/printer/status/{printerId}",  method=RequestMethod.GET, produces="application/json;charset=UTF-8" )
    @ResponseBody
    public String queryPrinter(@PathVariable int printerId) {
        // 根据打印机 id 获取打印机
        Printer printer = ShareMem.printerIdMap.get(printerId);
        PrinterDetail printerDetail = null;
        if(printer != null) {
            printerDetail = new PrinterDetail(printer);
        }else {
            LOGGER.debug("找不到id为" + printerId + "的打印机");
        }
        return JsonUtil.jsonToMap(new String[]{"printer"}, new Object[]{printerDetail});
    }

    @RequestMapping(value="/printer/{printerId}",  method=RequestMethod.DELETE, produces="application/json;charset=UTF-8" )
    @ResponseBody
    public String resetPrinter(@PathVariable int printerId) {
        // 根据打印机 id 获取打印机
        Printer printer = ShareMem.printerIdMap.get(printerId);

        // 若当前打印机存在，则将打印机的内部打印订单信息全重置
        if(printer != null) {
            synchronized (printer) {
                printer.reset();
            }
        }
        return JsonUtil.jsonToMap(new String[]{"status"}, new Object[]{"SUCCESS"});
    }

    /**
     * 系统状态提醒展示
     * @return
     */
    @RequestMapping(value = "/system/status",method=RequestMethod.POST ,produces="application/json;charset=UTF-8")
    @ResponseBody
    public String systemStatus(){
        int printersSum = ShareMem.printerIdMap.size();
        int unitsSum = ShareMem.printerUnitStatusMap.size();
        Map<String,Object> systemStatus = new HashMap<>();
        systemStatus.put("printersSum",printersSum);
        systemStatus.put("unitsSum",unitsSum);
        systemStatus.put("typesSum",ShareMem.systemStatus.get("typesSum"));
        systemStatus.put("isTyping",ShareMem.systemStatus.get("isTyping"));
        systemStatus.put("errorRate",0.1);
        systemStatus.put("handlingCapacity",300);
        String json =  JsonUtil.jsonToMap(new String[]{"retcode","data"},
                new Object[]{Constant.TRUE,systemStatus});
        return json;
    }

    /**
     * 展示所有主控板的接口
     * @return
     */
    @RequestMapping(value = "/system/showprinters",method=RequestMethod.POST ,produces="application/json;charset=UTF-8")
    @ResponseBody
    public String showPrinter(){
        List<Map<String,Object>> printersStatus = new ArrayList<>();
        int i;
        for (i=0;i<ShareMem.printerIdMap.size();i++) {
            Map<String,Object> printerStatus = new HashMap<>(3);
            printerStatus.put("printerId", ShareMem.printerIdMap.get(i).getId());
            printerStatus.put("printerStatus",ShareMem.printerIdMap.get(i).getPrinterStatus());
            printerStatus.put("unitsSum",ShareMem.printerIdMap.get(i).getPrinterUnitSize());
            printersStatus.add(printerStatus);
        }
        String json =  JsonUtil.jsonToMap(new String[]{"retcode","data"},
                new Object[]{Constant.TRUE,printersStatus});
        return json;
    }

    /**
     * 查看主控板信息
     * @param printerId
     * @return
     */
    @RequestMapping(value="/system/printerdetail/{printerId}",  method=RequestMethod.POST ,produces="application/json;charset=UTF-8")
    @ResponseBody
    public String showPrinterDetail(@PathVariable int printerId){
        Map<String,Object> printerDetail = new HashMap<>();
        Printer printer = ShareMem.printerIdMap.get(printerId);
        if (printer == null){
            return JsonUtil.jsonToMap(new String[]{"status"}, new Object[]{"EMPTY"});
        }
        long hasTypedTime = printer.getLastSendTime()-printer.getFirstSendTime();
        printerDetail.put("hasTypedTime",hasTypedTime/1000 + "秒");
        if(printer.getPrinterStatus() == "14"){
            printerDetail.put("printerStatus","正在打印");
        }else {
            printerDetail.put("printerStatus","不在打印");
        }
        printerDetail.put("orderSum",printer.getOredrsNum());
        printerDetail.put("cutSum",108);
        printerDetail.put("cunErrorSum",12);
        String json =  JsonUtil.jsonToMap(new String[]{"retcode","data"},
                new Object[]{Constant.TRUE,printerDetail});
        return json;
    }

    /**
     * 查询订单接口
     * @return
     */
    @RequestMapping(value="/show/menu",  method=RequestMethod.POST ,produces="application/json;charset=UTF-8")
    @ResponseBody
    public String showMenu(){
        List<Map<String,Object>> menuList = new ArrayList<>();
        Item item;
        for (Integer key:ShareMem.itemToShow.keySet()) {
            item = ShareMem.itemToShow.get(key);
            Map<String,Object> menuDetail = new HashMap<>();
            menuDetail.put("number",key);
            menuDetail.put("time",item.getOrderTime());
            menuDetail.put("good",item.getName());
            menuDetail.put("price",item.getPrice());
            menuDetail.put("count",item.getCount());
            menuList.add(menuDetail);
        }
        String json =  JsonUtil.jsonToMap(new String[]{"retcode","data"},
                new Object[]{Constant.TRUE,menuList});
        return json;
    }

    @RequestMapping(value="/show/batch",  method=RequestMethod.POST ,produces="application/json;charset=UTF-8")
    @ResponseBody
    public String showBatch(){
        List<Map<String,Object>> batchList = new ArrayList<>();
        BulkOrder bulkOrder;
        for (Integer key:ShareMem.bulkOrderToShow.keySet()){
            bulkOrder = ShareMem.bulkOrderToShow.get(key);
            Map<String,Object> batchDetail = new HashMap<>();
            batchDetail.put("number",bulkOrder.getId());
            batchDetail.put("sendTime",bulkOrder.getSendTimeToShow());
            List<Integer> ordersId = new ArrayList<>();
            for (int i = 0;i<bulkOrder.getOrders().size();i++){
                ordersId.add(bulkOrder.getOrders().get(i).getId());
            }
            batchDetail.put("orderId",ordersId);
            batchDetail.put("orderNum",bulkOrder.getReceNum());
            batchList.add(batchDetail);
        }
        String json =  JsonUtil.jsonToMap(new String[]{"retcode","data"},
                new Object[]{Constant.TRUE,batchList});
        return json;
    }

    /***
     * 添加打印机
     * @param
     * @return
     */
    @RequestMapping(value="/printer/add/{userId}/{printerId}",  method=RequestMethod.POST ,produces="application/json;charset=UTF-8")
    @ResponseBody
    public String addPrinter(@PathVariable int printerId, @PathVariable int userId) {
        Printer printer= new Printer();
        printer.setId(printerId);
        printer.setUserId(userId);
        printer.setPrinterStatus(String.valueOf((int)(Constant.PRINTER_HEATHY)));
        printerMapper.addPrinter(printer);
        printerMapper.addPrinterConnection(printer);

        User user = userMapper.selectByPrimaryKey(userId);
        user.setUserPrinters(user.getUserPrinters()+1);
        userMapper.updateByPrimaryKey(user);

        return JsonUtil.jsonToMap(new String[]{"status"}, new Object[]{"SUCCESS"});
    }



    /***
     * 发送合同网数据报文，暂时不用
     * 1
     * @param
     * @return
     */
    @RequestMapping(value="/printer/sendCompact/{userId}/{number}",  method=RequestMethod.GET ,produces="application/json;charset=UTF-8")
    @ResponseBody
    public String sendCompact(@PathVariable int number,@PathVariable int userId) {
        Compact compact = new Compact();
        List<Order> orders = new ArrayList<Order>();
        for (int i = 0; i<number; i++){
            orders.add(OrderBuilder.produceOrder(false,false));
        }
        compact.sendOrdersByCompact(userId,1,orders);
        return JsonUtil.jsonToMap(new String[]{"status"}, new Object[]{"SUCCESS"});
    }

    /***
     * 直接发送数据报文
     * 1
     * @param
     * @return
     */
    @RequestMapping(value="/printer/sendBulk/{userId}/{flag}/{number}/{size}/{point}",  method=RequestMethod.GET ,produces="application/json;charset=UTF-8")
    @ResponseBody
    public String sendBulk(@PathVariable int userId,@PathVariable int number,@PathVariable int flag, @PathVariable int size,@PathVariable double point) {		//此处先设置简略的逻辑
        if(ShareMem.userIdMap.get(userId)==null){
            return "请先连接打印机";
        }

        Compact compact = new Compact();
        if (ShareMem.priSocketMap.get(ShareMem.printerIdMap.get(compact.getMaxCreForBulkPrinter(userId)))==null){
            return "打印机目前已断开，请先连接打印机";
        }
        double finalsize;
        finalsize = size + 0.1 * point;
        //如果 size 不合规格，修改之
        if(finalsize > 10 || finalsize < 0){
            finalsize = 0;
        }
        List<Order> orders = new ArrayList<Order>();
        for (int i = 0; i<number; i++){
            orders.add(OrderBuilder.produceOrder(finalsize,false,false));
        }
        compact.sendBulkDitectly(userId,flag,orders);
        return JsonUtil.jsonToMap(new String[]{"status"}, new Object[]{"SUCCESS"});
    }

    /***
     * 测试接口，多台主控板平均分配订单，用于测试订单跟踪
     * 1
     * @param
     * @return
     */
    @RequestMapping(value="/printer/test/{userId}/{number}{size}/{point}",  method=RequestMethod.GET ,produces="application/json;charset=UTF-8")
    @ResponseBody
    public String test(@PathVariable int userId, @PathVariable int number, @PathVariable int size,@PathVariable int point) {
        //此处先设置简略的逻辑
        if(ShareMem.userIdMap.get(userId)==null){
            return "打印机目前已断开，请先连接打印机";
        }
        double finalsize;
        finalsize = size + 0.1*point;
        //如果 size 不合规格，修改之
        if(finalsize > 10 || finalsize < 0){
            finalsize = 0;
        }

        Compact compact = new Compact();
        List<Order> orders = new ArrayList<Order>();
        for (int i = 0; i<number; i++){
            orders.add(OrderBuilder.produceOrder(finalsize,false,false));
        }
        compact.test(userId,orders);
        return JsonUtil.jsonToMap(new String[]{"status"}, new Object[]{"SUCCESS"});
    }


    /***
     * 测试接口，指定某台打印机进行打印任务
     * 1
     * @param
     * @return
     */
    @RequestMapping(value="/printer/choicePrinter/{printerId}/{number}/{size}/{point}",  method=RequestMethod.GET ,produces="application/json;charset=UTF-8")
    @ResponseBody
    public String choicePrinter(@PathVariable int printerId, @PathVariable int number, @PathVariable int size,@PathVariable int point) {
        //此处先设置简略的逻辑
//        if(ShareMem.priSocketMap.get(ShareMem.printerIdMap.get(printerId))==null){
//            return "打印机目前已断开，请先连接打印机";
//        }
        double finalsize;
        finalsize = size + 0.1 * point;
        //如果 size 不合规格，修改之
        if(finalsize > 10 || finalsize < 0){
            finalsize = 0;
        }
        LOGGER.log(Level.DEBUG,"size 00<<< is " + size,PrinterController.class);
        Compact compact = new Compact();
        List<Order> orders = new ArrayList<Order>();
        for (int i = 0; i<number; i++){
            orders.add(OrderBuilder.produceOrder(finalsize,false,false));
        }
        compact.sendByPrinter(printerId,orders);
        LOGGER.log(Level.DEBUG,"json success>>>>",PrinterController.class);
        return JsonUtil.jsonToMap(new String[]{"status"}, new Object[]{"SUCCESS"});
    }
}

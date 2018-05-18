package com.qg.smpt.web.model;

/**
 * Created by K Lin
 * Date: 2018/5/17.
 * Time: 21:50
 * Remember to sow in the spring.
 * Description : smart_printers
 */
public class PrinterUnitDetail{

    private Integer id;                     //打印机单元id
    private String printerUnitStatus;       //打印机单元状态
    private Integer printerId;              //打印单元所属主控板id
    private int userId;                      //用户id
    private volatile boolean isBusy;         //true-忙时，false-闲时
    private volatile long printeTime;      //已打印时长

    private int printNum;            // 总打印次数
    private int sendedOrdersNum;     // 已打印订单数量
    private int unsendedOrdersNum;  // 未发送订单数量
    private int printSuccessNum;    // 打印成功数量
    private int printErrorNum;      // 打印失败数量
    private int successRate;        // 成功率

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getPrinterUnitStatus() {
        return printerUnitStatus;
    }

    public void setPrinterUnitStatus(String printerUnitStatus) {
        this.printerUnitStatus = printerUnitStatus;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public boolean isBusy() {
        return isBusy;
    }

    public void setBusy(boolean busy) {
        isBusy = busy;
    }

    public long getPrinteTime() {
        return printeTime;
    }

    public void setPrinteTime(long printeTime) {
        this.printeTime = printeTime;
    }

    public int getPrintNum() {
        return printNum;
    }

    public void setPrintNum(int printNum) {
        this.printNum = printNum;
    }

    public int getSendedOrdersNum() {
        return sendedOrdersNum;
    }

    public void setSendedOrdersNum(int sendedOrdersNum) {
        this.sendedOrdersNum = sendedOrdersNum;
    }

    public int getUnsendedOrdersNum() {
        return unsendedOrdersNum;
    }

    public void setUnsendedOrdersNum(int unsendedOrdersNum) {
        this.unsendedOrdersNum = unsendedOrdersNum;
    }

    public int getPrintSuccessNum() {
        return printSuccessNum;
    }

    public void setPrintSuccessNum(int printSuccessNum) {
        this.printSuccessNum = printSuccessNum;
    }

    public int getPrintErrorNum() {
        return printErrorNum;
    }

    public void setPrintErrorNum(int printErrorNum) {
        this.printErrorNum = printErrorNum;
    }

    public int getSuccessRate() {
        return successRate;
    }

    public void setSuccessRate(int successRate) {
        this.successRate = successRate;
    }

    public Integer getPrinterId() {
        return printerId;
    }

    public void setPrinterId(Integer printerId) {
        this.printerId = printerId;
    }
}

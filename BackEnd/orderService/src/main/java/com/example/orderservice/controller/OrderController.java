package com.example.orderservice.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.example.orderservice.entity.Order;
import com.example.orderservice.entity.Statement;
import com.example.orderservice.mapper.OrderMapper;
import com.example.orderservice.mapper.StatementMapper;
import com.example.orderservice.request.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.security.SecureRandom;
import java.time.Instant;
import java.time.Month;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import static com.zaxxer.hikari.util.ClockSource.toMillis;

@RestController
@CrossOrigin("*")
@RequestMapping()
public class OrderController {
    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private StatementMapper statementMapper;

    @GetMapping("/getOrdersForPassenger/{passenger_id}")
    public List<TaxiOrder> getOrdersForPassenger(@PathVariable String passenger_id){
        List<TaxiOrder> orderList=new ArrayList<TaxiOrder>();
        QueryWrapper<Order> orderQueryWrapper=new QueryWrapper<>();
        orderQueryWrapper.eq("passenger_id",passenger_id).orderByDesc("order_id");
        List<Order> tempList=orderMapper.selectList(orderQueryWrapper);
        for(Order order:tempList){
            TaxiOrder taxiOrder=new TaxiOrder();
            taxiOrder.setOrder_id(order.getOrder_id());
            taxiOrder.setPassenger_id(order.getPassenger_id());
            taxiOrder.setDriver_id(order.getDriver_id());
            taxiOrder.setDeparture(order.getDeparture());
            taxiOrder.setDestination(order.getDestination());
            taxiOrder.setPrice(order.getPrice());
            QueryWrapper<Statement> statementQueryWrapper=new QueryWrapper<>();
            statementQueryWrapper.eq("order_id",order.getOrder_id()).orderByDesc("stat_time");
            Statement stat=statementMapper.selectList(statementQueryWrapper).get(0);
            taxiOrder.setOrder_state(stat.getOrder_state());
            taxiOrder.setTime(stat.getStat_time());
            orderList.add(taxiOrder);
        }
        return orderList;
        //return tmp.getOrdersForPassenger(passenger_id);
    }

    @GetMapping("/getOrdersForDriver/{driver_id}")
    public List<TaxiOrder> getOrdersForDriver(@PathVariable String driver_id){
        List<TaxiOrder> orderList=new ArrayList<TaxiOrder>();
        QueryWrapper<Order> orderQueryWrapper=new QueryWrapper<>();
        orderQueryWrapper.eq("driver_id",driver_id).orderByDesc("order_id");
        List<Order> tempList=orderMapper.selectList(orderQueryWrapper);
        for(Order order:tempList){
            TaxiOrder taxiOrder=new TaxiOrder();
            taxiOrder.setOrder_id(order.getOrder_id());
            taxiOrder.setPassenger_id(order.getPassenger_id());
            taxiOrder.setDriver_id(order.getDriver_id());
            taxiOrder.setDeparture(order.getDeparture());
            taxiOrder.setDestination(order.getDestination());
            taxiOrder.setPrice(order.getPrice());
            QueryWrapper<Statement> statementQueryWrapper=new QueryWrapper<>();
            statementQueryWrapper.eq("order_id",order.getOrder_id()).orderByDesc("stat_time");
            Statement stat=statementMapper.selectList(statementQueryWrapper).get(0);
            taxiOrder.setOrder_state(stat.getOrder_state());
            taxiOrder.setTime(stat.getStat_time());
            orderList.add(taxiOrder);
        }
        return orderList;
    }

    @GetMapping("/getOrderByID/{order_id}")
    public TaxiOrder getOrdersByID(@PathVariable String order_id){
        TaxiOrder taxiOrder=new TaxiOrder();
        QueryWrapper<Order> orderQueryWrapper=new QueryWrapper<>();
        orderQueryWrapper.eq("order_id",order_id);
        Order order=orderMapper.selectOne(orderQueryWrapper);
        taxiOrder.setOrder_id(order.getOrder_id());
        taxiOrder.setPassenger_id(order.getPassenger_id());
        taxiOrder.setDriver_id(order.getDriver_id());
        taxiOrder.setDeparture(order.getDeparture());
        taxiOrder.setDestination(order.getDestination());
        taxiOrder.setPrice(order.getPrice());
        QueryWrapper<Statement> statementQueryWrapper=new QueryWrapper<>();
        statementQueryWrapper.eq("order_id",order.getOrder_id()).orderByDesc("stat_time");
        Statement stat=statementMapper.selectList(statementQueryWrapper).get(0);
        taxiOrder.setOrder_state(stat.getOrder_state());
        taxiOrder.setTime(stat.getStat_time());
        return taxiOrder;
    }
/*
    @PostMapping("/cancelOrder")
    public boolean cancelOrder(@RequestParam String order_id){
        QueryWrapper<Order> orderQueryWrapper=new QueryWrapper<>();
        orderQueryWrapper.eq("order_id",order_id);
        if(orderMapper.selectOne(orderQueryWrapper)==null){
            return false;
        }
        QueryWrapper<Statement> statementQueryWrapper=new QueryWrapper<>();
        statementQueryWrapper.eq("order_id",order_id);
        List<Statement> statementList=statementMapper.selectList(statementQueryWrapper);
        boolean sendFlag=false;
        for(Statement statement:statementList){
            if(!statement.getOrder_state().equals("已创建")&&!statement.getOrder_state().equals("已派单")){
                //sendFlag=false;
                return false;
            }else if(statement.getOrder_state().equals("已派单")){
                sendFlag=true;
            }
        }
        Statement statement=new Statement();
        statement.setOrder_id(order_id);
        statement.setOrder_state("已取消");
        //插入流水
        StringBuilder stat_id= new StringBuilder();
        while(true) {
            stat_id=new StringBuilder();
            Random rd = new SecureRandom();
            for (int i = 0; i < 16; i++) {
                int bit = rd.nextInt(10);
                stat_id.append(String.valueOf(bit));
            }
            QueryWrapper<Statement> statementWrapper=new QueryWrapper<>();
            statementWrapper.eq("stat_id",stat_id.toString());
            if(statementMapper.selectOne(statementWrapper)==null){
                break;
            }
        }
        statement.setStat_id(stat_id.toString());
        statement.setStat_time(Instant.now().plusMillis(TimeUnit.HOURS.toMillis(8)));
        statementMapper.insert(statement);
        //已派单状态下需要通知派单微服务释放司机
        if(sendFlag){

        }
        return true;
    }

    @Transactional
    @PostMapping("/newOrder")
    public void newOrder(@RequestParam("passenger_id") String passenger_id,
                         @RequestParam("departure") String departure,
                         @RequestParam("destination") String destination){
        Order order=new Order();
        StringBuilder order_id=new StringBuilder();
        order_id.append(java.time.Year.now().toString());
        order_id.append(java.time.MonthDay.now().toString().replaceAll("-",""));
        if(order_id.toString().length()<"20210101".length()){
            order_id.insert(4,"0");
        }
        while(true) {
            order_id=new StringBuilder(order_id.substring(0,8));
            Random rd = new SecureRandom();
            for (int i = 0; i < 8; i++) {
                int bit = rd.nextInt(10);
                order_id.append(String.valueOf(bit));
            }
            QueryWrapper<Order> orderQueryWrapper=new QueryWrapper<>();
            orderQueryWrapper.eq("order_id",order_id.toString());
            if(orderMapper.selectOne(orderQueryWrapper)==null){
                break;
            }
        }
        order.setOrder_id(order_id.toString());
        order.setPassenger_id(passenger_id);
        order.setDeparture(departure);
        order.setDestination(destination);
        orderMapper.insert(order);

        Statement statement=new Statement();
        statement.setOrder_id(order_id.toString());
        statement.setOrder_state("已创建");
        statement.setStat_time(Instant.now().plusMillis(TimeUnit.HOURS.toMillis(8)));
        StringBuilder stat_id= new StringBuilder();
        while(true) {
            stat_id=new StringBuilder();
            Random rd = new SecureRandom();
            for (int i = 0; i < 16; i++) {
                int bit = rd.nextInt(10);
                stat_id.append(String.valueOf(bit));
            }
            QueryWrapper<Statement> statementQueryWrapper=new QueryWrapper<>();
            statementQueryWrapper.eq("stat_id",stat_id.toString());
            if(statementMapper.selectOne(statementQueryWrapper)==null){
                break;
            }
        }
        statement.setStat_id(stat_id.toString());
        statementMapper.insert(statement);
    }

    @PostMapping("/orderTaken")
    public void orderTaken(@RequestParam("order_id") String order_id,@RequestParam("driver_id") String driver_id){
        //判断是否有该订单
        QueryWrapper<Order> orderQueryWrapper=new QueryWrapper<>();
        orderQueryWrapper.eq("order_id",order_id);
        Order order=orderMapper.selectOne(orderQueryWrapper);
        if(order==null){
            return;
        }
        //判断是否已派单
        if(order.getDriver_id()==null||order.getDriver_id().equals("")||order.getDriver_id().isEmpty()) {
            UpdateWrapper<Order> orderUpdateWrapper = new UpdateWrapper<>();
            orderUpdateWrapper.eq("order_id", order_id).set("driver_id", driver_id);
            orderMapper.update(order, orderUpdateWrapper);
        }
        //判断是否有已取消的流水，为后续分情况处理，插入新流水
        QueryWrapper<Statement> statementQueryWrapper=new QueryWrapper<>();
        statementQueryWrapper.eq("order_id",order_id);
        List<Statement> statementList=statementMapper.selectList(statementQueryWrapper);
        boolean flag=false;
        for(Statement statement:statementList){
            if(!statement.getOrder_state().equals("已创建")&&!statement.getOrder_state().equals("已取消")){
                return;
            }
            else if(statement.getOrder_state().equals("已取消")){
                flag=false;
                break;
            }
            else if(statement.getOrder_state().equals("已创建")){
                flag=true;
            }
        }
        if(flag) {
            Statement statement1 = new Statement();
            statement1.setOrder_id(order_id);
            statement1.setOrder_state("已派单");
            statement1.setStat_time(Instant.now().plusMillis(TimeUnit.HOURS.toMillis(8)));
            StringBuilder stat_id = new StringBuilder();
            while (true) {
                stat_id = new StringBuilder();
                Random rd = new SecureRandom();
                for (int i = 0; i < 16; i++) {
                    int bit = rd.nextInt(10);
                    stat_id.append(String.valueOf(bit));
                }
                QueryWrapper<Statement> statementWrapper = new QueryWrapper<>();
                statementWrapper.eq("stat_id", stat_id.toString());
                if (statementMapper.selectOne(statementWrapper) == null) {
                    break;
                }
            }
            statement1.setStat_id(stat_id.toString());
            statementMapper.insert(statement1);
            //读取取消订单队列消息，如果有取消需要通知派单微服务释放司机
        }else{//通知派单微服务释放司机

        }
    }

    @PostMapping("/passengerOn")
    public void passengerOn(@RequestParam("order_id") String order_id){
        //判断是否有该订单
        QueryWrapper<Order> orderQueryWrapper=new QueryWrapper<>();
        orderQueryWrapper.eq("order_id",order_id);
        Order order=orderMapper.selectOne(orderQueryWrapper);
        if(order==null){
            return;
        }
        //判断是否已派单
        QueryWrapper<Statement> statementQueryWrapper=new QueryWrapper<>();
        statementQueryWrapper.eq("order_id",order_id);
        List<Statement> statementList=statementMapper.selectList(statementQueryWrapper);
        boolean flag=false;
        for(Statement statement:statementList){
            if(statement.getOrder_state().equals("已开始")||statement.getOrder_state().equals("已结束")||statement.getOrder_state().equals("已取消")){
                return;
            }
            else if(statement.getOrder_state().equals("已派单")){
                flag=true;
            }
        }
        //插入新流水
        if(flag){
            Statement statement1 = new Statement();
            statement1.setOrder_id(order_id);
            statement1.setOrder_state("已开始");
            statement1.setStat_time(Instant.now().plusMillis(TimeUnit.HOURS.toMillis(8)));
            StringBuilder stat_id = new StringBuilder();
            while (true) {
                stat_id = new StringBuilder();
                Random rd = new SecureRandom();
                for (int i = 0; i < 16; i++) {
                    int bit = rd.nextInt(10);
                    stat_id.append(String.valueOf(bit));
                }
                QueryWrapper<Statement> statementWrapper = new QueryWrapper<>();
                statementWrapper.eq("stat_id", stat_id.toString());
                if (statementMapper.selectOne(statementWrapper) == null) {
                    break;
                }
            }
            statement1.setStat_id(stat_id.toString());
            statementMapper.insert(statement1);
        }
    }

    @PostMapping("/passengerOff")
    public void passengerOff(@RequestParam("order_id") String order_id,@RequestParam("price") Double price){
        //判断是否有该订单
        QueryWrapper<Order> orderQueryWrapper=new QueryWrapper<>();
        orderQueryWrapper.eq("order_id",order_id);
        Order order=orderMapper.selectOne(orderQueryWrapper);
        if(order==null){
            return;
        }
        //判断是否已开始
        QueryWrapper<Statement> statementQueryWrapper=new QueryWrapper<>();
        statementQueryWrapper.eq("order_id",order_id);
        List<Statement> statementList=statementMapper.selectList(statementQueryWrapper);
        boolean flag=false;
        for(Statement statement:statementList){
            if(statement.getOrder_state().equals("已结束")||statement.getOrder_state().equals("已取消")){
                return;
            }
            else if(statement.getOrder_state().equals("已开始")){
                flag=true;
            }
        }
        //修改订单金额字段，插入新流水
        if(flag){
            UpdateWrapper<Order> orderUpdateWrapper = new UpdateWrapper<>();
            orderUpdateWrapper.eq("order_id", order_id).set("double", price);
            orderMapper.update(order, orderUpdateWrapper);

            Statement statement1 = new Statement();
            statement1.setOrder_id(order_id);
            statement1.setOrder_state("已结束");
            statement1.setStat_time(Instant.now().plusMillis(TimeUnit.HOURS.toMillis(8)));
            StringBuilder stat_id = new StringBuilder();
            while (true) {
                stat_id = new StringBuilder();
                Random rd = new SecureRandom();
                for (int i = 0; i < 16; i++) {
                    int bit = rd.nextInt(10);
                    stat_id.append(String.valueOf(bit));
                }
                QueryWrapper<Statement> statementWrapper = new QueryWrapper<>();
                statementWrapper.eq("stat_id", stat_id.toString());
                if (statementMapper.selectOne(statementWrapper) == null) {
                    break;
                }
            }
            statement1.setStat_id(stat_id.toString());
            statementMapper.insert(statement1);
        }
    }

 */
}

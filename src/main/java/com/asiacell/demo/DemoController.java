package com.asiacell.demo;

import com.asiacell.demo.data.Order;
import com.paypal.http.HttpResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/")
public class DemoController {
    @Autowired
    private PaypalService paypalService;
    @GetMapping(produces = "application/json; charset=UTF-8")
    public Map<String,Object> index(){
        Map<String,Object> result = new LinkedHashMap<>();
        result.put("success",true);
        result.put("data","ok");
        return result;
    }

    @PostMapping(produces = "application/json; charset=UTF-8")
    public Map<String,Object>post(@RequestBody Order order){
        Map<String,Object> result = new LinkedHashMap<>();
        Map<String,String>orderItem = new LinkedHashMap<>();
        try {
            if( order.getReferenceId() == null) {
                order.setReferenceId( UUID.randomUUID().toString());
                HttpResponse<com.paypal.orders.Order> orderHttpResponse = paypalService.createOrder( order.getReferenceId() , order.getAmount());
                result.put("success",true);
                orderItem.put("orderId", orderHttpResponse.result().id());
                result.put("data", orderItem);
                result.put("message","Success");
            }
        }catch (Exception ex){
            result.put("success",false);
            result.put("message",ex.getMessage());
        }

        return result;
    }
}

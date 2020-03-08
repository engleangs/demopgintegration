package com.asiacell.demo;

import com.asiacell.demo.data.Result;
import com.paypal.http.HttpResponse;
import com.paypal.orders.Order;

import java.io.IOException;
import java.util.Map;

public interface PaypalService {
    HttpResponse<Order> createOrder(String referenceId, double amount) throws IOException;
}

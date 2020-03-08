package com.asiacell.demo;

import com.google.gson.Gson;
import com.paypal.core.PayPalEnvironment;
import com.paypal.core.PayPalHttpClient;
import com.paypal.http.HttpResponse;
import com.paypal.orders.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
public class PaypalServiceImpl implements PaypalService {

    private static final Logger LOGGER = LoggerFactory.getLogger(PaypalServiceImpl.class);
    @Value("${paypal.clientId}")
    private String clientId;
    @Value("${paypal.secrete}")
    private String secrete;
    @Autowired
    private Gson gson;
    private PayPalHttpClient payPalHttpClient;
    private PayPalEnvironment payPalEnvironment;
    @PostConstruct
    private void init(){
        payPalEnvironment = new PayPalEnvironment.Sandbox( clientId,secrete);
        payPalHttpClient = new PayPalHttpClient( payPalEnvironment);

    }
    @Override
    public HttpResponse<Order> createOrder(String referenceId,double amount) throws IOException {
        boolean debug = true;
        OrdersCreateRequest request = new OrdersCreateRequest();
        request.prefer("return=representation");
        request.requestBody( buildOrderRequest( referenceId ,amount));
        HttpResponse<Order>response =payPalHttpClient.execute( request);
        if( debug) {
            if( response.statusCode() == 201) {
                LOGGER.info("Status Code :"+response.statusCode());
                LOGGER.info("Status :"+response.result().status());
                LOGGER.info("Order ID :"+response.result().id());
                LOGGER.info("Intent :"+response.result().checkoutPaymentIntent());
                LOGGER.info("Links :");
                for(LinkDescription link:response.result().links()) {
                    LOGGER.info("\t"+link.rel()+" : "+link.href()+"\t Call Type:"+link.method());
                }
                LOGGER.info("Total Amount: "+response.result().purchaseUnits().get(0).amountWithBreakdown()
                .currencyCode()+" "+response.result().purchaseUnits().get(0).amountWithBreakdown().value());
            }
        }
        return response;
    }
    private OrderRequest buildOrderRequest(String referenceId,double amount){
        OrderRequest orderRequest = new OrderRequest();
        orderRequest.checkoutPaymentIntent("CAPTURE");
        ApplicationContext applicationContext  = new ApplicationContext().brandName("Asiacell plc").landingPage(
                "BILLING"
        ).shippingPreference("SET_PROVIDED_ADDRESS");
        orderRequest.applicationContext( applicationContext);
        List<PurchaseUnitRequest> purchaseUnitRequestList = new ArrayList<>();
        ArrayList<Item>items = new ArrayList<>();
        items.add( new Item().name("T-Shirt").description("Green XL").sku("sku01")
                .quantity("1")
        .unitAmount(new Money().currencyCode("USD").value( amount+"")));
        PurchaseUnitRequest purchaseUnitRequest = new PurchaseUnitRequest().referenceId(referenceId)
                .description("Sporting goods")
                .customId("CUST-HIGHFASHIONS")
                .softDescriptor("HighFashions")
                .amountWithBreakdown( new AmountWithBreakdown()
                .currencyCode("USD")
                        .value(amount+"")
                        .amountBreakdown(
                                new AmountBreakdown().itemTotal( new Money().currencyCode("USD").value(amount+""))
                        )

                ).items( items)
                .shippingDetail(new ShippingDetail().name(new Name().fullName("John Doe"))
                        .addressPortable(new AddressPortable().addressLine1("123 Townsend St").addressLine2("Floor 6")
                                .adminArea2("San Francisco").adminArea1("CA").postalCode("94107").countryCode("US")));
        purchaseUnitRequestList.add( purchaseUnitRequest);
        orderRequest.purchaseUnits( purchaseUnitRequestList);
        return orderRequest;
    }

}

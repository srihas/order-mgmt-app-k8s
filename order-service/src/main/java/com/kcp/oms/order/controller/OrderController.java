package com.kcp.oms.order.controller;

import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import com.kcp.oms.order.dto.OrderRequestResource;
import com.kcp.oms.order.dto.OrderResource;
import com.kcp.oms.order.dto.ProductInventoryResource;
import com.kcp.oms.order.model.Order;
import com.kcp.oms.order.rabbitmq.QueueProducer;
import com.kcp.oms.order.rabbitmq.ShippingMessage;
import com.kcp.oms.order.service.intf.OrderService;
import com.kcp.oms.order.service.mapper.DomainToResourceMapper;

import io.swagger.annotations.ApiOperation;

/**
 * 
 * @author kapanda
 *
 */
@RestController
@RequestMapping(value = "/orders", produces = { MediaType.APPLICATION_JSON_VALUE })
public class OrderController {
    protected Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private OrderService orderService;
    @Autowired
    private QueueProducer queueProducer;

    @Value("${INVENTORY_SERVICE_URI:http://localhost:9200}")
    private String inventoryServiceBaseUrl;


    @GetMapping
    @ApiOperation(value = "Find all orders", notes = "Returns all Orders")
    public ResponseEntity<List<OrderResource>> getOrders() {

        List<Order> orders = orderService.fetchAllOrders();

        List<OrderResource> orderResources = DomainToResourceMapper.map(orders);

        return new ResponseEntity<>(orderResources, HttpStatus.OK);
    }

    @GetMapping(value = "/{orderId}")
    @ApiOperation(value = "Find Order by id", notes = "Returns the Order by ID from inventory.")
    public ResponseEntity<OrderResource> getOrderById(@PathVariable("orderId") long orderId) {

        Order fetchedOrder = orderService.fetchOrderById(orderId);

        OrderResource orderResource = DomainToResourceMapper.map(fetchedOrder);

        return new ResponseEntity<>(orderResource, HttpStatus.OK);
    }

    @PostMapping(consumes = { MediaType.APPLICATION_JSON_VALUE })
    @ApiOperation(value = "Submit Orders for a user", notes = "Submit Orders for a user")
    public ResponseEntity<OrderResource> submitOrder(@RequestBody OrderRequestResource newOrderRequest) {

        // Get Inventory details
        Optional<ProductInventoryResource> productInventory = getProductInventoryByProductId(
                newOrderRequest.getProductId());
        // // Try to process order
        Order savedOrder = orderService.processOrder(newOrderRequest, productInventory);
        //
        OrderResource orderResource = DomainToResourceMapper.map(savedOrder);

        return new ResponseEntity<>(orderResource, HttpStatus.OK);

    }

    @DeleteMapping(path = "/{orderId}")
    @ApiOperation(value = "Delete Order by Id", notes = "delete Order")
    public ResponseEntity<OrderResource> cancelOrder(@PathVariable("orderId") long orderId) {

        orderService.deleteOrder(orderId);

        return new ResponseEntity<>(HttpStatus.OK);
    }

    public Optional<ProductInventoryResource> getProductInventoryByProductId(long productId) {
        ProductInventoryResource productInventoryResource = getInventoryByProductId(productId);
        return Optional.ofNullable(productInventoryResource);
    }

    private ProductInventoryResource getInventoryByProductId(long productId) {
        ProductInventoryResource inv = null;
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_XML);
            HttpEntity<ProductInventoryResource> entity = new HttpEntity<>(headers);
            RestTemplate restTemplate = new RestTemplate();
            ResponseEntity<ProductInventoryResource> response = restTemplate.exchange(inventoryServiceBaseUrl +"/inventory/" + productId,
                    HttpMethod.GET, entity, ProductInventoryResource.class);
            HttpStatus statusCode = response.getStatusCode();
            System.out.println("Response Satus Code: " + statusCode);
            if (statusCode == HttpStatus.OK) {
                inv = response.getBody();
                System.out.println("product Name: " + inv.getProductName());
                {
                    try {
                        ShippingMessage message = new ShippingMessage();
                        message.setAddress("Address to be delivered");
                        message.setLocation("Bangalore");
                        queueProducer.produce(message);
                        logger.info(
                                "Successfully sent the message to deliver the product in mentioned address of the user.");
                    } catch (Exception e) {
                        logger.error("error while sending message to shipping service.{}", e.getMessage());
                    }
                }
            }
        } catch (Exception e) {
            logger.error("error while fetching product from inventory service.{}", e.getMessage());
        }
        return inv;
    }
}

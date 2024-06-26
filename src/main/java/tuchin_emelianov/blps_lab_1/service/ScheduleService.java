package tuchin_emelianov.blps_lab_1.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import tuchin_emelianov.blps_lab_1.jpa.entity.Delivery;
import tuchin_emelianov.blps_lab_1.jpa.entity.Orders;
import tuchin_emelianov.blps_lab_1.jpa.entity.Pickup;
import tuchin_emelianov.blps_lab_1.request.MessageDTO;

import java.util.List;

@Service
@AllArgsConstructor
public class ScheduleService {
    private final OrderService orderService;
    private final PickupService pickupService;
    private final DeliveryService deliveryService;
    private final JmsTemplate rabbitMQProducer;

    private static final Logger LOGGER = LoggerFactory.getLogger(ScheduleService.class);

    @Scheduled(cron = "0 0 8 * * 1,2,3,4,5", zone = "Europe/Moscow")
    public void checkOrderStatus(){
        LOGGER.info("Checking delayed orders");
        List<Orders> orders = orderService.getDelayedOrders();
        orders.forEach(order -> {
            try {
                rabbitMQProducer.convertAndSend(
                        "messages",
                        new ObjectMapper().writeValueAsString(
                                new MessageDTO(
                                        "Возьмите заказ в работу",
                                        "Order"+order.getId(),
                                        true,
                                        List.of("Работник")
                                )
                        )
                );
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
        });
    }
    
    @Scheduled(cron = "0 0 8 * * 1,2,3,4,5", zone = "Europe/Moscow")
    public void checkPickupStatus(){
        LOGGER.info("Checking delayed pickups");
        List<Pickup> pickups = pickupService.getDelayedPickups();
        pickups.forEach(pickup -> {
            try {
                rabbitMQProducer.convertAndSend(
                        "messages",
                        new ObjectMapper().writeValueAsString(
                                new MessageDTO(
                                        "Возьмите заказ в работу",
                                        "Pickup"+pickup.getId(),
                                        true,
                                        List.of("Работник")
                                )
                        )
                );
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
        });
    }
    
    @Scheduled(cron = "0 0 8 * * 1,2,3,4,5", zone = "Europe/Moscow")
    public void checkDeliveryStatus(){
        LOGGER.info("Checking delayed deliveries");
        List<Delivery> deliveries = deliveryService.getDelayedDeliveries();
        deliveries.forEach(delivery -> {
            try {
                rabbitMQProducer.convertAndSend(
                        "messages",
                        new ObjectMapper().writeValueAsString(
                                new MessageDTO(
                                        "Возьмите заказ в работу",
                                        "Delivery"+delivery.getId(),
                                        true,
                                        List.of("Курьер")
                                )
                        )
                );
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
        });
    }


}

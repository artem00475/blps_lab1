package tuchin_emelianov.blps_lab_1.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import tuchin_emelianov.blps_lab_1.dto.DeliveryDTO;
import tuchin_emelianov.blps_lab_1.exceptions.BlankFieldException;
import tuchin_emelianov.blps_lab_1.exceptions.EntityNotFoundException;
import tuchin_emelianov.blps_lab_1.jpa.entity.Delivery;
import tuchin_emelianov.blps_lab_1.jpa.entity.DeliveryStatus;
import tuchin_emelianov.blps_lab_1.jpa.entity.Human;
import tuchin_emelianov.blps_lab_1.jpa.entity.Orders;
import tuchin_emelianov.blps_lab_1.jpa.repository.DeliveryRepository;
import tuchin_emelianov.blps_lab_1.jpa.repository.DeliveryStatusRepository;
import tuchin_emelianov.blps_lab_1.jpa.repository.OrderRepository;
import tuchin_emelianov.blps_lab_1.jpa.repository.OrderStatusRepository;
import tuchin_emelianov.blps_lab_1.request.MessageDTO;

import java.util.Date;
import java.util.List;

@Service
public class DeliveryService {

    private final DeliveryStatusRepository deliveryStatusRepository;
    private final DeliveryRepository deliveryRepository;
    private final ModelMapper modelMapper;
    private final OrderRepository orderRepository;
    private final OrderStatusRepository orderStatusRepository;
    private final TransactionTemplate transactionTemplate;
    private final JmsTemplate rabbitMQProducer;

    public DeliveryService(DeliveryStatusRepository deliveryStatusRepository, DeliveryRepository deliveryRepository, ModelMapper modelMapper, OrderRepository orderRepository, OrderStatusRepository orderStatusRepository, PlatformTransactionManager platformTransactionManager, JmsTemplate jmsTemplate) {
        this.deliveryStatusRepository = deliveryStatusRepository;
        this.deliveryRepository = deliveryRepository;
        this.modelMapper = modelMapper;
        this.orderRepository = orderRepository;
        this.orderStatusRepository = orderStatusRepository;
        this.transactionTemplate = new TransactionTemplate(platformTransactionManager);
        this.rabbitMQProducer = jmsTemplate;
    }


    public DeliveryDTO getDeliveryDTO(Long id) {
        Delivery delivery = deliveryRepository.findDeliveryById(id);
        if (delivery == null) throw new EntityNotFoundException("Доставка с id=%s не найдена".formatted(id));
        return modelMapper.map(delivery, DeliveryDTO.class);
    }

    public Page<DeliveryDTO> getDeliveriesDTO(Pageable pageable) {
        Page<Delivery> deliveryPage = deliveryRepository.findAll(pageable);
        return deliveryPage.map(delivery -> modelMapper.map(delivery, DeliveryDTO.class));
    }

//    public Page<Delivery> getDeliveries(Pageable pageable) {
//        return deliveryRepository.findAll(pageable);
//    }

//    public Delivery getDelivery(Long id) {
//        return deliveryRepository.findDeliveryById(id);
//    }

    public ResultMessage takeOrder (Orders order, Human courier){
        if (!deliveryRepository.existsByOrder(order)) {
            return new ResultMessage(0, "Заказ не найден в списке на доставку");
        }
        transactionTemplate.setIsolationLevelName("ISOLATION_REPEATABLE_READ");
        return transactionTemplate.execute(status -> {
            Delivery delivery = deliveryRepository.findDeliveryByOrder(order);
            DeliveryStatus deliveryStatus = delivery.getStatus();
            String deliveryStatusType = deliveryStatus.getType();
            boolean isReadyForDelivery = deliveryStatusType.equals("Ожидает обработки");
            if (isReadyForDelivery) {
                delivery.setStatus(deliveryStatusRepository.findByType("В работе"));
                delivery.setCourier(courier);
                deliveryRepository.save(delivery);
                return new ResultMessage(order.getId(), "Заказ доставляется!");
            } else {
                return new ResultMessage(0, "Недопустимый статус доставки заказа (чтобы его забрал курьер): " + deliveryStatusType);
            }
        });
    }

    public ResultMessage deliverOrder (Orders order, Human courier){
        if (!deliveryRepository.existsByOrder(order)) {
            return new ResultMessage(0, "Заказ не найден в списке на доставку");
        }
        transactionTemplate.setIsolationLevelName("ISOLATION_READ_COMMITTED");
        return transactionTemplate.execute(status -> {
            Delivery delivery = deliveryRepository.findDeliveryByOrder(order);
            DeliveryStatus deliveryStatus = delivery.getStatus();
            String deliveryStatusType = deliveryStatus.getType();
            boolean isDelivering = deliveryStatusType.equals("В работе");
            if (isDelivering) {
                if (courier.getId().equals(delivery.getCourier().getId())) {
                    delivery.setStatus(deliveryStatusRepository.findByType("Доставлено"));
                    delivery.setDate(new Date());
                    deliveryRepository.save(delivery);
                    try {
                        rabbitMQProducer.convertAndSend(
                                "messages",
                                new ObjectMapper().writeValueAsString(
                                        new MessageDTO(
                                                "Заказ доставлен",
                                                "Order"+order.getId(),
                                                false,
                                                List.of(order.getClient().getUser().getUsername())
                                        )
                                )
                        );
                    } catch (JsonProcessingException e) {
                        e.printStackTrace();
                    }
                    return new ResultMessage(order.getId(), "Заказ доставлен!");
                } else {
                    return new ResultMessage(0, "Заказ доставляется другим курьером");
                }
            } else {
                return new ResultMessage(0, "Недопустимый статус доставки заказа (чтобы его доставил курьер): " + deliveryStatusType);

            }
        });
    }

    public ResultMessage getOrder(Orders order, Human client){
        if (!deliveryRepository.existsByOrder(order)) {
            return new ResultMessage(0, "Заказ не найден в списке на доставку");
        }
        transactionTemplate.setIsolationLevelName("ISOLATION_READ_COMMITTED");
        return transactionTemplate.execute(status -> {
            Delivery delivery = deliveryRepository.findDeliveryByOrder(order);
            DeliveryStatus deliveryStatus = delivery.getStatus();
            String deliveryStatusType = deliveryStatus.getType();
            boolean isDelivered = deliveryStatusType.equals("Доставлено");
            if (isDelivered) {
                if (!order.getClient().equals(client)) {
                    return new ResultMessage(0, "Заказ был создан другим клиентом.");
                }
                if (order.getPaymentType().getType().equals("При получении") && !order.getOrderStatus().getType().equals("Оплачен курьеру")) {
                    return new ResultMessage(0, "Сначала необходимо оплатить заказ");
                }
                if (order.getPaymentType().getType().equals("Онлайн") && !order.getOrderStatus().getType().equals("Передан в доставку")) {
                    return new ResultMessage(0, "Недопустимый статус заказа: " + order.getOrderStatus().getType());
                }
                delivery.setStatus(deliveryStatusRepository.findByType("Получено"));
                deliveryRepository.save(delivery);
                order.setOrderStatus(orderStatusRepository.findByType("Завершен"));
                order.setLastStatusDate(new Date());
                orderRepository.save(order);
                return new ResultMessage(order.getId(), "Заказ получен!");
            } else {
                return new ResultMessage(0, "Недопустимый статус доставки заказа (чтобы его получил клиент): " + deliveryStatusType);
            }
        });
    }

    public void addOrder(Orders order, String address) {
        if (address == null || address.trim().isEmpty())
            throw new BlankFieldException("При доставке необходимо указать адрес");
        Delivery delivery = new Delivery();
        delivery.setOrder(order);
        delivery.setAddress(address);
        delivery.setStatus(deliveryStatusRepository.findByType("Новый"));
        deliveryRepository.save(delivery);
    }

    public void updateOrder(Orders order) {
        Delivery delivery = deliveryRepository.findDeliveryByOrder(order);
        delivery.setStatus(deliveryStatusRepository.findByType("Ожидает обработки"));
        deliveryRepository.save(delivery);
    }
    
    public List<Delivery> getDelayedDeliveries() {
        return deliveryRepository.findAllByDateLessThanAndStatus(new Date(), deliveryStatusRepository.findByType("Ожидает обработки"));
    }

}

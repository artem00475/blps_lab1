package tuchin_emelianov.blps_lab_1.service;

import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import tuchin_emelianov.blps_lab_1.dto.DeliveryDTO;
import tuchin_emelianov.blps_lab_1.exceptions.EntityNotFoundException;
import tuchin_emelianov.blps_lab_1.jpa.entity.*;
import tuchin_emelianov.blps_lab_1.jpa.repository.*;
import java.util.Date;

@Service
@AllArgsConstructor
public class DeliveryService {

    private DeliveryStatusRepository deliveryStatusRepository;

    private DeliveryRepository deliveryRepository;

    private ModelMapper modelMapper;

    public DeliveryDTO getDeliveryDTO(Long id) {
        Delivery delivery = deliveryRepository.findDeliveryById(id);
        if (delivery == null) throw new EntityNotFoundException("Доставка с id=%s не найдена".formatted(id));
        return modelMapper.map(delivery, DeliveryDTO.class);
    }

    public Page<DeliveryDTO> getDeliveriesDTO(Pageable pageable) {
        Page<Delivery> deliveryPage = deliveryRepository.findAll(pageable);
        return deliveryPage.map(delivery -> modelMapper.map(delivery, DeliveryDTO.class));
    }

    public Page<Delivery> getDeliveries(Pageable pageable) {
        return deliveryRepository.findAll(pageable);
    }

    public Delivery getDelivery(Long id) {
        return deliveryRepository.findDeliveryById(id);
    }

    public ResultMessage takeOrder (Orders order, Human courier){
        if (!deliveryRepository.existsByOrder(order)) {
            return new ResultMessage(0, "Заказ не найден в списке на доставку");
        }
        Delivery delivery = deliveryRepository.findDeliveryByOrder(order);
        DeliveryStatus deliveryStatus = delivery.getStatus();
        String deliveryStatusType = deliveryStatus.getType();
        boolean isReadyForDelivery = deliveryStatusType.equals("Ожидает обработки");
        if (isReadyForDelivery){
            delivery.setStatus(deliveryStatusRepository.findByType("В работе"));
            delivery.setCourier(courier);
            deliveryRepository.save(delivery);
            return new ResultMessage(order.getId(), "Заказ доставляется!");
        }else{
            return new ResultMessage(0, "Недопустимый статус доставки заказа (чтобы его забрал курьер): " + deliveryStatusType);
        }
    }

    public ResultMessage deliverOrder (Orders order, Human courier){
        if (!deliveryRepository.existsByOrder(order)) {
            return new ResultMessage(0, "Заказ не найден в списке на доставку");
        }
        Delivery delivery = deliveryRepository.findDeliveryByOrder(order);
        DeliveryStatus deliveryStatus = delivery.getStatus();
        String deliveryStatusType = deliveryStatus.getType();
        boolean isDelivering = deliveryStatusType.equals("В работе");
        if(isDelivering){
            if(courier.getId().equals(delivery.getCourier().getId())){
                delivery.setStatus(deliveryStatusRepository.findByType("Доставлено"));
                delivery.setDate(new Date());
                deliveryRepository.save(delivery);
                return new ResultMessage(order.getId(), "Заказ доставлен!");
            }else{
                return new ResultMessage(0, "Заказ доставляется другим курьером");
            }
        }else{
            return new ResultMessage(0, "Недопустимый статус доставки заказа (чтобы его доставил курьер): " + deliveryStatusType);

        }
    }

    public ResultMessage getOrder(Orders order, Human client){
        if (!deliveryRepository.existsByOrder(order)) {
            return new ResultMessage(0, "Заказ не найден в списке на доставку");
        }
        Delivery delivery = deliveryRepository.findDeliveryByOrder(order);
        DeliveryStatus deliveryStatus = delivery.getStatus();
        String deliveryStatusType = deliveryStatus.getType();
        boolean isDelivered = deliveryStatusType.equals("Доставлено");
        if(isDelivered){
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
            return new ResultMessage(order.getId(), "Заказ получен!");
        }else{
            return new ResultMessage(0, "Недопустимый статус доставки заказа (чтобы его получил клиент): " + deliveryStatusType);
        }
    }

    public void addOrder(Orders order, String address) {
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

}

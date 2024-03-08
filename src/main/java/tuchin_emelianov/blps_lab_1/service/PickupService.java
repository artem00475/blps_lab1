package tuchin_emelianov.blps_lab_1.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tuchin_emelianov.blps_lab_1.jpa.entity.*;
import tuchin_emelianov.blps_lab_1.jpa.repository.*;

import java.util.Date;

@Service
public class PickupService {

    @Autowired
    private PickupRepository pickupRepository;

    @Autowired
    private ReceiveStatusRepository receiveStatusRepository;

    public ResultMessage giveOrder(Orders order, Human worker){
        if (!pickupRepository.existsByOrder(order)) {
            return new ResultMessage(0, "Заказ не найден в списке на выдачу");
        }
        Pickup pickup = pickupRepository.findPickupByOrder(order);
        if (pickup.getReceiveStatus().getType().equals("Ожидает обработки")) {
            pickup.setReceiveStatus(receiveStatusRepository.findByType("Выдан"));
            pickup.setWorker(worker);
            pickup.setDate(new Date());
            pickupRepository.save(pickup);
            return new ResultMessage(order.getId(), "Заказ выдан!");
        }else{
            return new ResultMessage(0, "Некорректный статус выдачи заказа: " + pickup.getReceiveStatus().getType());
        }
    }

    public ResultMessage getOrder(Orders order, Human client){
        if (!pickupRepository.existsByOrder(order)) {
            return new ResultMessage(0, "Заказ не найден в списке на выдачу");
        }
        Pickup pickup = pickupRepository.findPickupByOrder(order);
        if(pickup.getReceiveStatus().getType().equals("Выдан")){
            if (!order.getClient().equals(client)) {
                return new ResultMessage(0, "Заказ был создан другим клиентом.");
            }
            if (order.getReceiveType().getType().equals("Доставка") && !order.getOrderStatus().getType().equals("Готов к выдаче")) {
                return new ResultMessage(0, "Некорректный статус заказа: " + order.getOrderStatus().getType());
            }
            pickup.setReceiveStatus(receiveStatusRepository.findByType("Получено"));
            pickupRepository.save(pickup);
            return new ResultMessage(order.getId(), "Заказ получен!");
        }else{
            return new ResultMessage(0, "Некорректный статус выдачи заказа: " + pickup.getReceiveStatus().getType());
        }
    }

}

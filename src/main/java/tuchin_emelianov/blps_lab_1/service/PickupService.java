package tuchin_emelianov.blps_lab_1.service;

import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import tuchin_emelianov.blps_lab_1.dto.PickupDTO;
import tuchin_emelianov.blps_lab_1.exceptions.EntityNotFoundException;
import tuchin_emelianov.blps_lab_1.jpa.entity.*;
import tuchin_emelianov.blps_lab_1.jpa.repository.*;

import java.util.Date;

@Service
@AllArgsConstructor
public class PickupService {

    private PickupRepository pickupRepository;

    private ReceiveStatusRepository receiveStatusRepository;

    private ModelMapper modelMapper;

    public PickupDTO getPickupDTO(Long id) {
        Pickup pickup = pickupRepository.findPickupById(id);
        if (pickup == null) throw new EntityNotFoundException("Самовывоз с id=%s не найден".formatted(id));
        return modelMapper.map(pickup, PickupDTO.class);
    }

    public Page<PickupDTO> getPickupsDTO(Pageable pageable) {
        Page<Pickup> pickupPage = pickupRepository.findAll(pageable);
        return pickupPage.map(pickup -> modelMapper.map(pickup, PickupDTO.class));
    }

    public Page<Pickup> getPickups(Pageable pageable) {
        return pickupRepository.findAll(pageable);
    }

    public Pickup getPickup(Long id) {
        return pickupRepository.findPickupById(id);
    }

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

    public void addOrder(Orders order) {
        Pickup pickup = new Pickup();
        pickup.setOrder(order);
        pickup.setReceiveStatus(receiveStatusRepository.findByType("Новый"));
        pickupRepository.save(pickup);
    }

    public void updateOrder(Orders order) {
        Pickup pickup = pickupRepository.findPickupByOrder(order);
        pickup.setReceiveStatus(receiveStatusRepository.findByType("Ожидает обработки"));
        pickupRepository.save(pickup);
    }

}

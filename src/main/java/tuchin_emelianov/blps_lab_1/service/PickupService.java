package tuchin_emelianov.blps_lab_1.service;

import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import tuchin_emelianov.blps_lab_1.dto.PickupDTO;
import tuchin_emelianov.blps_lab_1.exceptions.EntityNotFoundException;
import tuchin_emelianov.blps_lab_1.jpa.entity.Human;
import tuchin_emelianov.blps_lab_1.jpa.entity.Orders;
import tuchin_emelianov.blps_lab_1.jpa.entity.Pickup;
import tuchin_emelianov.blps_lab_1.jpa.repository.OrderRepository;
import tuchin_emelianov.blps_lab_1.jpa.repository.OrderStatusRepository;
import tuchin_emelianov.blps_lab_1.jpa.repository.PickupRepository;
import tuchin_emelianov.blps_lab_1.jpa.repository.ReceiveStatusRepository;

import java.util.Date;

@Service
public class PickupService {

    private final PickupRepository pickupRepository;
    private final ReceiveStatusRepository receiveStatusRepository;
    private final ModelMapper modelMapper;
    private final TransactionTemplate transactionTemplate;
    private final OrderRepository orderRepository;
    private final OrderStatusRepository orderStatusRepository;

    public PickupService(PickupRepository pickupRepository, ReceiveStatusRepository receiveStatusRepository, ModelMapper modelMapper, PlatformTransactionManager platformTransactionManager, OrderRepository orderRepository, OrderStatusRepository orderStatusRepository) {
        this.pickupRepository = pickupRepository;
        this.receiveStatusRepository = receiveStatusRepository;
        this.modelMapper = modelMapper;
        this.transactionTemplate = new TransactionTemplate(platformTransactionManager);
        this.orderRepository = orderRepository;
        this.orderStatusRepository = orderStatusRepository;
    }


    public PickupDTO getPickupDTO(Long id) {
        Pickup pickup = pickupRepository.findPickupById(id);
        if (pickup == null) throw new EntityNotFoundException("Самовывоз с id=%s не найден".formatted(id));
        return modelMapper.map(pickup, PickupDTO.class);
    }

    public Page<PickupDTO> getPickupsDTO(Pageable pageable) {
        Page<Pickup> pickupPage = pickupRepository.findAll(pageable);
        return pickupPage.map(pickup -> modelMapper.map(pickup, PickupDTO.class));
    }

//    public Page<Pickup> getPickups(Pageable pageable) {
//        return pickupRepository.findAll(pageable);
//    }
//
//    public Pickup getPickup(Long id) {
//        return pickupRepository.findPickupById(id);
//    }

    public ResultMessage giveOrder(Orders order, Human worker){
        if (!pickupRepository.existsByOrder(order)) {
            return new ResultMessage(0, "Заказ не найден в списке на выдачу");
        }
        transactionTemplate.setIsolationLevelName("ISOLATION_REPEATABLE_READ");
        return transactionTemplate.execute(status -> {
            Pickup pickup = pickupRepository.findPickupByOrder(order);
            if (pickup.getReceiveStatus().getType().equals("Ожидает обработки")) {
                pickup.setReceiveStatus(receiveStatusRepository.findByType("Выдан"));
                pickup.setWorker(worker);
                pickup.setDate(new Date());
                pickupRepository.save(pickup);
                return new ResultMessage(order.getId(), "Заказ выдан!");
            } else {
                return new ResultMessage(0, "Некорректный статус выдачи заказа: " + pickup.getReceiveStatus().getType());
            }
        });
    }

    public ResultMessage getOrder(Orders order, Human client){
        if (!pickupRepository.existsByOrder(order)) {
            return new ResultMessage(0, "Заказ не найден в списке на выдачу");
        }
        transactionTemplate.setIsolationLevelName("ISOLATION_READ_COMMITTED");
        return transactionTemplate.execute(status -> {
            Pickup pickup = pickupRepository.findPickupByOrder(order);
            if (pickup.getReceiveStatus().getType().equals("Выдан")) {
                if (!order.getClient().equals(client)) {
                    return new ResultMessage(0, "Заказ был создан другим клиентом.");
                }
                if (order.getReceiveType().getType().equals("Доставка") && !order.getOrderStatus().getType().equals("Готов к выдаче")) {
                    return new ResultMessage(0, "Некорректный статус заказа: " + order.getOrderStatus().getType());
                }
                pickup.setReceiveStatus(receiveStatusRepository.findByType("Получено"));
                pickupRepository.save(pickup);
                order.setOrderStatus(orderStatusRepository.findByType("Завершен"));
                order.setLastStatusDate(new Date());
                orderRepository.save(order);
                return new ResultMessage(order.getId(), "Заказ получен!");
            } else {
                return new ResultMessage(0, "Некорректный статус выдачи заказа: " + pickup.getReceiveStatus().getType());
            }
        });
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

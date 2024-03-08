package tuchin_emelianov.blps_lab_1.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tuchin_emelianov.blps_lab_1.jpa.entity.Orders;
import tuchin_emelianov.blps_lab_1.request.AddRequest;
import tuchin_emelianov.blps_lab_1.request.SetPaymentTypeRequest;
import tuchin_emelianov.blps_lab_1.request.SetReceiveTypeRequest;
import tuchin_emelianov.blps_lab_1.service.*;

@RestController
public class OrderController {
    @Autowired
    private HumanService humanService;
    @Autowired
    private OrderService orderService;
    @Autowired
    private PickupService pickupService;
    @Autowired
    private DeliveryService deliveryService;

    @PostMapping("/add")
    public ResponseEntity<ResultMessage> addOrder(@RequestBody AddRequest addRequest) {
        long userId;
        if (addRequest.getUserId() == null || addRequest.getUserId() < 1) {
            if (addRequest.getFio() != null && addRequest.getMail() != null && addRequest.getPhone() != null) {
                userId = humanService.addUser(addRequest.getFio(), addRequest.getMail(), addRequest.getPhone());
                if (userId < 1)
                    return ResponseEntity.status(406).body(new ResultMessage(0, "Ошибка при создании пользователя"));
            } else {
                return ResponseEntity.status(406).body(new ResultMessage(0,"Некорректные данные о пользователе"));
            }
        } else {
            if (!humanService.checkUser(addRequest.getUserId())) {
                userId = addRequest.getUserId();
            } else {
                return ResponseEntity.status(406).body(new ResultMessage(0,"пользователя не существует"));
            }
        }
        if (addRequest.getProducts() == null || addRequest.getProducts().size() == 0) {
            return ResponseEntity.status(406).body(new ResultMessage(0,"Необходимо указать товары"));
        }
        ResultMessage resultMessage = orderService.addOrder(humanService.getUser(userId), addRequest.getProducts());
        if (resultMessage.getId() < 1) {
            return ResponseEntity.status(406).body(resultMessage);
        }
        return ResponseEntity.ok(resultMessage);
    }

    @PostMapping("/set_receive_type")
    public ResponseEntity<ResultMessage> setReceiveType (@RequestBody SetReceiveTypeRequest receiveTypeRequest){
        if (receiveTypeRequest.getType().equals("Самовывоз") || (receiveTypeRequest.getType().equals("Доставка") && receiveTypeRequest.getAddress() != null)){
            if (orderService.checkOrder(receiveTypeRequest.getId())) {
                return ResponseEntity.status(406).body(new ResultMessage(0,"Заказ не найден."));
            }
            ResultMessage resultMessage = orderService.setReceiveType(receiveTypeRequest.getId(), receiveTypeRequest.getType());
            if (resultMessage.getId() > 0) {
                if (receiveTypeRequest.getType().equals("Самовывоз")) {
                    pickupService.addOrder(orderService.getOrder(receiveTypeRequest.getId()));
                } else {
                    deliveryService.addOrder(orderService.getOrder(receiveTypeRequest.getId()), receiveTypeRequest.getAddress());
                }
                return ResponseEntity.ok(resultMessage);
            } else {
                return ResponseEntity.status(406).body(resultMessage);
            }
        }
        else{
            return ResponseEntity.status(406).body(new ResultMessage(0,"Некорректный способ получения."));
        }
    }

    @PostMapping("/set_payment_type")
    public ResponseEntity<ResultMessage> setPaymentType (@RequestBody SetPaymentTypeRequest paymentTypeRequest){
        if (paymentTypeRequest.getType().equals("Онлайн") || paymentTypeRequest.getType().equals("При получении")){
            if (orderService.checkOrder(paymentTypeRequest.getId())) {
                return ResponseEntity.status(406).body(new ResultMessage(0,"Заказ не найден."));
            }
            ResultMessage resultMessage = orderService.setPaymentType(paymentTypeRequest.getId(), paymentTypeRequest.getType());
            if (resultMessage.getId() > 0) {
                return ResponseEntity.ok(resultMessage);
            } else {
                return ResponseEntity.status(406).body(resultMessage);
            }
        } else{
            return ResponseEntity.status(406).body(new ResultMessage(0,"Некорректный способ оплаты."));
        }
    }

    @GetMapping("/pay")
    public ResponseEntity<ResultMessage> pay(@RequestParam Long id) {
        if (id <= 0) {
            return ResponseEntity.status(406).body(new ResultMessage(0,"Некорректный номер заказа."));
        }
        if (orderService.checkOrder(id)) {
            return ResponseEntity.status(406).body(new ResultMessage(0,"Заказ не найден."));
        }
        ResultMessage resultMessage = orderService.payOnline(id);
        if (resultMessage.getId() == 0) {
            return ResponseEntity.status(406).body(resultMessage);
        } else {
            return ResponseEntity.ok(resultMessage);
        }
    }

    @GetMapping("/work")
    public ResponseEntity<ResultMessage> work(@RequestParam Long id, @RequestParam Long user) {
        if (id <= 0) {
            return ResponseEntity.status(406).body(new ResultMessage(0,"Некорректный номер заказа."));
        }
        if (user <= 0) {
            return ResponseEntity.status(406).body(new ResultMessage(0,"Некорректный пользователь."));
        }
        if (humanService.checkWorker(user)) {
            return ResponseEntity.status(406).body(new ResultMessage(0,"Работник не найден."));
        }
        if (orderService.checkOrder(id)) {
            return ResponseEntity.status(406).body(new ResultMessage(0,"Заказ не найден."));
        }
        ResultMessage resultMessage = orderService.work(id, humanService.getUser(user));
        if (resultMessage.getId() > 0) {
            return ResponseEntity.ok(resultMessage);
        } else {
            return ResponseEntity.status(406).body(resultMessage);
        }
    }

    @GetMapping("/done")
    public ResponseEntity<ResultMessage> done(@RequestParam Long id, @RequestParam Long user) {
        if (id <= 0) {
            return ResponseEntity.status(406).body(new ResultMessage(0,"Некорректный номер заказа."));
        }
        if (user <= 0) {
            return ResponseEntity.status(406).body(new ResultMessage(0,"Некорректный пользователь."));
        }
        if (humanService.checkWorker(user)) {
            return ResponseEntity.status(406).body(new ResultMessage(0,"Работник не найден."));
        }
        if (orderService.checkOrder(id)) {
            return ResponseEntity.status(406).body(new ResultMessage(0,"Заказ не найден."));
        }
        ResultMessage resultMessage = orderService.done(id, humanService.getUser(user));
        if (resultMessage.getId() > 0) {
            Orders order = orderService.getOrder(id);
            if (order.getReceiveType().getType().equals("Самовывоз")) {
                pickupService.updateOrder(order);
            } else {
                deliveryService.updateOrder(order);
            }
            return ResponseEntity.ok(resultMessage);
        } else {
            return ResponseEntity.status(406).body(resultMessage);
        }
    }
}

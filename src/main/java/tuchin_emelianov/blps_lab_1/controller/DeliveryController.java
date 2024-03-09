package tuchin_emelianov.blps_lab_1.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tuchin_emelianov.blps_lab_1.jpa.entity.Delivery;
import tuchin_emelianov.blps_lab_1.jpa.entity.Orders;
import tuchin_emelianov.blps_lab_1.jpa.entity.Pickup;
import tuchin_emelianov.blps_lab_1.request.UserRequest;
import tuchin_emelianov.blps_lab_1.service.DeliveryService;
import tuchin_emelianov.blps_lab_1.service.HumanService;
import tuchin_emelianov.blps_lab_1.service.OrderService;
import tuchin_emelianov.blps_lab_1.service.ResultMessage;

@RestController
public class DeliveryController {

    @Autowired
    private HumanService humanService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private DeliveryService deliveryService;

    @GetMapping("/delivery")
    public ResponseEntity<Page<Delivery>> getDeliveries(@PageableDefault(sort = {"id"}, direction = Sort.Direction.ASC) Pageable pageable) {
        return ResponseEntity.ok(deliveryService.getDeliveries(pageable));
    }

    @GetMapping("/delivery/{id}")
    public ResponseEntity<Delivery> getDelivery(@PathVariable Long id) {
        return ResponseEntity.ok(deliveryService.getDelivery(id));
    }

    @PostMapping("/delivery")
    public ResponseEntity<ResultMessage> work(@RequestBody UserRequest userRequest) {
        if (userRequest.getId() <= 0) {
            return ResponseEntity.badRequest().body(new ResultMessage(0,"Некорректный номер заказа."));
        }
        if (userRequest.getUser() <= 0) {
            return ResponseEntity.badRequest().body(new ResultMessage(0,"Некорректный пользователь."));
        }
        if (humanService.checkCourier(userRequest.getUser())) {
            return ResponseEntity.badRequest().body(new ResultMessage(0,"Курьер не найден."));
        }
        if (orderService.checkOrder(userRequest.getId())) {
            return ResponseEntity.badRequest().body(new ResultMessage(0,"Заказ не найден."));
        }
        ResultMessage resultMessage = deliveryService.takeOrder(orderService.getOrder(userRequest.getId()), humanService.getUser(userRequest.getUser()));
        if (resultMessage.getId() > 0) {
            return ResponseEntity.ok(resultMessage);
        } else {
            return ResponseEntity.badRequest().body(resultMessage);
        }
    }

    @PutMapping("/delivery")
    public ResponseEntity<ResultMessage> done(@RequestBody UserRequest userRequest) {
        if (userRequest.getId() <= 0) {
            return ResponseEntity.badRequest().body(new ResultMessage(0,"Некорректный номер заказа."));
        }
        if (userRequest.getUser() <= 0) {
            return ResponseEntity.badRequest().body(new ResultMessage(0,"Некорректный пользователь."));
        }
        if (humanService.checkCourier(userRequest.getUser())) {
            return ResponseEntity.badRequest().body(new ResultMessage(0,"Курьер не найден."));
        }
        if (orderService.checkOrder(userRequest.getId())) {
            return ResponseEntity.badRequest().body(new ResultMessage(0,"Заказ не найден."));
        }
        ResultMessage resultMessage = deliveryService.deliverOrder(orderService.getOrder(userRequest.getId()), humanService.getUser(userRequest.getUser()));
        if (resultMessage.getId() > 0) {
            return ResponseEntity.ok(resultMessage);
        } else {
            return ResponseEntity.badRequest().body(resultMessage);
        }
    }

    @PostMapping("/delivery/payment")
    public ResponseEntity<ResultMessage> pay(@RequestBody UserRequest userRequest) {
        if (userRequest.getId() <= 0) {
            return ResponseEntity.badRequest().body(new ResultMessage(0,"Некорректный номер заказа."));
        }
        if (orderService.checkOrder(userRequest.getId())) {
            return ResponseEntity.badRequest().body(new ResultMessage(0,"Заказ не найден."));
        }
        ResultMessage resultMessage = orderService.payDelivery(orderService.getOrder(userRequest.getId()));
        if (resultMessage.getId() == 0) {
            return ResponseEntity.badRequest().body(resultMessage);
        } else {
            return ResponseEntity.ok(resultMessage);
        }
    }

    @PostMapping("/delivery/receiving")
    public ResponseEntity<ResultMessage> get(@RequestBody UserRequest userRequest) {
        if (userRequest.getId() <= 0) {
            return ResponseEntity.badRequest().body(new ResultMessage(0,"Некорректный номер заказа."));
        }
        if (orderService.checkOrder(userRequest.getId())) {
            return ResponseEntity.badRequest().body(new ResultMessage(0,"Заказ не найден."));
        }
        if (userRequest.getUser() <= 0) {
            return ResponseEntity.badRequest().body(new ResultMessage(0,"Некорректный пользователь."));
        }
        if (humanService.checkUser(userRequest.getUser())) {
            return ResponseEntity.badRequest().body(new ResultMessage(0,"Пользователь не найден."));
        }
        ResultMessage resultMessage = deliveryService.getOrder(orderService.getOrder(userRequest.getId()), humanService.getUser(userRequest.getUser()));
        if (resultMessage.getId() == 0) {
            return ResponseEntity.badRequest().body(resultMessage);
        } else {
            orderService.closeOrder(userRequest.getId());
            return ResponseEntity.ok(resultMessage);
        }
    }
}

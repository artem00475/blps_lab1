package tuchin_emelianov.blps_lab_1.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
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

    @GetMapping("/delivery/work")
    public ResponseEntity<ResultMessage> work(@RequestParam Long id, @RequestParam Long user) {
        if (id <= 0) {
            return ResponseEntity.badRequest().body(new ResultMessage(0,"Некорректный номер заказа."));
        }
        if (user <= 0) {
            return ResponseEntity.badRequest().body(new ResultMessage(0,"Некорректный пользователь."));
        }
        if (humanService.checkCourier(user)) {
            return ResponseEntity.badRequest().body(new ResultMessage(0,"Курьер не найден."));
        }
        if (orderService.checkOrder(id)) {
            return ResponseEntity.badRequest().body(new ResultMessage(0,"Заказ не найден."));
        }
        ResultMessage resultMessage = deliveryService.takeOrder(orderService.getOrder(id), humanService.getUser(user));
        if (resultMessage.getId() > 0) {
            return ResponseEntity.ok(resultMessage);
        } else {
            return ResponseEntity.badRequest().body(resultMessage);
        }
    }

    @GetMapping("/delivery/done")
    public ResponseEntity<ResultMessage> done(@RequestParam Long id, @RequestParam Long user) {
        if (id <= 0) {
            return ResponseEntity.badRequest().body(new ResultMessage(0,"Некорректный номер заказа."));
        }
        if (user <= 0) {
            return ResponseEntity.badRequest().body(new ResultMessage(0,"Некорректный пользователь."));
        }
        if (humanService.checkCourier(user)) {
            return ResponseEntity.badRequest().body(new ResultMessage(0,"Курьер не найден."));
        }
        if (orderService.checkOrder(id)) {
            return ResponseEntity.badRequest().body(new ResultMessage(0,"Заказ не найден."));
        }
        ResultMessage resultMessage = deliveryService.deliverOrder(orderService.getOrder(id), humanService.getUser(user));
        if (resultMessage.getId() > 0) {
            return ResponseEntity.ok(resultMessage);
        } else {
            return ResponseEntity.badRequest().body(resultMessage);
        }
    }

    @GetMapping("/delivery/pay")
    public ResponseEntity<ResultMessage> pay(@RequestParam Long id) {
        if (id <= 0) {
            return ResponseEntity.badRequest().body(new ResultMessage(0,"Некорректный номер заказа."));
        }
        if (orderService.checkOrder(id)) {
            return ResponseEntity.badRequest().body(new ResultMessage(0,"Заказ не найден."));
        }
        ResultMessage resultMessage = orderService.payDelivery(orderService.getOrder(id));
        if (resultMessage.getId() == 0) {
            return ResponseEntity.badRequest().body(resultMessage);
        } else {
            return ResponseEntity.ok(resultMessage);
        }
    }

    @GetMapping("/delivery/get")
    public ResponseEntity<ResultMessage> get(@RequestParam Long id, @RequestParam Long user) {
        if (id <= 0) {
            return ResponseEntity.badRequest().body(new ResultMessage(0,"Некорректный номер заказа."));
        }
        if (orderService.checkOrder(id)) {
            return ResponseEntity.badRequest().body(new ResultMessage(0,"Заказ не найден."));
        }
        if (user <= 0) {
            return ResponseEntity.badRequest().body(new ResultMessage(0,"Некорректный пользователь."));
        }
        if (humanService.checkUser(user)) {
            return ResponseEntity.badRequest().body(new ResultMessage(0,"Пользователь не найден."));
        }
        ResultMessage resultMessage = deliveryService.getOrder(orderService.getOrder(id), humanService.getUser(user));
        if (resultMessage.getId() == 0) {
            return ResponseEntity.badRequest().body(resultMessage);
        } else {
            orderService.closeOrder(id);
            return ResponseEntity.ok(resultMessage);
        }
    }
}

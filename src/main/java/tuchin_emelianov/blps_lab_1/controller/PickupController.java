package tuchin_emelianov.blps_lab_1.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import tuchin_emelianov.blps_lab_1.service.*;

@RestController
public class PickupController {
    @Autowired
    private HumanService humanService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private PickupService pickupService;

    @GetMapping("/pickup/work")
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
        ResultMessage resultMessage = pickupService.giveOrder(orderService.getOrder(id), humanService.getUser(user));
        if (resultMessage.getId() > 0) {
            return ResponseEntity.ok(resultMessage);
        } else {
            return ResponseEntity.status(406).body(resultMessage);
        }
    }

    @GetMapping("/pickup/get")
    public ResponseEntity<ResultMessage> get(@RequestParam Long id, @RequestParam Long user) {
        if (id <= 0) {
            return ResponseEntity.status(406).body(new ResultMessage(0,"Некорректный номер заказа."));
        }
        if (orderService.checkOrder(id)) {
            return ResponseEntity.status(406).body(new ResultMessage(0,"Заказ не найден."));
        }
        if (user <= 0) {
            return ResponseEntity.status(406).body(new ResultMessage(0,"Некорректный пользователь."));
        }
        if (humanService.checkUser(user)) {
            return ResponseEntity.status(406).body(new ResultMessage(0,"Пользователь не найден."));
        }
        ResultMessage resultMessage = pickupService.getOrder(orderService.getOrder(id), humanService.getUser(user));
        if (resultMessage.getId() == 0) {
            return ResponseEntity.status(406).body(resultMessage);
        } else {
            orderService.closeOrder(id);
            return ResponseEntity.ok(resultMessage);
        }
    }
}

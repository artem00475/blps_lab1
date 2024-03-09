package tuchin_emelianov.blps_lab_1.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tuchin_emelianov.blps_lab_1.jpa.entity.Delivery;
import tuchin_emelianov.blps_lab_1.jpa.entity.Pickup;
import tuchin_emelianov.blps_lab_1.request.UserRequest;
import tuchin_emelianov.blps_lab_1.service.*;

@RestController
public class PickupController {
    @Autowired
    private HumanService humanService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private PickupService pickupService;

    @GetMapping("/pickup")
    public ResponseEntity<Page<Pickup>> getPickups(@PageableDefault(sort = {"id"}, direction = Sort.Direction.ASC) Pageable pageable) {
        return ResponseEntity.ok(pickupService.getPickups(pageable));
    }

    @GetMapping("/pickup/{id}")
    public ResponseEntity<Pickup> getPickup(@PathVariable Long id) {
        return ResponseEntity.ok(pickupService.getPickup(id));
    }

    @PostMapping ("/pickup")
    public ResponseEntity<ResultMessage> work(@RequestBody UserRequest userRequest) {
        if (userRequest.getId() <= 0) {
            return ResponseEntity.badRequest().body(new ResultMessage(0,"Некорректный номер заказа."));
        }
        if (userRequest.getUser() <= 0) {
            return ResponseEntity.badRequest().body(new ResultMessage(0,"Некорректный пользователь."));
        }
        if (humanService.checkWorker(userRequest.getUser())) {
            return ResponseEntity.badRequest().body(new ResultMessage(0,"Работник не найден."));
        }
        if (orderService.checkOrder(userRequest.getId())) {
            return ResponseEntity.badRequest().body(new ResultMessage(0,"Заказ не найден."));
        }
        ResultMessage resultMessage = pickupService.giveOrder(orderService.getOrder(userRequest.getId()), humanService.getUser(userRequest.getUser()));
        if (resultMessage.getId() > 0) {
            return ResponseEntity.ok(resultMessage);
        } else {
            return ResponseEntity.badRequest().body(resultMessage);
        }
    }

    @PutMapping("/pickup")
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
        ResultMessage resultMessage = pickupService.getOrder(orderService.getOrder(userRequest.getId()), humanService.getUser(userRequest.getUser()));
        if (resultMessage.getId() == 0) {
            return ResponseEntity.badRequest().body(resultMessage);
        } else {
            orderService.closeOrder(userRequest.getId());
            return ResponseEntity.ok(resultMessage);
        }
    }
}

package tuchin_emelianov.blps_lab_1.controller;

import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import tuchin_emelianov.blps_lab_1.jpa.entity.Delivery;
import tuchin_emelianov.blps_lab_1.request.UserRequest;
import tuchin_emelianov.blps_lab_1.service.*;

import java.security.Principal;

@PreAuthorize("hasAuthority('Курьер')")
@RestController
@AllArgsConstructor
public class DeliveryController {


    private HumanService humanService;


    private OrderService orderService;


    private DeliveryService deliveryService;

    private UserService userService;

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
        if (orderService.checkOrder(userRequest.getId())) {
            return ResponseEntity.badRequest().body(new ResultMessage(0,"Заказ не найден."));
        }
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        ResultMessage resultMessage = deliveryService.takeOrder(orderService.getOrder(userRequest.getId()), humanService.getUser(userService.getUserId(auth.getName())));
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
        if (orderService.checkOrder(userRequest.getId())) {
            return ResponseEntity.badRequest().body(new ResultMessage(0,"Заказ не найден."));
        }
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        ResultMessage resultMessage = deliveryService.deliverOrder(orderService.getOrder(userRequest.getId()), humanService.getUser(userService.getUserId(auth.getName())));
        if (resultMessage.getId() > 0) {
            return ResponseEntity.ok(resultMessage);
        } else {
            return ResponseEntity.badRequest().body(resultMessage);
        }
    }

    @PreAuthorize("hasAuthority('Клиент')")
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

    @PreAuthorize("hasAuthority('Клиент')")
    @PostMapping("/delivery/receiving")
    public ResponseEntity<ResultMessage> get(@RequestBody UserRequest userRequest) {
        if (userRequest.getId() <= 0) {
            return ResponseEntity.badRequest().body(new ResultMessage(0,"Некорректный номер заказа."));
        }
        if (orderService.checkOrder(userRequest.getId())) {
            return ResponseEntity.badRequest().body(new ResultMessage(0,"Заказ не найден."));
        }
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        ResultMessage resultMessage = deliveryService.getOrder(orderService.getOrder(userRequest.getId()), humanService.getUser(userService.getUserId(auth.getName())));
        if (resultMessage.getId() == 0) {
            return ResponseEntity.badRequest().body(resultMessage);
        } else {
            orderService.closeOrder(userRequest.getId());
            return ResponseEntity.ok(resultMessage);
        }
    }
}

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
import tuchin_emelianov.blps_lab_1.jpa.entity.Pickup;
import tuchin_emelianov.blps_lab_1.request.UserRequest;
import tuchin_emelianov.blps_lab_1.service.*;

import java.security.Principal;

@PreAuthorize("hasAuthority('Работник')")
@RestController
@AllArgsConstructor
public class PickupController {

    private HumanService humanService;


    private OrderService orderService;


    private PickupService pickupService;

    private UserService userService;

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
        if (orderService.checkOrder(userRequest.getId())) {
            return ResponseEntity.badRequest().body(new ResultMessage(0,"Заказ не найден."));
        }
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        ResultMessage resultMessage = pickupService.giveOrder(orderService.getOrder(userRequest.getId()), humanService.getUser(userService.getUserId(auth.getName())));
        if (resultMessage.getId() > 0) {
            return ResponseEntity.ok(resultMessage);
        } else {
            return ResponseEntity.badRequest().body(resultMessage);
        }
    }

    @PreAuthorize("hasAuthority('Клиент')")
    @PutMapping("/pickup")
    public ResponseEntity<ResultMessage> get(@RequestBody UserRequest userRequest) {
        if (userRequest.getId() <= 0) {
            return ResponseEntity.badRequest().body(new ResultMessage(0,"Некорректный номер заказа."));
        }
        if (orderService.checkOrder(userRequest.getId())) {
            return ResponseEntity.badRequest().body(new ResultMessage(0,"Заказ не найден."));
        }
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        ResultMessage resultMessage = pickupService.getOrder(orderService.getOrder(userRequest.getId()), humanService.getUser(userService.getUserId(auth.getName())));
        if (resultMessage.getId() == 0) {
            return ResponseEntity.badRequest().body(resultMessage);
        } else {
            orderService.closeOrder(userRequest.getId());
            return ResponseEntity.ok(resultMessage);
        }
    }
}

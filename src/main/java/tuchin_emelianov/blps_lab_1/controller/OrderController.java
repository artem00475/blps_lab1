package tuchin_emelianov.blps_lab_1.controller;

import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import tuchin_emelianov.blps_lab_1.jpa.entity.Orders;
import tuchin_emelianov.blps_lab_1.request.AddRequest;
import tuchin_emelianov.blps_lab_1.request.SetPaymentTypeRequest;
import tuchin_emelianov.blps_lab_1.request.SetReceiveTypeRequest;
import tuchin_emelianov.blps_lab_1.request.UserRequest;
import tuchin_emelianov.blps_lab_1.service.*;

@RestController
@AllArgsConstructor
public class OrderController {

    private final HumanService humanService;

    private final OrderService orderService;

    private final PickupService pickupService;

    private final DeliveryService deliveryService;

    private final UserService userService;

    @GetMapping("/order")
    public ResponseEntity<Page<Orders>> getOrders(@PageableDefault(sort = {"id"}, direction = Sort.Direction.ASC) Pageable pageable) {
        return ResponseEntity.ok(orderService.getOrders(pageable));
    }

    @GetMapping("/order/{id}")
    public ResponseEntity<Orders> getOrder(@PathVariable Long id) {
        return ResponseEntity.ok(orderService.getOrder(id));
    }

    @PostMapping("/order")
    public ResponseEntity<ResultMessage> addOrder(@RequestBody AddRequest addRequest) {
        long userId;
        if (addRequest.getUserId() == null || addRequest.getUserId() < 1) {
            if (addRequest.getFio() != null && addRequest.getMail() != null && addRequest.getPhone() != null && addRequest.getUsername() != null && addRequest.getPassword() != null) {
                if (userService.loadUserByUsername(addRequest.getUsername()) != null) {
                    return ResponseEntity.badRequest().body(new ResultMessage(0, "Пользователь с таким логином уже существует"));
                }
                userId = humanService.addUser(addRequest.getFio(), addRequest.getMail(), addRequest.getPhone(), addRequest.getUsername(), addRequest.getPassword());
                if (userId < 1)
                    return ResponseEntity.badRequest().body(new ResultMessage(0, "Ошибка при создании пользователя"));
            } else {
                return ResponseEntity.badRequest().body(new ResultMessage(0,"Некорректные данные о пользователе"));
            }
        } else {
            if (!humanService.checkUser(addRequest.getUserId())) {
                userId = addRequest.getUserId();
            } else {
                return ResponseEntity.badRequest().body(new ResultMessage(0,"пользователя не существует"));
            }
        }
        if (addRequest.getProducts() == null || addRequest.getProducts().size() == 0) {
            return ResponseEntity.badRequest().body(new ResultMessage(0,"Необходимо указать товары"));
        }
        ResultMessage resultMessage = orderService.addOrder(humanService.getUser(userId), addRequest.getProducts());
        if (resultMessage.getId() < 1) {
            return ResponseEntity.badRequest().body(resultMessage);
        }
        return ResponseEntity.ok(resultMessage);
    }
    @Transactional
    @PostMapping("/order/receiving")
    public ResponseEntity<ResultMessage> setReceiveType (@RequestBody SetReceiveTypeRequest receiveTypeRequest) {
        if (receiveTypeRequest.getType().equals("Самовывоз") || (receiveTypeRequest.getType().equals("Доставка") && receiveTypeRequest.getAddress() != null)){
            if (orderService.checkOrder(receiveTypeRequest.getId())) {
                return ResponseEntity.badRequest().body(new ResultMessage(0,"Заказ не найден."));
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
                return ResponseEntity.badRequest().body(resultMessage);
            }
        }
        else{
            return ResponseEntity.badRequest().body(new ResultMessage(0,"Некорректный способ получения."));
        }
    }

    @PostMapping("/order/payment")
    public ResponseEntity<ResultMessage> setPaymentType (@RequestBody SetPaymentTypeRequest paymentTypeRequest){
        if (paymentTypeRequest.getType().equals("Онлайн") || paymentTypeRequest.getType().equals("При получении")){
            if (orderService.checkOrder(paymentTypeRequest.getId())) {
                return ResponseEntity.badRequest().body(new ResultMessage(0,"Заказ не найден."));
            }
            ResultMessage resultMessage = orderService.setPaymentType(paymentTypeRequest.getId(), paymentTypeRequest.getType());
            if (resultMessage.getId() > 0) {
                return ResponseEntity.ok(resultMessage);
            } else {
                return ResponseEntity.badRequest().body(resultMessage);
            }
        } else{
            return ResponseEntity.badRequest().body(new ResultMessage(0,"Некорректный способ оплаты."));
        }
    }

    @PutMapping("/order/payment")
    public ResponseEntity<ResultMessage> pay(@RequestBody UserRequest userRequest) {
        if (userRequest.getId() <= 0) {
            return ResponseEntity.badRequest().body(new ResultMessage(0,"Некорректный номер заказа."));
        }
        if (orderService.checkOrder(userRequest.getId())) {
            return ResponseEntity.badRequest().body(new ResultMessage(0,"Заказ не найден."));
        }
        ResultMessage resultMessage = orderService.payOnline(userRequest.getId());
        if (resultMessage.getId() == 0) {
            return ResponseEntity.badRequest().body(resultMessage);
        } else {
            return ResponseEntity.ok(resultMessage);
        }
    }

    @PostMapping("/processing")
    public ResponseEntity<ResultMessage> work(@RequestBody UserRequest userRequest) {
        if (userRequest.getId() <= 0) {
            return ResponseEntity.badRequest().body(new ResultMessage(0,"Некорректный номер заказа."));
        }
        if (orderService.checkOrder(userRequest.getId())) {
            return ResponseEntity.badRequest().body(new ResultMessage(0,"Заказ не найден."));
        }
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        ResultMessage resultMessage = orderService.work(userRequest.getId(), humanService.getUser(userService.getUserId(auth.getName())));
        if (resultMessage.getId() > 0) {
            return ResponseEntity.ok(resultMessage);
        } else {
            return ResponseEntity.badRequest().body(resultMessage);
        }
    }

    @PutMapping("/processing")
    public ResponseEntity<ResultMessage> done(@RequestBody UserRequest userRequest) {
        if (userRequest.getId() <= 0) {
            return ResponseEntity.badRequest().body(new ResultMessage(0,"Некорректный номер заказа."));
        }
        if (orderService.checkOrder(userRequest.getId())) {
            return ResponseEntity.badRequest().body(new ResultMessage(0,"Заказ не найден."));
        }
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        ResultMessage resultMessage = orderService.done(userRequest.getId(), humanService.getUser(userService.getUserId(auth.getName())));
        if (resultMessage.getId() > 0) {
            Orders order = orderService.getOrder(userRequest.getId());
            if (order.getReceiveType().getType().equals("Самовывоз")) {
                pickupService.updateOrder(order);
            } else {
                deliveryService.updateOrder(order);
            }
            return ResponseEntity.ok(resultMessage);
        } else {
            return ResponseEntity.badRequest().body(resultMessage);
        }
    }
}

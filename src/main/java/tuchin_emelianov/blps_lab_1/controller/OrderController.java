package tuchin_emelianov.blps_lab_1.controller;

import com.atomikos.icatch.jta.UserTransactionImp;
import com.atomikos.jdbc.AtomikosDataSourceBean;
import jakarta.transaction.HeuristicMixedException;
import jakarta.transaction.HeuristicRollbackException;
import jakarta.transaction.RollbackException;
import jakarta.transaction.SystemException;
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
import tuchin_emelianov.blps_lab_1.jpa.entity.Orders;
import tuchin_emelianov.blps_lab_1.jpa.entity.Product;
import tuchin_emelianov.blps_lab_1.request.AddRequest;
import tuchin_emelianov.blps_lab_1.request.SetPaymentTypeRequest;
import tuchin_emelianov.blps_lab_1.request.SetReceiveTypeRequest;
import tuchin_emelianov.blps_lab_1.request.UserRequest;
import tuchin_emelianov.blps_lab_1.service.*;

import java.security.Principal;

@PreAuthorize("hasAuthority('Клиент')")
@RestController
@AllArgsConstructor
public class OrderController {

    private final HumanService humanService;

    private final OrderService orderService;

    private final PickupService pickupService;

    private final DeliveryService deliveryService;

    private final UserService userService;
    private final AtomikosDataSourceBean atomikosDataSourceBean;
    private final UserTransactionImp utx;

    @PreAuthorize("hasAnyAuthority('Работник', 'Клиент')")
    @GetMapping("/order")
    public ResponseEntity<Page<Orders>> getOrders(@PageableDefault(sort = {"id"}, direction = Sort.Direction.ASC) Pageable pageable) {
        return ResponseEntity.ok(orderService.getOrders(pageable));
    }

    @PreAuthorize("hasAnyAuthority('Работник', 'Клиент')")
    @GetMapping("/order/{id}")
    public ResponseEntity<Orders> getOrder(@PathVariable Long id) {
        return ResponseEntity.ok(orderService.getOrder(id));
    }

    @PreAuthorize("hasAnyAuthority('Работник', 'Клиент')")
    @GetMapping("/products")
    public ResponseEntity<Page<Product>> getProducts(@PageableDefault(sort = {"id"}, direction = Sort.Direction.ASC) Pageable pageable) {
        return ResponseEntity.ok(orderService.getProducts(pageable));
    }

    @PreAuthorize("hasAnyAuthority('Работник', 'Клиент')")
    @GetMapping("/products/{id}")
    public ResponseEntity<Product> getProduct(@PathVariable Long id) {
        return ResponseEntity.ok(orderService.getProduct(id));
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
    public ResponseEntity<ResultMessage> setReceiveType(@RequestBody SetReceiveTypeRequest receiveTypeRequest) throws SystemException {
        if (receiveTypeRequest.getType().equals("Самовывоз") || (receiveTypeRequest.getType().equals("Доставка") && receiveTypeRequest.getAddress() != null)) {
            if (orderService.checkOrder(receiveTypeRequest.getId())) {
                return ResponseEntity.badRequest().body(new ResultMessage(0, "Заказ не найден."));
            }
            ResultMessage resultMessage;
            try {
                utx.begin();
                resultMessage = orderService.setReceiveType(receiveTypeRequest.getId(), receiveTypeRequest.getType());
//                method();
                if (resultMessage.getId() > 0) {
                    if (receiveTypeRequest.getType().equals("Самовывоз")) {
                        pickupService.addOrder(orderService.getOrder(receiveTypeRequest.getId()));
                    } else {
                        deliveryService.addOrder(orderService.getOrder(receiveTypeRequest.getId()), receiveTypeRequest.getAddress());
                    }
                }
                utx.commit();
            } catch (Exception e) {
                utx.rollback();
                e.printStackTrace();
                return ResponseEntity.badRequest().body(new ResultMessage(0, e.getMessage()));
            }
            if (resultMessage.getId() > 0) {
                return ResponseEntity.ok(resultMessage);
            } else {
                return ResponseEntity.badRequest().body(resultMessage);
            }
        } else {
            return ResponseEntity.badRequest().body(new ResultMessage(0, "Некорректный способ получения."));
        }
    }

    public void method() {
        throw new RuntimeException();
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

    @PreAuthorize("hasAuthority('Работник')")
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

    @PreAuthorize("hasAuthority('Работник')")
    @PutMapping("/processing")
    public ResponseEntity<ResultMessage> done(@RequestBody UserRequest userRequest) throws SystemException {
        if (orderService.checkOrder(userRequest.getId())) {
            return ResponseEntity.badRequest().body(new ResultMessage(0, "Заказ не найден."));
        }
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        ResultMessage resultMessage;
        try {
            utx.begin();
            resultMessage = orderService.done(userRequest.getId(), humanService.getUser(userService.getUserId(auth.getName())));
            if (resultMessage.getId() > 0) {
                Orders order = orderService.getOrder(userRequest.getId());
                if (order.getReceiveType().getType().equals("Самовывоз")) {
                    pickupService.updateOrder(order);
                } else {
                    deliveryService.updateOrder(order);
                }
            }
            utx.commit();
        } catch (Exception e) {
            e.printStackTrace();
            utx.rollback();
            return ResponseEntity.badRequest().body(new ResultMessage(0, e.getMessage()));
        }
        if (resultMessage.getId() > 0) {
            return ResponseEntity.ok(resultMessage);
        } else {
            return ResponseEntity.badRequest().body(resultMessage);
        }
    }
}

package tuchin_emelianov.blps_lab_1.controller;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import tuchin_emelianov.blps_lab_1.dto.OrderDTO;
import tuchin_emelianov.blps_lab_1.exceptions.BlankFieldException;
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
    private final UserService userService;

    @PreAuthorize("hasAnyAuthority('Работник', 'Клиент')")
    @GetMapping("/order")
    public ResponseEntity<Page<OrderDTO>> getOrders(@PageableDefault(sort = {"id"}, direction = Sort.Direction.ASC) Pageable pageable) {
        return ResponseEntity.ok(orderService.getOrdersDTO(pageable));
    }

    @PreAuthorize("hasAnyAuthority('Работник', 'Клиент')")
    @GetMapping("/order/{id}")
    public ResponseEntity<OrderDTO> getOrder(@PathVariable Long id) {
        return ResponseEntity.ok(orderService.getOrderDTO(id));
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
    public ResponseEntity<ResultMessage> addOrder(@RequestBody @Valid AddRequest addRequest, BindingResult bindingResult, Principal principal) {
        if (bindingResult.hasErrors()) {
            throw new BlankFieldException("Необходимо заполнить список товаров");
        }
        ResultMessage resultMessage = orderService.addOrder(humanService.getHumanByUser(userService.getUser(principal.getName())), addRequest.getProducts());
        if (resultMessage.getId() < 1) {
            return ResponseEntity.badRequest().body(resultMessage);
        }
        return ResponseEntity.ok(resultMessage);
    }

    @PostMapping("/order/receiving")
    public ResponseEntity<ResultMessage> setReceiveType(@RequestBody @Valid SetReceiveTypeRequest receiveTypeRequest, BindingResult bindingResult) {
        if (bindingResult.hasErrors())
            throw new BlankFieldException(bindingResult.getAllErrors().get(0).getDefaultMessage());

        orderService.checkOrder(receiveTypeRequest.getId());

        ResultMessage resultMessage = orderService.setReceiveType(receiveTypeRequest.getId(), receiveTypeRequest.getType(), receiveTypeRequest.getAddress());

        if (resultMessage.getId() > 0) {
            return ResponseEntity.ok(resultMessage);
        } else {
            return ResponseEntity.badRequest().body(resultMessage);
        }
    }

//    public void method() {
//        throw new RuntimeException();
//    }

    @PostMapping("/order/payment")
    public ResponseEntity<ResultMessage> setPaymentType(@RequestBody @Valid SetPaymentTypeRequest paymentTypeRequest, BindingResult bindingResult) {
        if (bindingResult.hasErrors())
            throw new BlankFieldException(bindingResult.getAllErrors().get(0).getDefaultMessage());
        orderService.checkOrder(paymentTypeRequest.getId());
        ResultMessage resultMessage = orderService.setPaymentType(paymentTypeRequest.getId(), paymentTypeRequest.getType());
        if (resultMessage.getId() > 0) {
            return ResponseEntity.ok(resultMessage);
        } else {
            return ResponseEntity.badRequest().body(resultMessage);
        }
    }

    @PutMapping("/order/payment")
    public ResponseEntity<ResultMessage> pay(@RequestBody @Valid UserRequest userRequest, BindingResult bindingResult) {
        if (bindingResult.hasErrors())
            throw new BlankFieldException(bindingResult.getAllErrors().get(0).getDefaultMessage());
        orderService.checkOrder(userRequest.getId());
        ResultMessage resultMessage = orderService.payOnline(userRequest.getId());
        if (resultMessage.getId() == 0) {
            return ResponseEntity.badRequest().body(resultMessage);
        } else {
            return ResponseEntity.ok(resultMessage);
        }
    }

    @PreAuthorize("hasAuthority('Работник')")
    @PostMapping("/processing")
    public ResponseEntity<ResultMessage> work(@RequestBody @Valid UserRequest userRequest, Principal principal, BindingResult bindingResult) {
        if (bindingResult.hasErrors())
            throw new BlankFieldException(bindingResult.getAllErrors().get(0).getDefaultMessage());
        orderService.checkOrder(userRequest.getId());
        ResultMessage resultMessage = orderService.work(userRequest.getId(), humanService.getHumanByUser(userService.getUser(principal.getName())));
        if (resultMessage.getId() > 0) {
            return ResponseEntity.ok(resultMessage);
        } else {
            return ResponseEntity.badRequest().body(resultMessage);
        }
    }

    @PreAuthorize("hasAuthority('Работник')")
    @PutMapping("/processing")
    public ResponseEntity<ResultMessage> done(@RequestBody @Valid UserRequest userRequest, Principal principal, BindingResult bindingResult) {
        if (bindingResult.hasErrors())
            throw new BlankFieldException(bindingResult.getAllErrors().get(0).getDefaultMessage());

        orderService.checkOrder(userRequest.getId());

        ResultMessage resultMessage = orderService.done(userRequest.getId(), humanService.getHumanByUser(userService.getUser(principal.getName())));

        if (resultMessage.getId() > 0) {
            return ResponseEntity.ok(resultMessage);
        } else {
            return ResponseEntity.badRequest().body(resultMessage);
        }
    }
}

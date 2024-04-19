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
import tuchin_emelianov.blps_lab_1.dto.DeliveryDTO;
import tuchin_emelianov.blps_lab_1.exceptions.BlankFieldException;
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
    public ResponseEntity<Page<DeliveryDTO>> getDeliveries(@PageableDefault(sort = {"id"}, direction = Sort.Direction.ASC) Pageable pageable) {
        return ResponseEntity.ok(deliveryService.getDeliveriesDTO(pageable));
    }

    @GetMapping("/delivery/{id}")
    public ResponseEntity<DeliveryDTO> getDelivery(@PathVariable Long id) {
        return ResponseEntity.ok(deliveryService.getDeliveryDTO(id));
    }

    @PostMapping("/delivery")
    public ResponseEntity<ResultMessage> work(@RequestBody @Valid UserRequest userRequest, Principal principal, BindingResult bindingResult) {
        if (bindingResult.hasErrors())
            throw new BlankFieldException(bindingResult.getAllErrors().get(0).getDefaultMessage());
        orderService.checkOrder(userRequest.getId());
        ResultMessage resultMessage = deliveryService.takeOrder(orderService.getOrder(userRequest.getId()), humanService.getHumanByUser(userService.getUser(principal.getName())));
        if (resultMessage.getId() > 0) {
            return ResponseEntity.ok(resultMessage);
        } else {
            return ResponseEntity.badRequest().body(resultMessage);
        }
    }

    @PutMapping("/delivery")
    public ResponseEntity<ResultMessage> done(@RequestBody @Valid UserRequest userRequest, Principal principal, BindingResult bindingResult) {
        if (bindingResult.hasErrors())
            throw new BlankFieldException(bindingResult.getAllErrors().get(0).getDefaultMessage());
        orderService.checkOrder(userRequest.getId());
        ResultMessage resultMessage = deliveryService.deliverOrder(orderService.getOrder(userRequest.getId()), humanService.getHumanByUser(userService.getUser(principal.getName())));
        if (resultMessage.getId() > 0) {
            return ResponseEntity.ok(resultMessage);
        } else {
            return ResponseEntity.badRequest().body(resultMessage);
        }
    }

    @PreAuthorize("hasAuthority('Клиент')")
    @PostMapping("/delivery/payment")
    public ResponseEntity<ResultMessage> pay(@RequestBody @Valid UserRequest userRequest, BindingResult bindingResult) {
        if (bindingResult.hasErrors())
            throw new BlankFieldException(bindingResult.getAllErrors().get(0).getDefaultMessage());
        orderService.checkOrder(userRequest.getId());
        ResultMessage resultMessage = orderService.payDelivery(orderService.getOrder(userRequest.getId()));
        if (resultMessage.getId() == 0) {
            return ResponseEntity.badRequest().body(resultMessage);
        } else {
            return ResponseEntity.ok(resultMessage);
        }
    }

    @PreAuthorize("hasAuthority('Клиент')")
    @PostMapping("/delivery/receiving")
    public ResponseEntity<ResultMessage> get(@RequestBody @Valid UserRequest userRequest, Principal principal, BindingResult bindingResult) {
        if (bindingResult.hasErrors())
            throw new BlankFieldException(bindingResult.getAllErrors().get(0).getDefaultMessage());
        orderService.checkOrder(userRequest.getId());
        ResultMessage resultMessage = deliveryService.getOrder(orderService.getOrder(userRequest.getId()), humanService.getHumanByUser(userService.getUser(principal.getName())));
        if (resultMessage.getId() == 0) {
            return ResponseEntity.badRequest().body(resultMessage);
        } else {
            return ResponseEntity.ok(resultMessage);
        }
    }
}

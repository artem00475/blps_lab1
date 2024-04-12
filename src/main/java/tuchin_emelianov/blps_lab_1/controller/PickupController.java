package tuchin_emelianov.blps_lab_1.controller;

import com.atomikos.icatch.jta.UserTransactionImp;
import jakarta.transaction.SystemException;
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
import tuchin_emelianov.blps_lab_1.dto.PickupDTO;
import tuchin_emelianov.blps_lab_1.exceptions.BlankFieldException;
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
    private UserTransactionImp utx;

    @GetMapping("/pickup")
    public ResponseEntity<Page<PickupDTO>> getPickups(@PageableDefault(sort = {"id"}, direction = Sort.Direction.ASC) Pageable pageable) {
        return ResponseEntity.ok(pickupService.getPickupsDTO(pageable));
    }

    @GetMapping("/pickup/{id}")
    public ResponseEntity<PickupDTO> getPickup(@PathVariable Long id) {
        return ResponseEntity.ok(pickupService.getPickupDTO(id));
    }

    @PostMapping("/pickup")
    public ResponseEntity<ResultMessage> work(@RequestBody @Valid UserRequest userRequest, Principal principal, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) throw new BlankFieldException(bindingResult.getAllErrors().get(0).getDefaultMessage());
        orderService.checkOrder(userRequest.getId());
        ResultMessage resultMessage = pickupService.giveOrder(orderService.getOrder(userRequest.getId()), humanService.getHumanByUser(userService.getUser(principal.getName())));
        if (resultMessage.getId() > 0) {
            return ResponseEntity.ok(resultMessage);
        } else {
            return ResponseEntity.badRequest().body(resultMessage);
        }
    }

    @PreAuthorize("hasAuthority('Клиент')")
    @PutMapping("/pickup")
    public ResponseEntity<ResultMessage> get(@RequestBody @Valid UserRequest userRequest, Principal principal, BindingResult bindingResult) throws SystemException {
        if (bindingResult.hasErrors()) throw new BlankFieldException(bindingResult.getAllErrors().get(0).getDefaultMessage());
        orderService.checkOrder(userRequest.getId());
        try {
            utx.begin();
            ResultMessage resultMessage = pickupService.getOrder(orderService.getOrder(userRequest.getId()), humanService.getHumanByUser(userService.getUser(principal.getName())));
            if (resultMessage.getId() != 0) {
                orderService.closeOrder(userRequest.getId());
            }
            utx.commit();
            if (resultMessage.getId() == 0) {
                return ResponseEntity.badRequest().body(resultMessage);
            } else {
                return ResponseEntity.ok(resultMessage);
            }
        } catch (Exception e) {
            utx.rollback();
            return ResponseEntity.badRequest().body(new ResultMessage(0, e.getMessage()));
        }
    }
}

package tuchin_emelianov.blps_lab_1.controller;

import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import tuchin_emelianov.blps_lab_1.dto.NotificationDTO;
import tuchin_emelianov.blps_lab_1.service.MessageService;

import java.security.Principal;

@RestController
@PreAuthorize("isAuthenticated()")
@AllArgsConstructor
public class MessageController {
    private final MessageService messageService;

    @GetMapping("/message")
    public ResponseEntity<Page<NotificationDTO>> getMessages(@PageableDefault(sort = {"id"}, direction = Sort.Direction.ASC) Pageable pageable, Principal principal) {
        return ResponseEntity.ok(messageService.getMessages(pageable, principal.getName()));
    }

    @GetMapping("/message/{id}")
    public ResponseEntity<NotificationDTO> getMessage(@PathVariable Long id, Principal principal) {
        return ResponseEntity.ok(messageService.getMessage(id, principal.getName()));
    }
}

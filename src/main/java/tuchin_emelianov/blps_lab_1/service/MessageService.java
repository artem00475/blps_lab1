package tuchin_emelianov.blps_lab_1.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.modelmapper.ModelMapper;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Service;
import tuchin_emelianov.blps_lab_1.dto.NotificationDTO;
import tuchin_emelianov.blps_lab_1.exceptions.EntityNotFoundException;
import tuchin_emelianov.blps_lab_1.jpa.entity.Message;
import tuchin_emelianov.blps_lab_1.jpa.repository.MessageRepository;
import tuchin_emelianov.blps_lab_1.request.MessageDTO;

import java.util.LinkedHashSet;
import java.util.Set;

@Service
@AllArgsConstructor
public class MessageService {
    private MessageRepository messageRepository;
    private HumanService humanService;
    private UserService userService;
    private ModelMapper modelMapper;

    private static final Logger LOGGER = LoggerFactory.getLogger(MessageService.class);

    private void addMessage(Message message) {
        messageRepository.save(message);
    }

    public NotificationDTO getMessage(Long id, String username) {
        Message message = messageRepository.findMessagesByReceiverAndId(humanService.getHumanByUsername(username), id);
        if (message == null) throw new EntityNotFoundException("Сообщение с id=%s не найдено или не доступно".formatted(id));
        return modelMapper.map(message, NotificationDTO.class);
    }

    public Page<NotificationDTO> getMessages(Pageable pageable, String username) {
        Page<Message> messagePage = messageRepository.findMessagesByReceiver(humanService.getHumanByUsername(username),pageable);
        return messagePage.map(messages -> modelMapper.map(messages, NotificationDTO.class));
    }

    @JmsListener(destination = "messages", id = "first")
    public void receiveMessage1(String message) throws JsonProcessingException {
        LOGGER.info(message);
        handleMessage(message);
    }

    @JmsListener(destination = "messages", id="second")
    public void receiveMessage2(String message) throws JsonProcessingException {
        LOGGER.info(message);
        handleMessage(message);
    }

    private void handleMessage(String receivedMessage) throws JsonProcessingException {
        MessageDTO messageDTO = new ObjectMapper().readValue(receivedMessage,MessageDTO.class );
        Set<String> usernames = new LinkedHashSet<>();
        if (messageDTO.isRoles()) {
            messageDTO.getTo().forEach(role -> {
                userService.getUsersByRole(role).forEach(user -> usernames.add(user.getUsername()));
            });
        } else {
            usernames.addAll(messageDTO.getTo());
        }
        usernames.forEach(username -> {
            Message message = new Message();
            message.setContent(messageDTO.getContent());
            message.setObject(messageDTO.getObject());
            message.setReceiver(humanService.getHumanByUsername(username));
            addMessage(message);
        });
    }
}

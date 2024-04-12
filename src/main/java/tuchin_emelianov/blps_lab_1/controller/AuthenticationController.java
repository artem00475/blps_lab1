package tuchin_emelianov.blps_lab_1.controller;

import jakarta.transaction.SystemException;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import tuchin_emelianov.blps_lab_1.dto.HumanDTO;
import tuchin_emelianov.blps_lab_1.exceptions.BlankFieldException;
import tuchin_emelianov.blps_lab_1.request.SignUpRequest;
import tuchin_emelianov.blps_lab_1.service.HumanService;

@RestController
@AllArgsConstructor
public class AuthenticationController {
    private final HumanService humanService;
    private final ModelMapper modelMapper;

    @PostMapping("/sign-up")
    @ResponseStatus(HttpStatus.CREATED)
    public HumanDTO signUp(@RequestBody @Valid SignUpRequest signUpRequest, BindingResult bindingResult) throws SystemException {
        if (bindingResult.hasErrors()) throw new BlankFieldException("Необходимо заполнить все поля");
        return modelMapper.map(humanService.signUp(signUpRequest), HumanDTO.class);
    }
}

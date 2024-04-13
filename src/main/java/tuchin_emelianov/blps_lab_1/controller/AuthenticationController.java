package tuchin_emelianov.blps_lab_1.controller;

import jakarta.transaction.SystemException;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import tuchin_emelianov.blps_lab_1.dto.HumanDTO;
import tuchin_emelianov.blps_lab_1.exceptions.BlankFieldException;
import tuchin_emelianov.blps_lab_1.exceptions.UserAlreadyExistsException;
import tuchin_emelianov.blps_lab_1.request.SignUpRequest;
import tuchin_emelianov.blps_lab_1.service.HumanService;

@RestController
@AllArgsConstructor
public class AuthenticationController {
    private final HumanService humanService;
    private final ModelMapper modelMapper;

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/sign-up")
    @PreAuthorize("isAnonymous()")
    public HumanDTO signUp(@RequestBody @Valid SignUpRequest signUpRequest, BindingResult bindingResult) throws SystemException {
        if (bindingResult.hasErrors()) throw new BlankFieldException("Необходимо заполнить все поля");
        return modelMapper.map(humanService.signUp(signUpRequest), HumanDTO.class);
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("sign-in")
    public String signIn(CsrfToken csrfToken) {
        return csrfToken.getToken();
    }
}

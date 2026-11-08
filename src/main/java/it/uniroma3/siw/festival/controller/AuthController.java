package it.uniroma3.siw.festival.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import it.uniroma3.siw.festival.model.Credentials;
import it.uniroma3.siw.festival.DTO.RegistrationForm;
import it.uniroma3.siw.festival.model.User;
import it.uniroma3.siw.festival.repository.CredentialRepository;
import it.uniroma3.siw.festival.service.CredentialService;
import it.uniroma3.siw.festival.service.UserService;
import jakarta.validation.Valid;

@Controller
public class AuthController {

    @Autowired
    private CredentialService credentialService;

    @Autowired
    private CredentialRepository credentialRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @GetMapping("/login")
    public String showLogin() {
        return "login";
    }

    @GetMapping("/register")
    public String showRegister(Model model) {
        model.addAttribute("registrationForm", new RegistrationForm());
        return "register";
    }

    @PostMapping("/register")
    public String register(@Valid @ModelAttribute("registrationForm") RegistrationForm form,
                           BindingResult bindingResult, Model model) {

        if (credentialRepository.findByUsername(form.getUsername()).isPresent()) {
            bindingResult.rejectValue("username", "error.username", "Username gia' in uso");
            return "register";
        }

        if (bindingResult.hasErrors()) {
            return "register";
        }

        User user = new User();
        user.setName(form.getNome());
        user.setSurname(form.getCognome());
        user.setEmail(form.getEmail());
        userService.saveUser(user);

        Credentials credentials = new Credentials();
        credentials.setUsername(form.getUsername());
        credentials.setPassword(passwordEncoder.encode(form.getPassword()));
        credentials.setRole(Credentials.USER_ROLE);
        credentials.setUser(user);
        credentialService.saveCredentials(credentials);

        return "redirect:/login";
    }

    @GetMapping("/success")
    public String success() {
        return "redirect:/";
    }
}

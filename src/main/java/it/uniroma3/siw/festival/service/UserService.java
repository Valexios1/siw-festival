package it.uniroma3.siw.festival.service;


import it.uniroma3.siw.festival.exception.UserNotFoundException;
import it.uniroma3.siw.festival.model.User;
import it.uniroma3.siw.festival.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Transactional(readOnly = true)
    public User getUser(Long id) {
        return userRepository.findById(id).orElseThrow(() -> new UserNotFoundException(id));
    }

    @Transactional
    public User saveUser(User user) {
        return userRepository.save(user);
    }


}

package com.pagamento.pag_facil.service;

import com.pagamento.pag_facil.controllers.dto.UserDTORequest;
import com.pagamento.pag_facil.exceptions.BadRequestInvalidTransactionException;
import com.pagamento.pag_facil.exceptions.InvalidTransactionInternalServerError;
import com.pagamento.pag_facil.exceptions.UnprocessableEntity;
import com.pagamento.pag_facil.repo.IUserRepo;
import com.pagamento.pag_facil.domain.user.User;
import com.pagamento.pag_facil.domain.user.UserType;
import com.pagamento.pag_facil.util.ValidaEmail;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final IUserRepo repo;

    public void validateTransaction(User sender, BigDecimal amount){
        if (sender.getUserType() == UserType.LOGISTA){
            throw new InvalidTransactionInternalServerError("Usuário do tipo logista não autorizado realizar transação de pagamento");
        }
        if(sender.getBalance().compareTo(amount) < 0) throw new UnprocessableEntity("Saldo insuficiente");

    }
    public User getUserById(Long id){
        return repo.findById(id).orElseThrow(() -> new InvalidTransactionInternalServerError("Usuario não encontrado"));
    }
    public User createUser(UserDTORequest data){

        if (repo.existsByDocument(data.getDocument())){
            throw new BadRequestInvalidTransactionException("Documento já cadastrado");
        }
        if (repo.existsByEmail(data.getEmail())){
            throw new BadRequestInvalidTransactionException("Email já cadastrado");
        }
//        if (!ValidaEmail.isValidEmail(data.getEmail())){
//            throw new BadRequestInvalidTransactionException("Email "+ data.getEmail() + " invalido");
//
//        }
        User newUser = new User();

        newUser.setFirstName(data.getFirstName());
        newUser.setLastName(data.getLastName());
        newUser.setDocument(data.getDocument());
        newUser.setPassword(data.getPassword());
        newUser.setEmail(data.getEmail());
        newUser.setBalance(data.getBalance());
        newUser.setUserType(data.getUserType());
        return repo.save(newUser);
    }
    public List<User> getAllUsers(){
        return repo.findAll();
    }

    public User saveUser(User user){

        return repo.save(user);
    }
}

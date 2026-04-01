package com.pagamento.pag_facil.controllers;

import com.pagamento.pag_facil.controllers.dto.UserDTORequest;
import com.pagamento.pag_facil.controllers.dto.UserDTOResponse;
import com.pagamento.pag_facil.domain.user.User;
import com.pagamento.pag_facil.dto.UserDTO;
import com.pagamento.pag_facil.repo.IUserRepo;
import com.pagamento.pag_facil.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;
    private final IUserRepo userRepo;

    @PostMapping
    public ResponseEntity<UserDTOResponse> createUser(@RequestBody UserDTORequest userDTORequest){
        User newUser = userService.createUser(userDTORequest);
        UserDTOResponse response = new UserDTOResponse(newUser);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
//    @GetMapping
//    public ResponseEntity<List<User>> getAllUsers(){
//        List<User> users = userService.getAllUsers();
//        return new ResponseEntity<>(users,HttpStatus.OK);
//    }
@GetMapping
public ResponseEntity<List<UserDTO>> getAllUsers() {
    List<UserDTO> users = userService.getAllUsers()
            .stream()
            .map(user -> new UserDTO(
                    user.getId(),
                    user.getFirstName(),
                    user.getLastName(),
                    user.getDocument(),
                    user.getBalance(),
                    user.getUserType()
            ))
            .toList();

    return new ResponseEntity<>(users, HttpStatus.OK);
}
}

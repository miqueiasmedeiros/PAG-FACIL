package com.pagamento.pag_facil.repo;

import com.pagamento.pag_facil.domain.user.User;
import com.pagamento.pag_facil.dto.UserDTO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.ListCrudRepository;

import java.util.Optional;

public interface IUserRepo extends ListCrudRepository<User, Long> {
//   UserDTO findUserByDocument(String document);
    boolean existsByDocument(String document);
    boolean existsByEmail(String email);
}


package com.pagamento.pag_facil.service;

import com.pagamento.pag_facil.domain.transaction.Transaction;
import com.pagamento.pag_facil.domain.user.User;
import com.pagamento.pag_facil.dto.TransactionDTORequest;
import com.pagamento.pag_facil.dto.TransactionDTOResponse;
import com.pagamento.pag_facil.repo.ITransactionRepo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Supplier;

@Service
@Slf4j
@RequiredArgsConstructor
public class TransactionService {


   private final ITransactionRepo transactionRepo;
   private final AuthorizerService authorizerService;
   private final UserService userService;
   private final OutboxService outboxService;

   private static final ConcurrentMap<Long, ReentrantLock> USER_LOCKS = new ConcurrentHashMap<>();

   @Transactional
   public TransactionDTOResponse createTransaction(TransactionDTORequest transactionDTO)  {
       return executeWithUserLocks(transactionDTO.senderId(), transactionDTO.receiverId(),
               () -> processTransaction(transactionDTO));
   }

   private TransactionDTOResponse processTransaction(TransactionDTORequest transactionDTO) {
       User sender = userService.getUserById(transactionDTO.senderId());
       User receiver = userService.getUserById(transactionDTO.receiverId());

       userService.validateTransaction(sender, transactionDTO.value());
       authorizerService.authorize(transactionDTO);

       Transaction transaction = new Transaction();
       transaction.setAmount(transactionDTO.value());
       transaction.setSender(sender);
       transaction.setReceiver(receiver);
       transaction.setTimestamp(LocalDateTime.now());

       sender.setBalance(sender.getBalance().subtract(transactionDTO.value()));
       receiver.setBalance(receiver.getBalance().add(transactionDTO.value()));

       Transaction savedTransaction = transactionRepo.save(transaction);
       outboxService.saveTransferNotification(savedTransaction);
       return toResponse(savedTransaction);
   }

   private TransactionDTOResponse executeWithUserLocks(Long senderId, Long receiverId, Supplier<TransactionDTOResponse> supplier) {
       Long firstId = senderId <= receiverId ? senderId : receiverId;
       Long secondId = senderId <= receiverId ? receiverId : senderId;

       ReentrantLock firstLock = getUserLock(firstId);
       ReentrantLock secondLock = firstId.equals(secondId) ? null : getUserLock(secondId);

       firstLock.lock();
       try {
           if (secondLock != null) {
               secondLock.lock();
           }
           try {
               return supplier.get();
           } finally {
               if (secondLock != null) {
                   secondLock.unlock();
               }
           }
       } finally {
           firstLock.unlock();
       }
   }

   private ReentrantLock getUserLock(Long userId) {
       return USER_LOCKS.computeIfAbsent(userId, id -> new ReentrantLock());
   }

   private TransactionDTOResponse toResponse(Transaction transaction) {
       return new TransactionDTOResponse(
               transaction.getId(),
               transaction.getAmount(),
               toParticipant(transaction.getSender()),
               toParticipant(transaction.getReceiver()),
               transaction.getTimestamp()
       );
   }

   private TransactionDTOResponse.ParticipantSummary toParticipant(User user) {
       return new TransactionDTOResponse.ParticipantSummary(
               user.getId(),
               user.getFirstName(),
               user.getBalance(),
               user.getUserType()
       );
   }

}

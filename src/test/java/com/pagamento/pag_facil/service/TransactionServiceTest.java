package com.pagamento.pag_facil.service;

import com.pagamento.pag_facil.domain.transaction.Transaction;
import com.pagamento.pag_facil.domain.user.User;
import com.pagamento.pag_facil.dto.TransactionDTORequest;
import com.pagamento.pag_facil.dto.TransactionDTOResponse;
import com.pagamento.pag_facil.exceptions.UnprocessableEntity;
import com.pagamento.pag_facil.repo.ITransactionRepo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @Mock
    private ITransactionRepo transactionRepo;

    @Mock
    private AuthorizerService authorizerService;

    @Mock
    private UserService userService;

    @Mock
    private OutboxService outboxService;

    @InjectMocks
    private TransactionService transactionService;

    @Test
    void shouldCreateTransactionUpdateBalancesAndWriteOutbox() {
        User sender = user(1L, new BigDecimal("100.00"));
        User receiver = user(2L, new BigDecimal("10.00"));

        when(userService.getUserById(1L)).thenReturn(sender);
        when(userService.getUserById(2L)).thenReturn(receiver);
        doNothing().when(userService).validateTransaction(sender, new BigDecimal("25.00"));
        doNothing().when(authorizerService).authorize(any(TransactionDTORequest.class));

        when(transactionRepo.save(any(Transaction.class))).thenAnswer(invocation -> {
            Transaction tx = invocation.getArgument(0);
            tx.setId(50L);
            return tx;
        });

        TransactionDTORequest dto = new TransactionDTORequest(new BigDecimal("25.00"), 1L, 2L);

        TransactionDTOResponse result = transactionService.createTransaction(dto);

        assertThat(result.id()).isEqualTo(50L);

        ArgumentCaptor<Transaction> txCaptor = ArgumentCaptor.forClass(Transaction.class);
        verify(transactionRepo).save(txCaptor.capture());
        Transaction toSave = txCaptor.getValue();
        assertThat(toSave.getAmount()).isEqualByComparingTo("25.00");
        assertThat(toSave.getSender()).isSameAs(sender);
        assertThat(toSave.getReceiver()).isSameAs(receiver);
        assertThat(toSave.getTimestamp()).isNotNull();

        assertThat(sender.getBalance()).isEqualByComparingTo("75.00");
        assertThat(receiver.getBalance()).isEqualByComparingTo("35.00");

        verify(outboxService).saveTransferNotification(any(Transaction.class));
        verify(authorizerService).authorize(dto);
        verify(userService).validateTransaction(sender, new BigDecimal("25.00"));
    }

    @Test
    void shouldAcceptOnlyOneTransactionWhenTwoRequestsRaceForSameBalance() throws Exception {
        User sender = user(1L, new BigDecimal("50.00"));
        User receiver = user(2L, new BigDecimal("10.00"));

        when(userService.getUserById(1L)).thenReturn(sender);
        when(userService.getUserById(2L)).thenReturn(receiver);

        doAnswer(invocation -> {
            User s = invocation.getArgument(0);
            BigDecimal amount = invocation.getArgument(1);
            if (s.getBalance().compareTo(amount) < 0) {
                throw new UnprocessableEntity("Saldo insuficiente");
            }
            return null;
        }).when(userService).validateTransaction(any(User.class), any(BigDecimal.class));

        // Simula uma janela de concorrência no fluxo após validação.
        doAnswer(invocation -> {
            Thread.sleep(100);
            return null;
        }).when(authorizerService).authorize(any(TransactionDTORequest.class));

        AtomicLong ids = new AtomicLong(100);
        when(transactionRepo.save(any(Transaction.class))).thenAnswer(invocation -> {
            Transaction tx = invocation.getArgument(0);
            tx.setId(ids.incrementAndGet());
            return tx;
        });

        TransactionDTORequest dto = new TransactionDTORequest(new BigDecimal("50.00"), 1L, 2L);

        int successCount;
        try (ExecutorService executor = Executors.newFixedThreadPool(2)) {
            CountDownLatch startGate = new CountDownLatch(1);
            Callable<Boolean> task = () -> {
                startGate.await();
                try {
                    transactionService.createTransaction(dto);
                    return true;
                } catch (Exception ex) {
                    return false;
                }
            };

            Future<Boolean> first = executor.submit(task);
            Future<Boolean> second = executor.submit(task);
            startGate.countDown();
            List<Future<Boolean>> futures = List.of(first, second);

            successCount = 0;
            for (Future<Boolean> future : futures) {
                if (future.get()) {
                    successCount++;
                }
            }

            executor.shutdown();
            assertThat(executor.awaitTermination(5, TimeUnit.SECONDS)).isTrue();
        }

        assertThat(successCount).isEqualTo(1);
        assertThat(sender.getBalance()).isEqualByComparingTo("0.00");
        assertThat(receiver.getBalance()).isEqualByComparingTo("60.00");

        verify(transactionRepo, times(1)).save(any(Transaction.class));
        verify(outboxService, times(1)).saveTransferNotification(any(Transaction.class));
    }

    private User user(Long id, BigDecimal balance) {
        User u = new User();
        u.setId(id);
        u.setBalance(balance);
        return u;
    }
}

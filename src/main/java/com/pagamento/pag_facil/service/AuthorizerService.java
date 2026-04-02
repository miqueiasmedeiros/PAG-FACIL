package com.pagamento.pag_facil.service;

import com.pagamento.pag_facil.dto.AuthorizationDTO;
import com.pagamento.pag_facil.dto.TransactionDTORequest;
import com.pagamento.pag_facil.exceptions.BadRequestInvalidTransactionException;
import com.pagamento.pag_facil.exceptions.UnauthorizedTransactionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Service
public class AuthorizerService {

    private static final Logger LOGGER = LoggerFactory.getLogger(AuthorizerService.class);
    private final RestClient restClient;

    public AuthorizerService(RestClient.Builder builder) {
        this.restClient = builder.baseUrl("https://util.devi.tools/api/v2/authorize").build();
    }
    public void authorize(TransactionDTORequest transaction){
        LOGGER.info("authorizing transaction{}...", transaction);

        try {
            AuthorizationDTO responseBody = restClient.get()
                    .retrieve()
                    .body(AuthorizationDTO.class);

            if(responseBody==null || !responseBody.isAuthorization()){
                throw new UnauthorizedTransactionException("Unauthorized");
            }
        }catch (HttpClientErrorException.Forbidden ex){
            LOGGER.warn("Authorization service denied transaction. Response: {}", ex.getResponseBodyAsString());
            throw new UnauthorizedTransactionException("Service unavailable");
        }catch (RestClientException ex){
            LOGGER.error("Error calling authorization service: ", ex);
            throw new BadRequestInvalidTransactionException("Error calling authorization service");
        }


//        var response = restClient.get()
//                .retrieve()
//                .toEntity(AuthorizationDTO.class);
//
//        if(response.getStatusCode().isError()
//                || response.getBody() == null
//                || !response.getBody().isAuthorization()){
//            throw new UnauthorizedTransactionException("Unauthorized");
//    }

}
}

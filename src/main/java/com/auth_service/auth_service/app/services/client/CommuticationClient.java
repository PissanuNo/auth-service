package com.auth_service.auth_service.app.services.client;


import com.auth_service.auth_service.app.model.dto.client.EmailRequest;
import com.auth_service.auth_service.core.model.ResponseBodyModel;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

@Service
public class CommuticationClient {

    private static final Logger log = LoggerFactory.getLogger(CommuticationClient.class);

    private final WebClient webClient;

    @Autowired
    public CommuticationClient(WebClient.Builder webClientBuilder,
                                @Value("${communication.service}") String accountServiceUrl) {
        this.webClient = webClientBuilder.baseUrl(accountServiceUrl).build();
    }

    public String sendEmail(EmailRequest request) {
        return webClient.post()
                .uri("/v2/email")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<ResponseBodyModel<String>>() {
                }) //   Generic
                .doOnNext(response -> log.info("Raw Response: {}", response)) //  Log response
                .map(ResponseBodyModel::getCode) //  use `objectValue` from ResponseBodyModel
                .doOnError(WebClientResponseException.class,
                        ex -> log.error("Error send email {}", ex.getMessage()))
                .block();
    }

}

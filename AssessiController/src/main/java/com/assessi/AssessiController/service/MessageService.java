package com.assessi.AssessiController.service;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import com.assessi.AssessiController.model.Answer;
import com.assessi.AssessiController.model.Message;

import reactor.core.publisher.Mono;


@Service
public class MessageService {

	private List<Message> messages = new ArrayList<>();

	private static final Logger logger = LoggerFactory.getLogger(MessageService.class);
	
    public List<Message> getAllMessages() {
        return messages;
    }

    public void addMessage(Message message) {
        messages.add(message);
    }
    private final WebClient webClient;

    public MessageService(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.baseUrl("https://triggerrephraserusingflask-webapp-b2h7frc0hyhjdab4.centralindia-01.azurewebsites.net").defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    public Mono<List<Answer>> getApiResponse(String answers) {
        return this.webClient.post()
            .uri("/submit_answers")
            .accept(MediaType.APPLICATION_JSON)
            .bodyValue(answers)
            .retrieve()
            .bodyToFlux(Answer.class)
            .collectList()
            .doOnError(error -> {
                if (error instanceof WebClientResponseException) {
                    WebClientResponseException ex = (WebClientResponseException) error;
                    logger.error("Error Status Code: " + ex.getStatusCode().value());
                } else {
                	logger.error("An unexpected error occurred: " + error.getMessage());
                }
            });
    }
}

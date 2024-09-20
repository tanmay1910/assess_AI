package com.assessi.AssessiController.controller;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.assessi.AssessiController.model.Answer;
import com.assessi.AssessiController.model.Message;
import com.assessi.AssessiController.service.DocumentIntelligenceService;
import com.assessi.AssessiController.service.AnswerGrammarSpellEvaluatorService;
import com.assessi.AssessiController.service.MessageService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@RestController
@RequestMapping("/assessi/messages")
public class RestMessageController {

	@Autowired
    private MessageService messageService;
	
	@Autowired
	DocumentIntelligenceService documentIntelligenceService;
	
	@Autowired
	AnswerGrammarSpellEvaluatorService answerGrammarSpellEvaluatorService;
	
	@Autowired
	ObjectMapper objectMapper;
	
	private static final Logger logger = LoggerFactory.getLogger(RestMessageController.class);
	
	String returnObj = "";

    @GetMapping
    public List<Message> getAllMessages() {
        return messageService.getAllMessages();
    }

    @PostMapping
    public void addMessage(@RequestBody Message message) {
    	logger.info("Message: "+ message);
        messageService.addMessage(message);
        if ("".equalsIgnoreCase(returnObj)) {
        	returnObj = documentIntelligenceService.processDocument(message.getUploadedFileName(), returnObj);
        	logger.info("DocParser ReferenceAnswer response: "+ returnObj);
        } else {
        	try {
        		returnObj = documentIntelligenceService.processDocument(message.getUploadedFileName(), returnObj);
        		logger.info("DocParser StudentAnswer response: "+ objectMapper.writeValueAsString(returnObj));
	        	List<Answer> answer = messageService.getApiResponse(returnObj).block();
	        	logger.info("Rephrase response: "+ objectMapper.writeValueAsString(answer));
	        	answer = answerGrammarSpellEvaluatorService.evaluateAnswersFromJson(answer);
	        	
					logger.info("Grammar/Spell check response: "+ objectMapper.writeValueAsString(answer));
				} catch (JsonProcessingException e) {
					logger.error(e.toString());
				}
	        	returnObj = "";
        }
        
    }
}

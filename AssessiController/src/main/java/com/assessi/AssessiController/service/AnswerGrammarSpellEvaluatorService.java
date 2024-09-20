package com.assessi.AssessiController.service;

import java.util.List;

import org.languagetool.JLanguageTool;
import org.languagetool.language.AmericanEnglish;
import org.languagetool.rules.RuleMatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.assessi.AssessiController.model.Answer;

@Service
public class AnswerGrammarSpellEvaluatorService {

		 private static final float GRAMMAR_ERROR_PENALTY = 0.25f;
		    private static final float SPELLING_ERROR_PENALTY = 0.1f;
		    private static final float MAX_MARKS_PER_ANSWER = 2.0f;
		    
		    private static final Logger logger = LoggerFactory.getLogger(AnswerGrammarSpellEvaluatorService.class);

		    public List<Answer> evaluateAnswersFromJson(List<Answer> answers) {
		        try {
		            // Parse the JSON file to extract the answers
		            //ObjectMapper objectMapper = new ObjectMapper();
		            //List<Answer> answers = objectMapper.readValue(answer, new TypeReference<List<Answer>>() {});

		            // Create a LanguageTool instance for grammar and spell checking
		            JLanguageTool languageTool = new JLanguageTool(new AmericanEnglish());

		            double totalMarks = 0.0;
		            int answerCount = 0;

		            // Process each answer in the JSON list
		            for (Answer answerObj : answers) {
		                String answerText = answerObj.getAnswer();
		                answerCount++;

		                logger.info("Evaluating Answer " + answerCount + ": " + answerText);

		                // Start with the full marks for this answer
		                float marksForAnswer = answerObj.getRephraseMarks(); //MAX_MARKS_PER_ANSWER;

		                // Check for grammar and spelling errors
		                List<RuleMatch> matches = languageTool.check(answerText);
		                int grammarErrors = 0;
		                int spellingErrors = 0;

						/*
						 * for (RuleMatch match : matches) { // Detect spelling errors by checking the
						 * rule ID if (match.getRule().getId().startsWith("MORFOLOGIK_RULE")) {
						 * spellingErrors++; } else { grammarErrors++; } }
						 */
		                
		                // If there are no matches, the sentence is grammatically correct
		                if (matches.isEmpty()) {
		                	logger.info("The sentence is grammatically correct.");
		                } else {
		                	logger.info("The sentence has grammar issues:");

		                    // Print out the grammar errors and suggestions
		                    for (RuleMatch match : matches) {
		                    	if (match.getRule().getId().startsWith("MORFOLOGIK_RULE")) {
		                            spellingErrors++;
		                        } else {
		                            grammarErrors++;
		                        }
		                    	logger.info("Potential error at positions " + match.getFromPos() + "-" + match.getToPos() + ": " + match.getMessage());
		                    	logger.info("Suggested correction(s): " + match.getSuggestedReplacements());
		                    }
		                }

		                // Deduct marks based on errors
		                float grammarPenalty = grammarErrors * GRAMMAR_ERROR_PENALTY;
		                float spellingPenalty = spellingErrors * SPELLING_ERROR_PENALTY;
		                float totalPenalty = grammarPenalty + spellingPenalty;

		                // Ensure total penalty does not exceed the max marks
		                marksForAnswer = Math.max(marksForAnswer - totalPenalty, 0);
		                answerObj.setGrammarMarks(marksForAnswer);
		                totalMarks += marksForAnswer;

		                logger.info("Grammar errors: " + grammarErrors + ", Spelling errors: " + spellingErrors);
		                logger.info("Marks for Answer " + answerCount + ": " + marksForAnswer);
		                logger.info("--------------------------------------------------");
		            }

		            // Output total marks
		            logger.info("Total marks for all answers: " + totalMarks + " out of " + (answerCount * MAX_MARKS_PER_ANSWER));

		        } catch (Exception e) {
		        	logger.error("Error processing the JSON file: " + e.getMessage());
		        }
		        return answers;
		    }
}

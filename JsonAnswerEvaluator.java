package evalPaper;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.languagetool.JLanguageTool;
import org.languagetool.language.AmericanEnglish;
import org.languagetool.rules.RuleMatch;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

public class JsonAnswerEvaluator {
	
	 private static final double GRAMMAR_ERROR_PENALTY = 0.25;
	    private static final double SPELLING_ERROR_PENALTY = 0.1;
	    private static final double MAX_MARKS_PER_ANSWER = 2.0;

	    public static void main(String[] args) {
	        // Path to the JSON file
	        String filePath = "C:\\Geetanjali\\TestResult.json"; 

	        // Parse the JSON and evaluate the answers
	        evaluateAnswersFromJson(filePath);
	    }

	    public static void evaluateAnswersFromJson(String filePath) {
	        try {
	            // Parse the JSON file to extract the answers
	            ObjectMapper objectMapper = new ObjectMapper();
	            List<Answer> answers = objectMapper.readValue(new File(filePath), new TypeReference<List<Answer>>() {});

	            // Create a LanguageTool instance for grammar and spell checking
	            JLanguageTool languageTool = new JLanguageTool(new AmericanEnglish());

	            double totalMarks = 0.0;
	            int answerCount = 0;

	            // Process each answer in the JSON list
	            for (Answer answerObj : answers) {
	                String answerText = answerObj.getAnswer();
	                answerCount++;

	                System.out.println("Evaluating Answer " + answerCount + ": " + answerText);

	                // Start with the full marks for this answer
	                double marksForAnswer = MAX_MARKS_PER_ANSWER;

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
	                    System.out.println("The sentence is grammatically correct.");
	                } else {
	                    System.out.println("The sentence has grammar issues:");

	                    // Print out the grammar errors and suggestions
	                    for (RuleMatch match : matches) {
	                    	if (match.getRule().getId().startsWith("MORFOLOGIK_RULE")) {
	                            spellingErrors++;
	                        } else {
	                            grammarErrors++;
	                        }
	                        System.out.println("Potential error at positions " + match.getFromPos() + "-" + match.getToPos() + ": " + match.getMessage());
	                        System.out.println("Suggested correction(s): " + match.getSuggestedReplacements());
	                    }
	                }

	                // Deduct marks based on errors
	                double grammarPenalty = grammarErrors * GRAMMAR_ERROR_PENALTY;
	                double spellingPenalty = spellingErrors * SPELLING_ERROR_PENALTY;
	                double totalPenalty = grammarPenalty + spellingPenalty;

	                // Ensure total penalty does not exceed the max marks
	                marksForAnswer = Math.max(marksForAnswer - totalPenalty, 0);
	                totalMarks += marksForAnswer;

	                System.out.println("Grammar errors: " + grammarErrors + ", Spelling errors: " + spellingErrors);
	                System.out.println("Marks for Answer " + answerCount + ": " + marksForAnswer);
	                System.out.println("--------------------------------------------------");
	            }

	            // Output total marks
	            System.out.println("Total marks for all answers: " + totalMarks + " out of " + (answerCount * MAX_MARKS_PER_ANSWER));

	        } catch (IOException e) {
	            System.err.println("Error processing the JSON file: " + e.getMessage());
	        }
	    }

}

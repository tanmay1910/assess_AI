package com.assessi.AssessiController.model;

import lombok.Data;

@Data
public class Answer {
	private int questionNumber;
	private float maxMarks;
	private float rephraseMarks;
	private float grammarMarks;
	private String question;
	private String answer;
	private String referenceAnswer;
}

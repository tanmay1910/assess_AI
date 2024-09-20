package com.assessi.AssessiController.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.assessi.AssessiController.model.Answer;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class Parser {

	private static Logger log = LoggerFactory.getLogger(Parser.class);
	static JSONArray sectionsArray = new JSONArray();
	static JSONArray tablesArray = new JSONArray();
	static JSONArray paragraphArray = new JSONArray();
	static String lastParaHeader = "";
	
	
	
	public List<Answer> parseSection(List<Answer> refAnswerlist, JSONObject section) {
		log.debug("DocIntellPostRequest.parseSection: Entering method.");
		JSONArray elementsArray = section.getJSONArray("elements");
		log.debug("DocIntellPostRequest.parseSection: Exiting method.");
		return parseElement(refAnswerlist, elementsArray);
	}

	public List<Answer> parseSection(List<Answer> refAnswerlist, String id) {
		log.debug("DocIntellPostRequest.parseSection: Entering method.");
		String s = id.split("/")[2];
		JSONObject obj = sectionsArray.getJSONObject((Integer.parseInt(s)));
		log.debug("DocIntellPostRequest.parseSection: Exiting method.");
		return parseSection(refAnswerlist, obj);
	}

	public List<Answer> parseElement(List<Answer> refAnswerlist, JSONArray elementsArray) {
		log.debug("DocIntellPostRequest.parseElement: Entering method.");
		List<Answer> answerList = new ArrayList<Answer>();
		Answer answer = new Answer();
		try {
		if (!elementsArray.isEmpty()) {
			for (int i = 0; i < elementsArray.length(); i++) {
				String element = elementsArray.getString(i);
				if (element.contains("paragraphs")) {
					answer = parseParagraph(refAnswerlist, answer, element);
					
					if (answer.getAnswer() != null) {
						answerList.add(answer);
					} else {

					}
		
				}
			}
		}
		log.debug("DocIntellPostRequest.parseElement: Entering method.");
		}catch(Exception e) {
			log.error("DocIntellPostRequest.parseElement: Exception ", e);
		}
		log.debug("DocIntellPostRequest.parseElement: Exiting method.");
		return answerList;
	}

	public Answer parseParagraph(List<Answer> refAnswerlist,Answer answer, String id) {
		log.debug("DocIntellPostRequest.parseParagraph: Entering method.");
		String s = id.split("/")[2];
		JSONObject obj = paragraphArray.getJSONObject((Integer.parseInt(s)));
		String text = obj.getString("content");
		if (text.startsWith("(") && !text.startsWith("(A")) {// (1) Explain the importance of photosynthesis in plants.
																// (2)
			Answer newAnswer = new Answer();
			String[] split = text.split("\\(");// 1) Explain the importance of photosynthesis in plants. ,2)
			String[] split1 = split[1].split("\\)");// 1 Explain the importance of photosynthesis in plants. ,2)
			String[] split2 = split[2].split("\\)");
			int questionNo = Integer.parseInt(split1[0]);
			newAnswer.setQuestionNumber(questionNo);
			newAnswer.setQuestion(split1[1]);
			newAnswer.setMaxMarks(Float.parseFloat(split2[0]));
			return newAnswer;
		} else if (text.startsWith("(A")) {
			String[] split = text.split("\\)");
			answer.setAnswer(split[1]);
			if(refAnswerlist.size()>0) {
				int questionNo = answer.getQuestionNumber();
				String refAnswer = refAnswerlist.get(questionNo-1).getAnswer();
				answer.setReferenceAnswer(refAnswer);
			}
			
			log.debug("AnswerObject:" + answer);
			return answer;
		}

		log.debug("DocIntellPostRequest.parseParagraph: Exiting method.");
		return answer;

	}

	public String parseCell(String id, JSONArray cellArray) {
		log.debug("DocIntellPostRequest.parseParagraph: Entering method.");
		JSONObject obj = cellArray.getJSONObject((Integer.parseInt(id)));
		log.debug("DocIntellPostRequest.parseParagraph: Exiting method.");
		return obj.getString("content");
	}

	public String parseDocIntellJson(String refJsonString, String newJson) throws JsonProcessingException {
		log.debug("DocIntellPostRequest.parseDocIntellJson: Entering method.");
		
		JSONObject jsonObject = new JSONObject(newJson);
		JSONObject analyzeResult = jsonObject.getJSONObject("analyzeResult");
		
		tablesArray = analyzeResult.getJSONArray("tables");
		paragraphArray = analyzeResult.getJSONArray("paragraphs");
		sectionsArray = analyzeResult.getJSONArray("sections");
		ObjectMapper Obj = new ObjectMapper();
		List<Answer> list= new ArrayList<Answer>();
		List<Answer> refAnswerList= new ArrayList<Answer>();
		Answer[] refAnswerArray;
		try {
			if (!isNullEmpty(refJsonString)) {
				refAnswerArray = Obj.readValue(refJsonString, Answer[].class);
				refAnswerList = Arrays.asList(refAnswerArray);
			}
		} catch (Exception e) {
			log.error("DocIntellPostRequest.parseDocIntellJson: Exception ", e);
		}
		
		if (!sectionsArray.isEmpty()) {
			for (int i = 0; i < sectionsArray.length(); i++) {
				JSONObject section = sectionsArray.getJSONObject(i);
				list = parseSection(refAnswerList, section);
				log.debug("parsed section:" + list);
			}
		}
		log.debug("DocIntellPostRequest.parseDocIntellJson: Exiting method.");
		return Obj.writeValueAsString(list);
	}

	public boolean isNullEmpty(String s) {
		if (s == null || s.isEmpty()) {
			return true;
		}
		return false;
	}
}

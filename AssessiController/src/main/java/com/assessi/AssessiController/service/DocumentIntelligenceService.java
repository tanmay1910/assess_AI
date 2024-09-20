package com.assessi.AssessiController.service;



import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Map;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;


@Service
public class DocumentIntelligenceService {
		
		private static Logger log = LoggerFactory.getLogger(DocumentIntelligenceService.class);
		
		public String processDocument(String path, String ref) {
			DocumentIntelligenceService obj = new DocumentIntelligenceService();
			Parser parser= new Parser();
			String returnObj = "";
			// invoking the method POSTReq()
			try {
				String uri = obj.postDocument(path);//send document
				Thread.sleep(5000);//wait for the processing
				String json = obj.getJson(uri);//get scanned json
				JSONObject jsonObject = new JSONObject(json);
				log.info("jsonObject: " + jsonObject);
				returnObj = parser.parseDocIntellJson(ref, json);
				log.info("returnObj: " + returnObj);
			} catch (Exception e) {
				log.error(e.toString());
			}
			return returnObj;
		}
	
		
		 public String postDocument(String docPath) throws Exception {
				log.debug("DocIntellPostRequest.postDocument: Entering method.");
			 	String uri = "https://sneh08-docintell.cognitiveservices.azure.com/documentintelligence/documentModels/prebuilt-layout:analyze?api-version=2024-02-29-preview";
		        HttpClient client = HttpClient.newBuilder().build();
		        String messageContent = "{\r\n" + "'urlSource':'https://sneh08storagedocintell.z13.web.core.windows.net/hackbox/"
		        		+ docPath + "'\r\n" + "}";
		        log.info("messagecontent: "+messageContent);
		        HttpRequest request = HttpRequest.newBuilder()
		                .uri(URI.create(uri))
		                .header("Content-Type", "application/json").header("Ocp-Apim-Subscription-Key", "dummy")
		                .POST(HttpRequest.BodyPublishers.ofString(messageContent))
		                .build();
		        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
		        log.info("Response headers ->\n" + response.headers());
		        log.info("Response statusCode ->\n" + response.statusCode());
		        Map<String,List<String>> headerMap = response.headers().map();
		        log.debug("DocIntellPostRequest.postDocument: Exiting method.");
		        return headerMap.get("operation-location").get(0);
		    }
		 
		 public String getJson(String arg) throws Exception {
			 log.debug("DocIntellPostRequest.getJson: Entering method.");
		        HttpClient client = HttpClient.newBuilder().build();
		        HttpRequest request = HttpRequest.newBuilder()
		                .uri(URI.create(arg)).header("Ocp-Apim-Subscription-Key", "dummy")
		                .build();
		        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
		        log.info("Response ->\n" + response.body());
		        log.debug("DocIntellPostRequest.getJson: Exiting method.");
		        return response.body();
		    }
		 
		
}

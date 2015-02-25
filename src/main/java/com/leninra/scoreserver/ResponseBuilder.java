package com.leninra.scoreserver;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;

import com.leninra.scoreserver.scoremanager.ScoreManager;
import com.leninra.scoreserver.scoremanager.ScoreManager.LevelNotFoundException;
import com.leninra.scoreserver.scoremanager.ScoreManager.UserNotFoundException;
import com.leninra.scoreserver.session.UserSessionPool;

public class ResponseBuilder {
	
	private UserSessionPool userSessionPool;
	private ScoreManager scoreManager;
	
	public ResponseBuilder() {
		
		userSessionPool = UserSessionPool.getInstance(10 * 60 * 1000);
		scoreManager = ScoreManager.getInstance();
		
	}
	
	
	public HttpResponse login(String[] pathElems) throws IOException {
		
		try {
			
			int userId = Integer.parseInt(pathElems[1]);
			
			String sessionKey = userSessionPool.getSessionKey(userId);
			return new HttpResponse(HttpURLConnection.HTTP_OK, sessionKey);
			
		} catch (NumberFormatException nfe) {
			return new HttpResponse(HttpURLConnection.HTTP_BAD_REQUEST, "MALFORMED USER ID");
		}
		
	}
	
	
	public HttpResponse getHighscoreList(String[] pathElems) throws IOException {
		
		try {
			
			int levelId = Integer.parseInt(pathElems[1]);
			
			String highscoresCsv = scoreManager.getHighscoresAsCsv(levelId, 15);
			return new HttpResponse(HttpURLConnection.HTTP_OK, "text/csv", highscoresCsv);
			
		} catch (LevelNotFoundException lnfe) {
			return new HttpResponse(HttpURLConnection.HTTP_INTERNAL_ERROR, "LEVEL NOT FOUND");
		} catch (NumberFormatException nfe) {
			return new HttpResponse(HttpURLConnection.HTTP_BAD_REQUEST, "MALFORMED LEVEL ID");
		}
		
	}
	
	public HttpResponse saveScore(String[] pathElems, String query, InputStream requestBody) throws IOException {
		
		String[] sessionKeyParam;
		System.out.println(pathElems[0]+", "+pathElems[1]+", "+pathElems[2]+", ");
		System.out.println(query);
		
		if (!pathElems[2].equalsIgnoreCase("score") ||
			query == null || query.length() == 0 || 
			(sessionKeyParam = query.split("=")).length != 2 ||
			!sessionKeyParam[0].equalsIgnoreCase("sessionkey")) {
			return new HttpResponse(HttpURLConnection.HTTP_BAD_REQUEST, "INVALID REQUEST");
		}
		
		String sessionKey = sessionKeyParam[1];
		
		int level = -1;
		
		try {
			level = Integer.parseInt(pathElems[1]);
		} catch (NumberFormatException nfe) {
			return new HttpResponse(HttpURLConnection.HTTP_BAD_REQUEST, "MALFORMED LEVEL PARAMETER");
		}
		
		String content = readRequestBody(requestBody);
	        
	    try {
	        	
	    	int score = Integer.parseInt(content.trim());
	    	
	    	int userId = userSessionPool.getUserId(sessionKey);
	    	scoreManager.saveScore(userId, level, score);
				
			return new HttpResponse(HttpURLConnection.HTTP_OK, "");
				
	    } catch (NumberFormatException nfe) {
	    	return new HttpResponse(HttpURLConnection.HTTP_BAD_REQUEST, "INVALID SCORE PARAMETER");
	    } catch (UserNotFoundException unfe) {
	    	return new HttpResponse(HttpURLConnection.HTTP_INTERNAL_ERROR, "USER NOT FOUND FOR SESSION");
		}
	    
	}
	
	
	private String readRequestBody(InputStream requestBody) throws IOException {
		
		InputStreamReader input = new InputStreamReader(requestBody);
		BufferedReader buffer = new BufferedReader(input);
	    StringBuilder out = new StringBuilder();
	    String line;
	    while ((line = buffer.readLine()) != null) {
	    	out.append(line);
	    }
	    
	   return out.toString();
		
	}

	
}

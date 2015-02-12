package com.king.scoreserver;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.nio.ByteBuffer;
import java.util.List;

import com.king.scoreserver.RequestUtils.InvalidScoreInputException;
import com.king.scoreserver.ScoreManager.Highscore;
import com.king.scoreserver.ScoreManager.LevelNotFoundException;
import com.king.scoreserver.ScoreManager.UserNotFoundException;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

class RequestHandler implements HttpHandler {
	
	protected static final String LOGIN_PATH = "login";
	protected static final String HIGHSCORE_LIST_PATH = "highscorelist";
	
	private ScoreManager scoreManager;
	
	
	public RequestHandler() {
		scoreManager = new ScoreManager();
	}
	
	
	public void handle(HttpExchange exchange) throws IOException {
		
		String method = exchange.getRequestMethod();
		String path = exchange.getRequestURI().getPath();
		
		String[] pathElems = path.split("/");
		
		if(pathElems.length != 3) {
			httpResponse(exchange, HttpURLConnection.HTTP_NOT_FOUND, "404 - NOT FOUND");
			return;
		}
		
		String lastPathElem = pathElems[pathElems.length-1];
		
		if(method.equalsIgnoreCase("GET")) {
			
			if(lastPathElem.equalsIgnoreCase(LOGIN_PATH)) {
				
				try {
					
					int userId = Integer.parseInt(pathElems[1]);
					
					String sessionKey = scoreManager.getSessionKey(userId);
					httpResponse(exchange, HttpURLConnection.HTTP_OK, sessionKey);
					
				} catch(NumberFormatException nfe) {
					httpResponse(exchange, HttpURLConnection.HTTP_BAD_REQUEST, "MALFORMED USER ID");
				}
				
			} else if(lastPathElem.equalsIgnoreCase(HIGHSCORE_LIST_PATH)) {
				
				try {
					
					int levelId = Integer.parseInt(pathElems[1]);
					
					String highscoresCsv = highscoresToCsv(scoreManager.getHighscores(levelId));
					httpResponse(exchange, HttpURLConnection.HTTP_OK, "text/csv", highscoresCsv);
					
				} catch(LevelNotFoundException lnfe) {
					httpResponse(exchange, HttpURLConnection.HTTP_INTERNAL_ERROR, "LEVEL NOT FOUND");
				} catch(NumberFormatException nfe) {
					httpResponse(exchange, HttpURLConnection.HTTP_BAD_REQUEST, "MALFORMED LEVEL ID");
				}
				
				
			} else {
				
				httpResponse(exchange, HttpURLConnection.HTTP_NOT_FOUND, "404 - NOT FOUND");
				
			}
			
			
		} else if(method.equalsIgnoreCase("POST")) {
			
			String query = exchange.getRequestURI().getQuery();
			
			if(query == null || query.length() == 0) {
				httpResponse(exchange, HttpURLConnection.HTTP_NOT_FOUND, "404 - NOT FOUND");
				return;
			}
			
			String[] sessionKeyParam = query.split("=");
			
			if(sessionKeyParam.length != 2) {
				httpResponse(exchange, HttpURLConnection.HTTP_NOT_FOUND, "404 - NOT FOUND");
				return;
			}
			
			String sessionKey = sessionKeyParam[1];
			
			int levelId = -1;
			
			try {
				levelId = Integer.parseInt(pathElems[1]);
			} catch(NumberFormatException nfe) {
				httpResponse(exchange, HttpURLConnection.HTTP_BAD_REQUEST, "MALFORMED LEVEL ID");
				return;
			}
			
			System.out.println(sessionKey);
			Headers headers = exchange.getRequestHeaders();
			String contentType = headers.get("Content-Type").get(0);
			
			int score;
			
			if(contentType.equalsIgnoreCase("binary")) {
			
				try {
					
					score = RequestUtils.readScoreInput(exchange.getRequestBody());
					scoreManager.saveScore(sessionKey, levelId, score);
					
					httpResponse(exchange, HttpURLConnection.HTTP_OK, "");
					
				} catch(InvalidScoreInputException isie) {
					httpResponse(exchange, HttpURLConnection.HTTP_BAD_REQUEST, "INVALID SCORE PARAMETER");
				} catch(UserNotFoundException unfe) {
					httpResponse(exchange, HttpURLConnection.HTTP_INTERNAL_ERROR, "USER NOT FOUND FOR SESSION");
				}
				
			} else {
				
				BufferedReader reader = new BufferedReader(new InputStreamReader(exchange.getRequestBody()));
		        StringBuilder out = new StringBuilder();
		        String line;
		        while ((line = reader.readLine()) != null) {
		            out.append(line);
		        }
		        
		        String content = out.toString();
		        
		        try {
		        	
		        	score = Integer.parseInt(content.trim());
		        	scoreManager.saveScore(sessionKey, levelId, score);
					
					httpResponse(exchange, HttpURLConnection.HTTP_OK, "");
					
		        } catch(NumberFormatException nfe) {
		        	httpResponse(exchange, HttpURLConnection.HTTP_BAD_REQUEST, "INVALID SCORE PARAMETER");
		        } catch(UserNotFoundException unfe) {
					httpResponse(exchange, HttpURLConnection.HTTP_INTERNAL_ERROR, "USER NOT FOUND FOR SESSION");
				}
		        
			}
			
			
			
			
		} else {
			
			httpResponse(exchange, HttpURLConnection.HTTP_BAD_METHOD, "");
			
		}
		
		
	}

	
	private String highscoresToCsv(List<Highscore> levelHighscores) {
		
		String csv = "";
		
		for(Highscore highscore : levelHighscores) {
			csv += highscore.getUserId()+"="+highscore.getScore()+",";
		}
		
		return csv;
		
	}
	
	private void httpResponse(HttpExchange exchange, int responseCode, String content) throws IOException {
		httpResponse(exchange, responseCode, "text/plain", content);
	}

	private void httpResponse(HttpExchange exchange, int responseCode, String contentType, String content) throws IOException {
		
		byte[] response = content.getBytes();
		exchange.getResponseHeaders().set("Content-Type", contentType);
		exchange.sendResponseHeaders(responseCode, response.length);
		exchange.getResponseBody().write(response);
		exchange.close();
		
	}

		
}

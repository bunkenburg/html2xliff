package com.leninra.scoreserver;

import java.io.IOException;
import java.net.HttpURLConnection;

import com.sun.net.httpserver.HttpExchange;

public class ScoreServerExchange {
	
	private HttpExchange httpExchange;
	
	public ScoreServerExchange(HttpExchange httpExchange) {
		this.httpExchange = httpExchange;
	}
	
	public HttpResponse handle() throws IOException {
		
		ResponseBuilder responseBuilder = new ResponseBuilder();
		
		String method = httpExchange.getRequestMethod();
		String path = httpExchange.getRequestURI().getPath();
		
		String[] pathElems = path.split("/");
		
		if (pathElems.length != 3) {
			return new HttpResponse(HttpURLConnection.HTTP_NOT_FOUND, "404 - NOT FOUND");
		}
		
		String lastPathElem = pathElems[pathElems.length-1];
		
		if (method.equalsIgnoreCase("GET")) {
			
			if (lastPathElem.equalsIgnoreCase("login")) {
				
				return responseBuilder.login(pathElems);
				
			} else if (lastPathElem.equalsIgnoreCase("highscorelist")) {
				
				return responseBuilder.getHighscoreList(pathElems);
				
			} else {
				
				return new HttpResponse(HttpURLConnection.HTTP_NOT_FOUND, "404 - NOT FOUND");
				
			}
			
			
		} else if (method.equalsIgnoreCase("POST")) {
			
			String query = httpExchange.getRequestURI().getQuery();
			return responseBuilder.saveScore(pathElems, query, httpExchange.getRequestBody());
		    
		} else {
			
			return new HttpResponse(HttpURLConnection.HTTP_BAD_METHOD, "");
			
		}
		
		
	}
	
	
	public void sendHttpResponse(HttpResponse httpResponse) throws IOException {
		
		byte[] response = httpResponse.getContent().getBytes();
		httpExchange.getResponseHeaders().set("Content-Type", httpResponse.getContentType());
		httpExchange.sendResponseHeaders(httpResponse.getCode(), response.length);
		httpExchange.getResponseBody().write(response);
		httpExchange.close();
		
	}

	
}

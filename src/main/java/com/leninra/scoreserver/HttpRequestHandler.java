package com.leninra.scoreserver;
import java.io.IOException;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

class HttpRequestHandler implements HttpHandler {
	
	public void handle(HttpExchange exchange) throws IOException {
		
		ScoreServerExchange scoreServerExchange = new ScoreServerExchange(exchange);
		scoreServerExchange.sendHttpResponse(scoreServerExchange.handle());
		
	}
	
	
}

package com.leninra.scoreserver;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.sun.net.httpserver.HttpServer;

public class ScoreServer {
	
	static final Logger log = LogManager.getLogger(ScoreServer.class.getName());
	
	public static void main(String[] args) {
		
		try {
			new ScoreServer().start("/");
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
		
	}
	
	public HttpServer start(String context) throws IOException {
		
		log.debug("Hello, King!");
		
		InetSocketAddress addr = new InetSocketAddress(8888);
		
		HttpServer server = HttpServer.create(addr, 0);
		server.createContext(context, new HttpRequestHandler());
			
		server.setExecutor(Executors.newCachedThreadPool());
			
		server.start();
			
		log.debug("Server running on port 8888.");
			
		return server;
		  
		
	}
	
	
}

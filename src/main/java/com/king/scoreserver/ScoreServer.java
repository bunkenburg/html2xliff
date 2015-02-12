package com.king.scoreserver;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.sun.net.httpserver.HttpServer;

public class ScoreServer {
	
	static final Logger log = LogManager.getLogger(ScoreServer.class.getName());
	
	public static void main(String[] args) {
		
		log.debug("Hello, King!");
		
		InetSocketAddress addr = new InetSocketAddress(8888);
		
		try {
			
			HttpServer server = HttpServer.create(addr, 0);
			server.createContext("/", new RequestHandler());
			 
			// Using default CachedThreadPool Executor for optimal 
			// concurrent request performance without reinventing the wheel
			server.setExecutor(Executors.newCachedThreadPool());
			
			server.start();
			
			log.debug("Server is listening.");
		  
		} catch(IOException ioe) {
			ioe.printStackTrace();
		}
		
	}
	
	
}

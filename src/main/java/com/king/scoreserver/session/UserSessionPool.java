package com.king.scoreserver.session;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class UserSessionPool implements Runnable {
	

	static final Logger log = LogManager.getLogger(UserSessionPool.class.getName());
	
	private Map<String, UserSession> sessionsBySessionKey;
	private Map<Integer, UserSession> sessionsByUserId;
	
	private long expirationTime;
	
	private Thread thread;
	private boolean keepRunning = true;
	
	private SecureRandom random = new SecureRandom();
	
	
	public UserSessionPool(long expirationTime) {
		
		log.trace("Initializing user session pool.");
		
		sessionsBySessionKey = new HashMap<String, UserSession>();
		sessionsByUserId = new HashMap<Integer, UserSession>();
		
		this.expirationTime = expirationTime;
		
		thread = new Thread(this);
		thread.start();
		
	}
	
	
	public String addUserSession(int userId) {
		
		Iterator<String> sessionKeys = sessionsBySessionKey.keySet().iterator();
		
		UserSession existingUserSession = null;
		
		while (sessionKeys.hasNext()) {
			
			String sessionKey = sessionKeys.next();
			UserSession userSession = sessionsBySessionKey.get(sessionKey);
			
			if(userSession.getUserId() == userId) {
				existingUserSession = userSession;
				break;
			}
			
		}
		
		if(existingUserSession != null) {
			
			log.trace("Retrieved existing session key.");
			
			return existingUserSession.getSessionKey();
			
		} else {
			
			String sessionKey = new BigInteger(130, random).toString(32);
			UserSession userSession = new UserSession(userId, sessionKey);
			sessionsBySessionKey.put(sessionKey, userSession);
			sessionsByUserId.put(userId, userSession);
			
			log.debug("Created new session "+sessionKey);
			
			return userSession.getSessionKey();
			
		}
		
	}
	
	
	public int getUserId(String sessionKey) {
		
		UserSession userSession = sessionsBySessionKey.get(sessionKey);
		
		if(userSession != null) {
			return userSession.getUserId();
		} else {
			return -1;
		}
			
	}
	
	
	public void expireUserSessions() {
		
		long now = System.currentTimeMillis();
		
		ArrayList<String> sessionKeys = new ArrayList<String>(sessionsBySessionKey.keySet());//.iterator();
		
		for (int i=0; i<sessionKeys.size(); i++) {
			
			String sessionKey = sessionKeys.get(i);
			UserSession userSession = sessionsBySessionKey.get(sessionKey);
			
			log.trace(sessionKey+": "+(now - userSession.getCreationTime()));
			
			if(now - userSession.getCreationTime() >= expirationTime) {
				sessionsBySessionKey.remove(sessionKey);
				sessionsByUserId.remove(userSession.getUserId());
				log.debug("Session with key "+sessionKey+" has expired.");
			}
			
		}
		
	}
	
	
	public void run() {
		
		while(keepRunning) {
			
			try {
				
				expireUserSessions();
				
				thread.sleep(1000L);
				
			} catch(InterruptedException ie) {
				ie.printStackTrace();
			}
			
		}
		
	}
	
	
	public void destroy() {
		keepRunning = false;
	}
	
	
	
	public long getExpirationTime() {
		return expirationTime;
	}


	public void setExpirationTime(long expirationTime) {
		this.expirationTime = expirationTime;
	}


	class UserSession {
		
		private final int userId;
		private final String sessionKey;
		
		private final long creationTime;
		
		public UserSession(int userId, String sessionKey) {
			this.userId = userId;
			this.sessionKey = sessionKey;
			this.creationTime = System.currentTimeMillis();
		}
		
		
		public int getUserId() {
			return userId;
		}
		
		public String getSessionKey() {
			return sessionKey;
		}
		
		public long getCreationTime() {
			return creationTime;
		}
		
	}
	

}

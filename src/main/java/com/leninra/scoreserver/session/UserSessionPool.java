package com.leninra.scoreserver.session;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class UserSessionPool {
	
	static final Logger log = LogManager.getLogger(UserSessionPool.class.getName());
	
	private Map<String, UserSession> sessionsBySessionKey;
	private Map<Integer, UserSession> sessionsByUserId;
	
	private long expirationTime;
	
	private SecureRandom random = new SecureRandom();
	
	private static UserSessionPool userSessionPool;
	
	public static UserSessionPool getInstance(long expirationTime) {
		
		if (userSessionPool == null) {
			
			synchronized (UserSessionPool.class) {
				if (userSessionPool == null) {
					userSessionPool = new UserSessionPool(expirationTime);
				}
			}
			
		}
		
		return userSessionPool;
		
	}
	
	private UserSessionPool(long expirationTime) {
		
		log.trace("Initializing user session pool.");
		
		sessionsBySessionKey = new ConcurrentHashMap<String, UserSession>();
		sessionsByUserId = new ConcurrentHashMap<Integer, UserSession>();
		
		this.expirationTime = expirationTime;
		
	}
	
	
	public String getSessionKey(int userId) {
		
		UserSession userSession = sessionsByUserId.get(userId);
		
		if (userSession != null && !userSessionExpired(userSession)) {
			
			log.trace("Retrieved existing session key.");
			return userSession.getSessionKey();
			
		} else {
			
			userSession = createUserSession(userId); 
			return userSession.getSessionKey();
			
		}
		
	}
	
	
	private UserSession createUserSession(int userId) {
		
		String sessionKey = new BigInteger(130, random).toString(32);
		UserSession userSession = new UserSession(userId, sessionKey);
		sessionsBySessionKey.put(sessionKey, userSession);
		sessionsByUserId.put(userId, userSession);
		
		log.debug("Created new session "+sessionKey);
		
		return userSession;
		
	}
	
	
	public int getUserId(String sessionKey) {
		
		UserSession userSession = sessionsBySessionKey.get(sessionKey);
		
		if (userSession != null && !userSessionExpired(userSession)) {
			return userSession.getUserId();
		} else {
			return -1;
		}
			
	}
	
	private boolean userSessionExpired(UserSession userSession) {
		
		boolean isExpired = userSession.getCreationTime() <= System.currentTimeMillis() - expirationTime;
		
		if (isExpired) {
			sessionsByUserId.remove(userSession.getUserId());
			sessionsBySessionKey.remove(userSession.getSessionKey());
		}
		
		return isExpired;
		
	}
	
	
	public long getExpirationTime() {
		return expirationTime;
	}


	public void setExpirationTime(long expirationTime) {
		this.expirationTime = expirationTime;
	}

	

}

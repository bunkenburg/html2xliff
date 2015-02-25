package com.leninra.scoreserver.session;

public class UserSession {
	
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

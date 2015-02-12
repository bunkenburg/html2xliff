package com.king.scoreserver.session;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class UserSessionPoolTest {
	
	private UserSessionPool userSessionPool;
	
	private int USER_ID_1 = 10001;
	private int USER_ID_2 = 10002;
	
	private String SESSION_KEY_1;
	private String SESSION_KEY_2;
	
	@Before
	public void setUp() {
		
		userSessionPool = new UserSessionPool(60000);
		
		SESSION_KEY_1 = userSessionPool.addUserSession(USER_ID_1);
		SESSION_KEY_2 = userSessionPool.addUserSession(USER_ID_2);	
		
	}
	
	
	@Test
	public void testAddUserSession() {
		
		String sessionKey = userSessionPool.addUserSession(10003);
		
		assertNotNull(sessionKey);
		assertTrue(sessionKey.length() > 0);
		
	}
	
	
	@Test
	public void testGetUserId() {
		
		int existingSessionUserId = userSessionPool.getUserId(SESSION_KEY_1);
		int inexistingSessionUserId = userSessionPool.getUserId("invalidSessionKey");
		
		assertEquals(USER_ID_1, existingSessionUserId);
		assertEquals(-1, inexistingSessionUserId);
		
	}
	
	
	@Test
	public void testExpireUserSessions() {
		
		userSessionPool.setExpirationTime(0);
		assertEquals(0, userSessionPool.getExpirationTime());
		userSessionPool.expireUserSessions();
		int inexistingSessionUserId = userSessionPool.getUserId(SESSION_KEY_1);
		assertEquals(-1, inexistingSessionUserId);
		
	}
	
	
	@After
	public void tearDown() {
		userSessionPool.destroy();
	}
	
	
}

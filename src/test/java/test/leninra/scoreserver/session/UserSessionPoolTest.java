package test.leninra.scoreserver.session;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import test.leninra.utils.SeparateClassloaderTestRunner;

import com.leninra.scoreserver.session.UserSessionPool;

@RunWith(SeparateClassloaderTestRunner.class)
public class UserSessionPoolTest {
	
	private UserSessionPool userSessionPool;
	
	private int testUserId1 = 10001;
	private int testUserId2 = 10002;
	
	private String testSessionKey1;
	private String testSessionKey2;
	
	@Before
	public void setUp() {
		
		userSessionPool = UserSessionPool.getInstance(60000);
		
		testSessionKey1 = userSessionPool.getSessionKey(testUserId1);
		testSessionKey2 = userSessionPool.getSessionKey(testUserId2);	
		
		System.out.println(testSessionKey1+", "+testSessionKey2);
		
	}
	
	
	@Test
	public void testAddUserSession() {
		
		String sessionKey = userSessionPool.getSessionKey(10003);
		
		assertNotNull(sessionKey);
		assertTrue(sessionKey.length() > 0);
		
	}
	
	
	@Test
	public void testGetUserId() {
		
		int existingSessionUserId = userSessionPool.getUserId(testSessionKey1);
		int inexistingSessionUserId = userSessionPool.getUserId("invalidSessionKey");
		
		assertEquals(testUserId1, existingSessionUserId);
		assertEquals(-1, inexistingSessionUserId);
		
	}
	
	
	@Test
	public void testGetExpiredUserSessions() {
		
		String oldSessionKey = userSessionPool.getSessionKey(testUserId1);
		String newSessionKey = userSessionPool.getSessionKey(testUserId1);
		assertEquals(oldSessionKey, newSessionKey);
		
		userSessionPool.setExpirationTime(0);
		assertEquals(0, userSessionPool.getExpirationTime());
		
		newSessionKey = userSessionPool.getSessionKey(testUserId1);
		assertNotSame(oldSessionKey, newSessionKey);
		
	}
	
	
	
}

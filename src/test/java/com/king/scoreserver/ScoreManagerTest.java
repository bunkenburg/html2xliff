package com.king.scoreserver;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.king.scoreserver.ScoreManager.Highscore;
import com.king.scoreserver.ScoreManager.LevelNotFoundException;
import com.king.scoreserver.ScoreManager.UserNotFoundException;

public class ScoreManagerTest {
	
	private ScoreManager scoreManager;
	
	private int USER_ID_1 = 10001;
	private int USER_ID_2 = 10002;
	
	private int LEVEL_ID_1 = 20001;
	private int LEVEL_ID_2 = 20002;
	
	private String SESSION_KEY_1;
	private String SESSION_KEY_2;
	
	@Before
	public void setUp() throws UserNotFoundException {
		
		scoreManager = new ScoreManager();
		
		SESSION_KEY_1 = scoreManager.getSessionKey(USER_ID_1);
		SESSION_KEY_2 = scoreManager.getSessionKey(USER_ID_2);
		
		for(int i=0; i<100; i+=2) {
			scoreManager.saveScore(SESSION_KEY_1, LEVEL_ID_1, i);
			scoreManager.saveScore(SESSION_KEY_2, LEVEL_ID_2, i+1);
			scoreManager.saveScore(SESSION_KEY_1, LEVEL_ID_1, 100+i);
			scoreManager.saveScore(SESSION_KEY_2, LEVEL_ID_2, 100+i+1);
		}
		
	}
	
	@Test
	public void testGetSessionKey() {
		
		String sessionKey = scoreManager.getSessionKey(USER_ID_1);
		assertEquals(SESSION_KEY_1, sessionKey);
		
	}
	
	@Test
	public void testGetHighscores() throws LevelNotFoundException {
		
		List<Highscore> level1Highscores = scoreManager.getHighscores(LEVEL_ID_1);
		
		assertEquals(15, level1Highscores.size());
		assertEquals(196, level1Highscores.get(1).getScore());
		assertEquals(USER_ID_1, level1Highscores.get(1).getUserId());
		
	}
	
	@Test(expected=LevelNotFoundException.class)
    public void testGetHighscoresForInvalidLevel() throws InterruptedException, LevelNotFoundException {
		scoreManager.getHighscores(20003);
	}
	
	@Test
    public void testSaveScore() throws InterruptedException, UserNotFoundException, LevelNotFoundException {
		
		scoreManager.saveScore(SESSION_KEY_1, LEVEL_ID_1, 197);
		scoreManager.saveScore(SESSION_KEY_1, LEVEL_ID_2, 198);
		
		List<Highscore> level1Highscores = scoreManager.getHighscores(LEVEL_ID_1);
		List<Highscore> level2Highscores = scoreManager.getHighscores(LEVEL_ID_2);
		
		assertEquals(15, level1Highscores.size());
		assertEquals(15, level2Highscores.size());
		
		assertEquals(197, level1Highscores.get(1).getScore());
		assertEquals(198, level2Highscores.get(1).getScore());
		
	}
	
	
	@Test(expected=UserNotFoundException.class)
    public void testSaveScoreForInvalidSession() throws InterruptedException, UserNotFoundException {
		scoreManager.saveScore("invalidSessionKey", LEVEL_ID_1, 197);
	}
	
	
}

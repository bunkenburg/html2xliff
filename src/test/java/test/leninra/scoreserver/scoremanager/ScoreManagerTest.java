package test.leninra.scoreserver.scoremanager;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import test.leninra.utils.SeparateClassloaderTestRunner;

import com.leninra.scoreserver.scoremanager.Highscore;
import com.leninra.scoreserver.scoremanager.ScoreManager;
import com.leninra.scoreserver.scoremanager.ScoreManager.LevelNotFoundException;
import com.leninra.scoreserver.scoremanager.ScoreManager.UserNotFoundException;
import com.leninra.scoreserver.session.UserSessionPool;

@RunWith(SeparateClassloaderTestRunner.class)
public class ScoreManagerTest {
	
	private ScoreManager scoreManager;
	private UserSessionPool userSessionPool;
	
	private int USER_ID_1 = 10001;
	private int USER_ID_2 = 10002;
	private int USER_ID_3 = 10003;
	
	private int LEVEL_1 = 20001;
	private int LEVEL_2 = 20002;
	
	
	@Before
	public void setUp() throws UserNotFoundException {
		
		scoreManager = ScoreManager.getInstance();
		
		scoreManager.saveScore(USER_ID_1, LEVEL_1, 100);
		scoreManager.saveScore(USER_ID_2, LEVEL_1, 101);
		scoreManager.saveScore(USER_ID_1, LEVEL_2, 200);
		scoreManager.saveScore(USER_ID_2, LEVEL_2, 201);
		
	}
	
	
	@Test
	public void testGetHighscores() throws LevelNotFoundException {
		
		List<Highscore> level1Highscores = scoreManager.getHighscores(LEVEL_1, 15);
		
		assertEquals(2, level1Highscores.size());
		assertEquals(100, level1Highscores.get(1).getScore());
		assertEquals(USER_ID_1, level1Highscores.get(1).getUserId());
		
	}
	
	@Test
	public void testGetHighscoresAsCsv() throws LevelNotFoundException {
		
		String scoresCsv = scoreManager.getHighscoresAsCsv(LEVEL_1, 15);
		assertEquals(USER_ID_2+"=101,"+USER_ID_1+"=100,", scoresCsv);
		
	}
	
	
	@Test(expected=LevelNotFoundException.class)
    public void testGetHighscoresForInvalidLevel() throws InterruptedException, LevelNotFoundException {
		scoreManager.getHighscores(20003, 15);
	}
	
	@Test
    public void testSaveScore() throws InterruptedException, UserNotFoundException, LevelNotFoundException {
		
		scoreManager.saveScore(USER_ID_1, LEVEL_1, 102);
		scoreManager.saveScore(USER_ID_2, LEVEL_1, 103);
		scoreManager.saveScore(USER_ID_1, LEVEL_1, 104);
		
		scoreManager.saveScore(USER_ID_3, LEVEL_2, 202);
		scoreManager.saveScore(USER_ID_2, LEVEL_2, 203);
		scoreManager.saveScore(USER_ID_1, LEVEL_2, 204);
		
		List<Highscore> level1Highscores = scoreManager.getHighscores(LEVEL_1, 15);
		List<Highscore> level2Highscores = scoreManager.getHighscores(LEVEL_2, 15);
		
		assertEquals(2, level1Highscores.size());
		assertEquals(3, level2Highscores.size());
		
		assertEquals(104, level1Highscores.get(0).getScore());
		assertEquals(204, level2Highscores.get(0).getScore());
		assertEquals(USER_ID_1, level2Highscores.get(0).getUserId());
		
	}
	
	
	@Test(expected=UserNotFoundException.class)
    public void testSaveScoreForInvalidSession() throws InterruptedException, UserNotFoundException {
		scoreManager.saveScore(-1, LEVEL_1, 197);
	}
	
	
}

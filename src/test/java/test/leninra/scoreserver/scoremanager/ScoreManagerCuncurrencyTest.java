package test.leninra.scoreserver.scoremanager;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import test.leninra.utils.ConcurrentAssert;
import test.leninra.utils.ConcurrentTestSuite;
import test.leninra.utils.SeparateClassloaderTestRunner;

import com.leninra.scoreserver.scoremanager.Highscore;
import com.leninra.scoreserver.scoremanager.ScoreManager;
import com.leninra.scoreserver.scoremanager.ScoreManager.LevelNotFoundException;
import com.leninra.scoreserver.scoremanager.ScoreManager.UserNotFoundException;
import com.leninra.scoreserver.session.UserSessionPool;

@RunWith(SeparateClassloaderTestRunner.class)
public class ScoreManagerCuncurrencyTest {
	
	private List<Integer> scores;
	private ScoreManager scoreManager;
	private UserSessionPool userSessionPool;
	private List<String> sessionKeys;
	
	private int scoresCount = 1000;
	
	@Before
	public void setUp() {
		
		userSessionPool = UserSessionPool.getInstance(10 * 60 * 1000);
		scoreManager = ScoreManager.getInstance();
		
		sessionKeys = new ArrayList<String>();
		scores = new ArrayList<Integer>();
		
		for(int i=1; i<=scoresCount; i++) {
			sessionKeys.add(userSessionPool.getSessionKey(i));
		}
		
	}
	
	
	@Test
    public void testConcurrentScoreSubmissions() throws InterruptedException {
		
		List<ScoreSubmission> scoreSubmissions = new ArrayList<ScoreSubmission>();
		
		for(int i=0; i<scoresCount; i++) {
			scores.add(i);
		}
		
		for(int i=0; i<scoresCount; i++) {
			
			int scoreIndex = (int)Math.floor(Math.random()*scores.size());
			int randomScore = scores.get(scoreIndex);
			scores.remove(scoreIndex);
			
			scoreSubmissions.add(new ScoreSubmission(scoreManager, userSessionPool.getUserId(sessionKeys.get(i)), 1, randomScore));
			
		}
		
		ScoreSubmissionTestSuite testSuite = new ScoreSubmissionTestSuite(scoreSubmissions);
		
		ConcurrentAssert.assertConcurrent("Concurrent score submissions", testSuite, 10000);
		
	}
	
	
	private class ScoreSubmissionTestSuite extends ConcurrentTestSuite {
		
		public ScoreSubmissionTestSuite(List<? extends Runnable> runnables) {
			super(runnables);
		}
		
		public void finalAssert() {
			
			try {
				
				List<Highscore> levelHighscores = scoreManager.getHighscores(1, 15);
				assertEquals(15, levelHighscores.size());
				
				for(int i=0; i<levelHighscores.size(); i++) {
					assertEquals(levelHighscores.get(i).getScore(), scoresCount-1-i);
				}
				
			} catch(LevelNotFoundException lnfe) {
				lnfe.printStackTrace();
			}
				
		
		}
		
	}
	
	
	private class ScoreSubmission implements Runnable {
		
		private ScoreManager scoreManager;
		private int userId;
		private int levelId;
		private int score;
		
		public ScoreSubmission(ScoreManager scoreManager, int userId, int levelId, int score) {
			this.scoreManager = scoreManager;
			this.userId = userId;
			this.levelId = levelId;
			this.score = score;
		}
		
		public void run() {
			try {
				scoreManager.saveScore(userId, levelId, score);
			} catch(UserNotFoundException unfe) {
				unfe.printStackTrace();
			}
		}
		
		
	}
	
	
	
}

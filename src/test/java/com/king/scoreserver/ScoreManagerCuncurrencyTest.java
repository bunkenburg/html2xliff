package com.king.scoreserver;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.junit.Test;

import com.king.scoreserver.ScoreManager.Highscore;
import com.king.scoreserver.ScoreManager.LevelNotFoundException;
import com.king.scoreserver.ScoreManager.UserNotFoundException;

public class ScoreManagerCuncurrencyTest {
	
	private ScoreManager scoreManager = new ScoreManager();
	
	@Test
    public void testConcurrentScoreSubmissions() throws InterruptedException {
		
		String sessionKey = scoreManager.getSessionKey(12345);
		
		List<ScoreSubmission> scoreSubmissions = new ArrayList<ScoreSubmission>();
		
		List<Integer> scores = new ArrayList<Integer>();
		for(int i=0; i<1000; i++) {
			scores.add(i);
		}
		
		for(int i=0; i<1000; i++) {
			
			int scoreIndex = (int)Math.floor(Math.random()*scores.size());
			int randomScore = scores.get(scoreIndex);
			scores.remove(scoreIndex);
			
			scoreSubmissions.add(new ScoreSubmission(scoreManager, sessionKey, 1, randomScore));
			
		}
		
		ScoreSubmissionTestSuite testSuite = new ScoreSubmissionTestSuite(scoreSubmissions);
		
		assertConcurrent("Concurrent score submissions", testSuite, 10000);
		
	}
	
	
	private class ScoreSubmissionTestSuite extends RunnableTestSuite {
		
		public ScoreSubmissionTestSuite(List<? extends Runnable> runnables) {
			super(runnables);
		}
		
		public void finalAssert() {
			
			try {
				
				List<Highscore> levelHighscores = scoreManager.getHighscores(1);
					
				assertEquals(15, levelHighscores.size());
					
				for(int i=0; i<15; i++) {
					assertEquals(levelHighscores.get(i).getScore(), 999-i);
				}
				
			} catch(LevelNotFoundException lnfe) {
				lnfe.printStackTrace();
			}
				
		
		}
		
	}
	
	
	private abstract class RunnableTestSuite {
		
		final List<? extends Runnable> runnables;
		
		public RunnableTestSuite(List<? extends Runnable> runnables) {
			this.runnables = runnables;
		}
		
		public List<? extends Runnable> getRunnables() {
			return runnables;
		}
		
		public abstract void finalAssert();
		
	}
	
	
	private class ScoreSubmission implements Runnable {
		
		private ScoreManager scoreManager;
		private String sessionKey;
		private int levelId;
		private int score;
		
		public ScoreSubmission(ScoreManager scoreManager, String sessionKey, int levelId, int score) {
			this.scoreManager = scoreManager;
			this.sessionKey = sessionKey;
			this.levelId = levelId;
			this.score = score;
		}
		
		public void run() {
			try {
				scoreManager.saveScore(sessionKey, levelId, score);
			} catch(UserNotFoundException unfe) {
				unfe.printStackTrace();
			}
		}
		
		
	}
	
	
	
	public static void assertConcurrent(final String message, final RunnableTestSuite testSuite, final int maxTimeoutSeconds) throws InterruptedException {
        
		List<? extends Runnable> runnables = testSuite.getRunnables();
		
		final int numThreads = runnables.size();
        final List<Throwable> exceptions = Collections.synchronizedList(new ArrayList<Throwable>());
        final ExecutorService threadPool = Executors.newFixedThreadPool(numThreads);
        
        try {
        
        	final CountDownLatch allExecutorThreadsReady = new CountDownLatch(numThreads);
        	final CountDownLatch afterInitBlocker = new CountDownLatch(1);
        	final CountDownLatch allDone = new CountDownLatch(numThreads);
        	
        	for (final Runnable submittedTestRunnable : runnables) {
            
        		threadPool.submit(new Runnable() {
        			
        			public void run() {
        				allExecutorThreadsReady.countDown();
        				try {
        					afterInitBlocker.await();
        					submittedTestRunnable.run();
        				} catch (final Throwable e) {
        					exceptions.add(e);
        				} finally {
        					allDone.countDown();
        				}
        			}
        			
        		});
        		
        	}
        	
        	// wait until all threads are ready
        	assertTrue("Timeout initializing threads! Perform long lasting initializations before passing runnables to assertConcurrent", allExecutorThreadsReady.await(runnables.size() * 10, TimeUnit.MILLISECONDS));
        	
        	// start all test runners
        	afterInitBlocker.countDown();
        	
        	assertTrue(message +" timeout! More than" + maxTimeoutSeconds + "seconds", allDone.await(maxTimeoutSeconds, TimeUnit.SECONDS));
        
        } finally {
        	
        	threadPool.shutdownNow();
        	
        }
        
        assertTrue(message + "failed with exception(s)" + exceptions, exceptions.isEmpty());
        
        testSuite.finalAssert();
        
      
	}
	
	
}

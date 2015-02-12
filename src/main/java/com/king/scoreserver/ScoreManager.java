package com.king.scoreserver;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.king.scoreserver.session.UserSessionPool;

public class ScoreManager {
	
	static final Logger log = LogManager.getLogger(ScoreManager.class.getName());
	
	private final long SESSION_EXPIRATION_TIME = 10 * 60 * 1000;
	private final int MAX_HIGHSCORE_LIST_SIZE = 15;
	
	private UserSessionPool userSessionPool;
	
	private Map<Integer, List<Highscore>> highscores;
	
	public ScoreManager() {
		
		userSessionPool = new UserSessionPool(SESSION_EXPIRATION_TIME);
		highscores = new HashMap<Integer, List<Highscore>>();
		
	}
	
	
	public String getSessionKey(int userId) {
		
		String sessionKey = userSessionPool.addUserSession(userId);
		
		return sessionKey;
	
	}
	
	
	public synchronized void saveScore(String sessionKey, int levelId, int score) throws UserNotFoundException {
		
		log.debug("Score submitted from "+sessionKey+": "+levelId+"="+score);
		
		int userId = userSessionPool.getUserId(sessionKey);
		
		if(userId == -1) {
			throw new UserNotFoundException();
		}
		
		List<Highscore> levelHighscores = highscores.get(levelId);
		
		if(levelHighscores == null) {
			
			levelHighscores = new ArrayList<Highscore>();
			levelHighscores.add(new Highscore(userId, score));
			highscores.put(levelId, levelHighscores);
			
			log.trace("Create new levelHighscores "+levelHighscores);
			
		} else {
			
			boolean added = false;
			
			for (int i=0; i<levelHighscores.size(); i++) {
				if(score > levelHighscores.get(i).getScore()) {
					if(levelHighscores.size() == MAX_HIGHSCORE_LIST_SIZE) {
						levelHighscores.remove(levelHighscores.size()-1);
					}
					levelHighscores.add(i, new Highscore(userId, score));
					added = true;
					break;
				}
			}
			
			if(!added && levelHighscores.size() < MAX_HIGHSCORE_LIST_SIZE) {
				levelHighscores.add(new Highscore(userId, score));
			}
		
		}
		
		log.trace("Scores in list "+levelHighscores+": "+levelHighscores.size());
		
	}
	
	
	public List<Highscore> getHighscores(int levelId) throws LevelNotFoundException {
		
		List<Highscore> levelHighscores = highscores.get(levelId);
		
		if(levelHighscores != null) {
			return levelHighscores;
		} else {
			throw new LevelNotFoundException();
		}
		
	}
	
	

	class Highscore {
		
		private final int userId;
		private final int score;
		
		public Highscore(int userId, int score) {
			this.userId = userId;
			this.score = score;
		}

		public int getUserId() {
			return userId;
		}

		public int getScore() {
			return score;
		}
		
		@Override
		public String toString() {
			return userId+"="+score;
		}
		
	}
	
	public class UserNotFoundException extends Exception {}
	public class LevelNotFoundException extends Exception {}
	
	
}

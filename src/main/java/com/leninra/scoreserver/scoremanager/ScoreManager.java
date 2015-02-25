package com.leninra.scoreserver.scoremanager;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ScoreManager {
	
	static final Logger log = LogManager.getLogger(ScoreManager.class.getName());
	
	private Map<Integer, ConcurrentHashMap<Integer, Highscore>> highscoresByLevel;
	
	private static ScoreManager scoreManager;
	
	public static ScoreManager getInstance() {
		
		if (scoreManager == null) {
		
			synchronized (ScoreManager.class) {
				if (scoreManager == null) {
					scoreManager = new ScoreManager();
				}
			}
			
		}
		
		return scoreManager;
		
	}
	
	private ScoreManager() {
		highscoresByLevel = new ConcurrentHashMap<Integer, ConcurrentHashMap<Integer, Highscore>>();
	}
	
	public void saveScore(int userId, int levelId, int score) throws UserNotFoundException {
		
		if (userId == -1) {
			throw new UserNotFoundException();
		}
		
		Map<Integer, Highscore> levelHighscores = getSynchronizedLevelHighscores(levelId);
		
		if (!levelHighscores.containsKey(userId) || score > levelHighscores.get(userId).getScore()) {
			levelHighscores.put(userId, new Highscore(userId, score));
		}
		
	}
	
	
	public List<Highscore> getHighscores(int level, int count) throws LevelNotFoundException {
		
		Map<Integer, Highscore> levelHighscores = getSynchronizedLevelHighscores(level, false);
		
		if (levelHighscores == null) {
			throw new LevelNotFoundException();
		}
		
		TreeMap<Integer, Highscore> sortedMap = new TreeMap<Integer, Highscore>(new HighscoreComparator(levelHighscores));
		sortedMap.putAll(levelHighscores);
		
		Iterator<Integer> iterator = sortedMap.keySet().iterator();
		List<Highscore> highscoresList = new ArrayList<Highscore>();
		
		int index = 0;
		while (iterator.hasNext() && ++index <= count) {
			highscoresList.add(levelHighscores.get(iterator.next()));
		}
		
		return highscoresList;
		
		
	}
	
	
	private Map<Integer, Highscore> getSynchronizedLevelHighscores(int level) {
		return getSynchronizedLevelHighscores(level, true);
	}
	
	private Map<Integer, Highscore> getSynchronizedLevelHighscores(int level, boolean create) {
		
		ConcurrentHashMap<Integer, Highscore> levelHighscores = highscoresByLevel.get(level);
		
		if (levelHighscores == null && create) {
			
			synchronized (highscoresByLevel) {
				
				if(levelHighscores == null) {
					levelHighscores = new ConcurrentHashMap<Integer, Highscore>();
					highscoresByLevel.put(level, levelHighscores);
				}
				
				
			}
			
		}
		
		return levelHighscores;
		
	}

	
	public String getHighscoresAsCsv(int level, int count) throws LevelNotFoundException {
		
		StringBuilder builder = new StringBuilder();
		
		List<Highscore> levelHighscores = getHighscores(level, count);
		
		for (Highscore highscore : levelHighscores) {
			builder.append(highscore);
			builder.append(",");
		}
		
		return builder.toString();
		
	}


	public class UserNotFoundException extends Exception {}
	public class LevelNotFoundException extends Exception {}
	
	
}

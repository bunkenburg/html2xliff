package com.leninra.scoreserver.scoremanager;

import java.util.Comparator;
import java.util.Map;

public class HighscoreComparator implements Comparator<Integer> {
	 
	private Map<Integer, Highscore> highscoresByUser;
	
	public HighscoreComparator(Map<Integer, Highscore> highscoresByUser) {
		this.highscoresByUser = highscoresByUser;
	}
	
	public int compare(Integer user1, Integer user2) {
		return highscoresByUser.get(user2).getScore() - highscoresByUser.get(user1).getScore();
	}
	
}

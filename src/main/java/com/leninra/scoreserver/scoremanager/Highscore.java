package com.leninra.scoreserver.scoremanager;

public class Highscore {
	
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

package test.leninra.utils;

import java.util.List;

public abstract class ConcurrentTestSuite {
	
	final List<? extends Runnable> runnables;
	
	public ConcurrentTestSuite(List<? extends Runnable> runnables) {
		this.runnables = runnables;
	}
	
	public List<? extends Runnable> getRunnables() {
		return runnables;
	}
	
	public abstract void finalAssert();
	
}

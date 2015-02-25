package test.leninra.scoreserver;

import static org.junit.Assert.*;

import java.io.IOException;

import org.junit.Test;

import com.leninra.scoreserver.ScoreServer;
import com.sun.net.httpserver.HttpServer;

public class ScoreServerTest {
	
	@Test
	public void testServerStart() throws IOException {
		
		HttpServer server = new ScoreServer().start("/");
		assertNotNull(server);
		
	}
	
}

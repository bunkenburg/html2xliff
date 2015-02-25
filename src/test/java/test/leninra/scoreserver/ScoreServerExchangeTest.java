package test.leninra.scoreserver;

import static org.junit.Assert.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;

import org.junit.Before;
import org.junit.Test;

import com.leninra.scoreserver.HttpResponse;
import com.leninra.scoreserver.ScoreServerExchange;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;

public class ScoreServerExchangeTest {
	
    private int testUserId = 12345;
    private String testLevel = "2";
    private String testScore = "202";
    private String testSessionKey;

    @Before
    public void setUp() throws IOException {
    	
        testSessionKey = login(testUserId).getContent();
        updateScore(testLevel, testScore, testSessionKey);
        
    }

    @Test
    public void testWrongPathRequest() throws IOException {
        
    	HttpExchange httpExchange = mock(HttpExchange.class);
        given(httpExchange.getRequestMethod()).willReturn("GET");
        given(httpExchange.getRequestURI()).willReturn(URI.create("/12345/67890/login"));
        
        HttpResponse response = new ScoreServerExchange(httpExchange).handle();
        
        assertEquals(HttpURLConnection.HTTP_NOT_FOUND, response.getCode());
        
    }
    
    
    @Test
    public void testWrongMethodRequest() throws IOException {
        
    	HttpExchange httpExchange = mock(HttpExchange.class);
        given(httpExchange.getRequestMethod()).willReturn("DELETE");
        given(httpExchange.getRequestURI()).willReturn(URI.create("/12345/login"));
        
        HttpResponse response = new ScoreServerExchange(httpExchange).handle();
        
        assertEquals(HttpURLConnection.HTTP_BAD_METHOD, response.getCode());
        
    }
    
    
    @Test
    public void testLogin() throws IOException {
        
        HttpResponse response = login(67890);
        
        assertEquals(HttpURLConnection.HTTP_OK, response.getCode());
        assertTrue(response.getContent().length() > 0);
        
    }
    
    
    @Test
    public void testLoginWithBadUserId() throws IOException {
        
    	HttpExchange httpExchange = mock(HttpExchange.class);
        given(httpExchange.getRequestMethod()).willReturn("GET");
        given(httpExchange.getRequestURI()).willReturn(URI.create("/xxxx/login"));
        
        HttpResponse response = new ScoreServerExchange(httpExchange).handle();
        
        assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, response.getCode());
        
    }
    
    @Test
    public void testGetScores() throws IOException {
        
        HttpResponse response = getScores(testLevel);
        
        assertEquals(HttpURLConnection.HTTP_OK, response.getCode());
        assertEquals(response.getContentType(), "text/csv");
        assertEquals(response.getContent(), testUserId+"="+testScore+",");
        
    }
    
    @Test
    public void testGetScoresWithInvlidLevelParam() throws IOException {
        
    	HttpResponse response = getScores("xxx");
        
        assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, response.getCode());
        assertEquals(response.getContent(), "MALFORMED LEVEL ID");
        
    }
    
    @Test
    public void testGetScoresOfInexistingLevel() throws IOException {
        
    	HttpResponse response = getScores("999");
        
        assertEquals(HttpURLConnection.HTTP_INTERNAL_ERROR, response.getCode());
        assertEquals(response.getContent(), "LEVEL NOT FOUND");
        
    }
    
    
    @Test
    public void testUpdateScore() throws IOException {
        
        HttpResponse response = updateScore("1", "101", testSessionKey);
        System.out.println(response.getContent());
        assertEquals(HttpURLConnection.HTTP_OK, response.getCode());
        
    }
    
    @Test
    public void testUpdateScoreWithInvalidRequest() throws IOException {
        
    	HttpExchange httpExchange = mock(HttpExchange.class);
        given(httpExchange.getRequestMethod()).willReturn("POST");
        given(httpExchange.getRequestURI()).willReturn(URI.create("/1/score?xxx="+testSessionKey));
        
        HttpResponse response = new ScoreServerExchange(httpExchange).handle();
        assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, response.getCode());
        assertEquals("INVALID REQUEST", response.getContent());
        
    }
    
    
    @Test
    public void testUpdateScoreWithInvalidSessionKey() throws IOException {
        
        HttpResponse response = updateScore("1", "101", "xxxxxxxxxxxx");
        
        assertEquals(HttpURLConnection.HTTP_INTERNAL_ERROR, response.getCode());
        assertEquals("USER NOT FOUND FOR SESSION", response.getContent());
        
    }
    
    @Test
    public void testUpdateScoreWithInvalidScoreValue() throws IOException {
        
        HttpResponse response = updateScore("1", "xxx", testSessionKey);
        
        assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, response.getCode());
        assertEquals("INVALID SCORE PARAMETER", response.getContent());
        
    }
    
    @Test
    public void testUpdateScoreWithInvalidLevelValue() throws IOException {
        
        HttpResponse response = updateScore("xxx", "101", testSessionKey);
        
        assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, response.getCode());
        assertEquals("MALFORMED LEVEL PARAMETER", response.getContent());
        
    }
    
    
    @Test
    public void testHttpResponseWriteWithoutIOException() throws IOException {
        
    	HttpExchange httpExchange = mock(HttpExchange.class);
        given(httpExchange.getRequestMethod()).willReturn("GET");
        given(httpExchange.getRequestURI()).willReturn(URI.create("/12345/login"));
        given(httpExchange.getResponseHeaders()).willReturn(new Headers());
        given(httpExchange.getResponseBody()).willReturn(new ByteArrayOutputStream());
        
        ScoreServerExchange exchange = new ScoreServerExchange(httpExchange);
        HttpResponse response = exchange.handle();
        exchange.sendHttpResponse(response);
        
    }
    
    
    private HttpResponse login(int userId) throws IOException {
    	
    	HttpExchange httpExchange = mock(HttpExchange.class);
    	given(httpExchange.getRequestMethod()).willReturn("GET");
        given(httpExchange.getRequestURI()).willReturn(URI.create("/"+userId+"/login"));
        
        return new ScoreServerExchange(httpExchange).handle();
        
    }
    
    private HttpResponse getScores(String level) throws IOException {
    	
    	HttpExchange httpExchange = mock(HttpExchange.class);
    	given(httpExchange.getRequestMethod()).willReturn("GET");
        given(httpExchange.getRequestURI()).willReturn(URI.create("/"+level+"/highscorelist"));
        
        return new ScoreServerExchange(httpExchange).handle();
        
    }
    
    public HttpResponse updateScore(String level, String score, String sessionKey) throws IOException {
        
    	HttpExchange httpExchange = mock(HttpExchange.class);
        given(httpExchange.getRequestMethod()).willReturn("POST");
        given(httpExchange.getRequestURI()).willReturn(URI.create("/"+level+"/score?sessionkey="+sessionKey));
        given(httpExchange.getRequestBody()).willReturn(new ByteArrayInputStream(score.getBytes()));
        
        return new ScoreServerExchange(httpExchange).handle();
        
        
    }
    
}

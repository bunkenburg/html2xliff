package com.king.scoreserver;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

import org.junit.Test;

import com.king.scoreserver.RequestUtils.InvalidScoreInputException;

public class RequestUtilsTest {

	@Test
    public void testReadScoreInput() throws InvalidScoreInputException {
		
		ByteBuffer dbuf = ByteBuffer.allocate(4);
		dbuf.putInt(67890);
		
		InputStream is = new ByteArrayInputStream(dbuf.array());
		int score = RequestUtils.readScoreInput(is);
		
		assertEquals(score, 67890);
		
    }
	
	
	@Test(expected=InvalidScoreInputException.class)
    public void testReadInvalidScoreInput() throws IOException, InvalidScoreInputException {
		
		ByteBuffer dbuf = ByteBuffer.wrap(new byte[] {});
		InputStream is = new ByteArrayInputStream(dbuf.array());
		
		RequestUtils.readScoreInput(is);
		
    }
	
	
	
	
}

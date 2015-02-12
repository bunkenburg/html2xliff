package com.king.scoreserver;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

public class RequestUtils {
	
	public static int readScoreInput(InputStream is) throws InvalidScoreInputException {
		
		try {
		
			byte[] buf = new byte[4];
			int length = is.read(buf);
			
			if(length != 4) {
				throw new IOException();
			}
			
			return ByteBuffer.wrap(buf).getInt();
			
		} catch(IOException ioe) {
			
			throw new InvalidScoreInputException();
			
		}
        
	}
	
	
	static class InvalidScoreInputException extends Exception {}
	
}

package com.leninra.scoreserver;

public class HttpResponse {
	
	private int code;
	private String contentType;
	private String content;
	
	public HttpResponse(int code, String content) {
		this(code, "text/plain", content);
	}
	
	public HttpResponse(int code, String contentType, String content) {
		this.code = code;
		this.contentType = contentType;
		this.content = content;
	}

	public int getCode() {
		return code;
	}

	public String getContentType() {
		return contentType;
	}

	public String getContent() {
		return content;
	}


}

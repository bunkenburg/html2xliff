package com.leninra.html2xliff;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.Node;
import org.dom4j.io.SAXReader;
import org.dom4j.tree.DefaultText;

/**
 * Utility class for generating XLIFF XML from HTML content.
 * 
 * @author vhalme
 *
 */
public class Html2Xliff {
	
	static final Logger logger = LogManager.getLogger(Html2Xliff.class.getName());
	
	public static void main(String[] args) {
		
		logger.debug("Hello, Netcentric!");
		
		String html = "<html><h1>This</h1> is<br/><b>really <i>super</i> duper</b> important.</html>";
		
		if(args.length > 0) {
			html = args[0];
		}
		
		Html2Xliff html2xliff = new Html2Xliff();
		
		try {
		
			String xliff = html2xliff.escape(html);
			System.out.println(xliff);
			
		} catch(IOException ioe) {
			ioe.printStackTrace();
		} catch(DocumentException ioe) {
			ioe.printStackTrace();
		}
		
		
	}
	
	
	/**
	 * Escapes HTML code into subset of XLIFF XML.
	 * 
	 * @param html Input HTML. Must be correctly formatted, otherwise an exception will occur.
	 * 
	 * @return XLIFF XML as a String
	 *  
	 * @throws IOException
	 * @throws DocumentException
	 * 
	 */
	public String escape(String html) throws IOException, DocumentException {
		
		// Add wrapping <html> tags in case they're missing
		if(!html.trim().toLowerCase().startsWith("<html>")) {
			html = "<html>" + html + "</html>";
		}
		
		SAXReader reader = new SAXReader();
		InputStream htmlInput = new ByteArrayInputStream(html.getBytes(StandardCharsets.UTF_8));
		Document document = reader.read(htmlInput);
		Element htmlRoot = document.getRootElement();
		
		Document source = DocumentHelper.createDocument();
        Element xliffRoot = source.addElement("source");
        
        // Start recursive DOM traversal from root element
		readElement(htmlRoot, xliffRoot, 0, 0);
		
		String xliff = xliffRoot.asXML();
		
		return xliff;
		
	}
	
	
	/**
	 * Recursive method that traverses the DOM tree of the input HTML document and generates a new one
	 * based on XLIFF specifications.
	 * 
	 * @param htmlElem Input html element
	 * @param xliffElem Root XLIFF element
	 * 
	 * @param id Unique ID
	 * @param rid Pairing ID matching in corresponding bpt and ept elements
	 * 
	 */
	private void readElement(Element htmlElem, Element xliffElem, int id, int rid) {
		
		List<Node> content = htmlElem.content();
		
		// Read each content node within an element
		for(Node node : content) {
			
			// For an element node, generate opening and closing XLIFF elems and 
			// continue the recursion if necessary
			if(node instanceof Element) {
			    
				Element element = (Element)node;
				String elemName = element.getName();
				
				String elemHtml = element.asXML();
				
				// Skip recursion in case of single-tag element
				if(elemHtml.endsWith("/>")) {
							
					xliffElem.addElement("ph")
						.addAttribute("id", new Integer(id++).toString())
						.addText(elemHtml);
							
							
				} else {
					
					// Add an opening XLIFF elem
					Element bptElem = 
							xliffElem.addElement("bpt")
								.addAttribute("id", new Integer(id++).toString())
								.addAttribute("rid", new Integer(rid).toString())
								.addText("<"+elemName+">");
					
					// Recurse into child nodes
					readElement(element, xliffElem, id++, ++rid);
					
					// Add a closing XLIFF elem
					Element eptElem = 
							xliffElem.addElement("ept")
								.addAttribute("id", new Integer(id++).toString())
								.addAttribute("rid", new Integer(--rid).toString())
								.addText("</"+elemName+">");
					
					if(elemName.equals("b")) {
						bptElem.addAttribute("ctype", "bold");
						eptElem.addAttribute("ctype", "bold");
					}
					
					rid++;
					
				}
				
			// In case of a text node, simply add to the root XLIFF elem and skip recursion
			} else if(node instanceof DefaultText) {
				
				DefaultText text = (DefaultText)node;
				logger.trace(text.asXML());
				xliffElem.addText(text.asXML());
				
			}
			
		}
		
		
	}
	
	
}

package test.leninra.html2xliff;

import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Set;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.Node;
import org.dom4j.io.SAXReader;
import org.junit.Before;
import org.junit.Test;

import com.leninra.html2xliff.Html2Xliff;

public class Html2XliffTest {

	private Html2Xliff html2xliff;
	
	@Before
	public void setUp() {
		html2xliff = new Html2Xliff();
	}
	
	
	/**
	 * Test the validity of escaped DOM formatting
	 * 
	 * @throws IOException
	 * @throws DocumentException
	 */
	@Test
	public void testEscapedDom() throws IOException, DocumentException {
		
		String html = "<html>This is <b>very</b><br/>important</html>";
		String xliff = html2xliff.escape(html);
		
		SAXReader reader = new SAXReader();
		InputStream htmlInput = new ByteArrayInputStream(xliff.getBytes(StandardCharsets.UTF_8));
		Document document = reader.read(htmlInput);
		Element xliffRoot = document.getRootElement();
		
		assertEquals(xliffRoot.getName(), "source");
		assertEquals(6, xliffRoot.nodeCount());
		
		Element bpt = xliffRoot.element("bpt");
		assertEquals("<b>", bpt.getText());
		assertEquals("bold", bpt.attribute("ctype").getText());
		
		Element ept = xliffRoot.element("ept");
		assertEquals("</b>", ept.getText());
		assertEquals("bold", ept.attribute("ctype").getText());
		
		Element ph = xliffRoot.element("ph");
		assertEquals("<br/>", ph.getText());
		
	}
	
	
	/**
	 * Test that all XLIFF elem ids are unique
	 * 
	 * @throws IOException
	 * @throws DocumentException
	 */
	@Test
	public void testDistinctXliffIds() throws IOException, DocumentException {
		
		String html = "<html>This is <b>very <i>super</i><br/>duper</b> important</html>";
		String xliff = html2xliff.escape(html);
		
		SAXReader reader = new SAXReader();
		InputStream htmlInput = new ByteArrayInputStream(xliff.getBytes(StandardCharsets.UTF_8));
		Document document = reader.read(htmlInput);
		Element xliffRoot = document.getRootElement();
		
		assertEquals(xliffRoot.getName(), "source");
		assertEquals(10, xliffRoot.nodeCount());
		
		Set<String> ids = new HashSet<String>();
		for(int i=0; i<xliffRoot.nodeCount(); i++) {
			Node node = xliffRoot.node(i);
			if(node instanceof Element) {
				Element elem = (Element)node;
				String elemId = elem.attribute("id").getText();
				assertFalse(ids.contains(elemId));
				ids.add(elemId);
			}
		}
		
		
	}
	
	/**
	 * Test that XLIFF elem rids are the same for corresponding opening and closing elems
	 * 
	 * @throws IOException
	 * @throws DocumentException
	 */
	@Test
	public void testXliffRids() throws IOException, DocumentException {
		
		String html = "<html>This is <b>very <i>super</i> duper</b> important</html>";
		String xliff = html2xliff.escape(html);
		
		SAXReader reader = new SAXReader();
		InputStream htmlInput = new ByteArrayInputStream(xliff.getBytes(StandardCharsets.UTF_8));
		Document document = reader.read(htmlInput);
		Element xliffRoot = document.getRootElement();
		
		assertEquals(xliffRoot.getName(), "source");
		assertEquals(9, xliffRoot.nodeCount());
		
		Element bOpen = (Element)xliffRoot.node(1);
		Element bClose = (Element)xliffRoot.node(7);
		Element iOpen = (Element)xliffRoot.node(3);
		Element iClose = (Element)xliffRoot.node(5);
		
		assertEquals(bOpen.attribute("rid").getText(), bClose.attribute("rid").getText());
		assertEquals(iOpen.attribute("rid").getText(), iClose.attribute("rid").getText());
		
		
	}
	
	/**
	 * Test that an exception is thrown when the input document is not valid
	 * 
	 * @throws IOException
	 * @throws DocumentException
	 */
	@Test(expected=DocumentException.class)
	public void testInvalidHtml() throws IOException, DocumentException {
		
		String html = "<html>This is <b>very <i>super</i> duper important</html>";
		String xliff = html2xliff.escape(html);
		
	}
	
	
	/**
	 * Test that the input is wrapped in enclosing &lt;html&gt;&lt/html&gt; tags if these are missing
	 *  
	 * @throws IOException
	 * @throws DocumentException
	 */
	public void testMissingHtmlNodeWrapper() throws IOException, DocumentException {
		
		String html = "This is <b>very <i>super</i> duper</b> important";
		String xliff = html2xliff.escape(html);
		
		SAXReader reader = new SAXReader();
		InputStream htmlInput = new ByteArrayInputStream(xliff.getBytes(StandardCharsets.UTF_8));
		Document document = reader.read(htmlInput);
		Element xliffRoot = document.getRootElement();
		
		assertEquals(xliffRoot.getName(), "source");
		assertEquals(9, xliffRoot.nodeCount());
		
	}
	
}

package base;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

import com.bms.base.io.TextIo;

public class TestTextIo {
	
	TextIo tio;
	
	@Before
	public void setup() {
		tio = new TextIo();
	}

	@Test
	public void testGetFileName() {
		String x = "src/main/resources/xanadu.txt";
		String fileName = tio.getFileName();
		assertEquals(x,fileName);
	}

}

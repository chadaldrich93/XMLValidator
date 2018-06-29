package xmlprocessing.xmlprocessing;

import java.io.PushbackReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ValidatorTest{

    /*@Test
    public void firstNameCharacterIsAlphabetic(){
        Validator v = new Validator();
        for (int i = 0; i < 256; i++){
        	v.setCurrentChar(i);
            if ( (i >= 65 && i <= 90) || (i >= 97 && i <= 122) || (i == 95)) 
                assertTrue(v.hasNameFirstChar());
	        else 
                assertFalse(v.hasNameFirstChar());
        }
    }*/
    
    /*@Test
    public void testAllNameCharacters() {
    	Validator v = new Validator();
    	for (int i = 0; i < 256; i++) {
    		v.setCurrentChar(i);
    		if (v.hasNameChar(true))
    			assertTrue(v.hasNameFirstChar());
    		else if ( (i == 46) || (i == 58) || (i >= 48 && i <= 57) )
    			assertTrue(v.hasNameChar(false));
    		else
    			assertFalse(v.hasNameChar(false));
    	}
    }*/
    
   /* @Test
    public void testValueCharacters() {
    	Validator v = new Validator();
    	for (int i = 0; i < 256; i++) {
    		v.setCurrentChar(i);
    		if ( (i == 38) || (i == 60) )
    			assertFalse(v.hasValueChar());
    		else
    			assertTrue(v.hasValueChar());
    	}
    }*/
    
    @Test
    public void testReadChar() throws FileNotFoundException, IOException{
		try {
		Validator v = new Validator();
		PushbackReader file = new PushbackReader(new FileReader
				       ("src/main/java/xmlprocessing/xmlprocessing/example1"),
				       Validator.REC_LINE_LENGTH);
		v.setTargetFile(file);
		assertEquals(v.readNextChar(),'g');
		}
		catch (FileNotFoundException e) { return;}
    }
    
    @Test
    public void testReadSpace() throws FileNotFoundException, IOException{
		try {
		Validator v = new Validator();
		PushbackReader file = new PushbackReader(new FileReader 
				       ("src/main/java/xmlprocessing/xmlprocessing/example2"),
				       Validator.REC_LINE_LENGTH);
		v.setTargetFile(file);
		v.readWhiteSpace();
		assertEquals(v.readNextChar(),'f');
		assertNotEquals(v.readNextChar(),'g');
		v.readWhiteSpace();
		assertEquals(v.readNextChar(),'g');
		v.readWhiteSpace();
		assertEquals(v.readNextChar(),'i');
		}
		catch (FileNotFoundException e) { return;}
    }
    
    @Test
    public void testReadTagName() throws FileNotFoundException, IOException{
    	try {
    		Validator v = new Validator();
    		PushbackReader file = new PushbackReader( new FileReader
    				   ("src/main/java/xmlprocessing/xmlprocessing/example3"),
    				   Validator.REC_LINE_LENGTH);
    		v.setTargetFile(file);
    		String a = v.readTagName();
    		assertEquals(a,"aa");
    		v.readWhiteSpace();
    		String b = v.readTagName();
    		assertEquals(b,"a");
    		v.readNextChar();
    		v.readWhiteSpace();
    		//Next line attempts to read invalid name start
    		//String c = v.readTagName();
    		v.readNextChar();
    		v.readNextChar();
    		v.readWhiteSpace();
    		//Next line attempts to read invalid name character after start
    		//String d = v.readTagName();
    		v.readNextChar();
    		v.readNextChar();
    		v.readWhiteSpace();
    		//Next line reads character valid for name, but not start
    		//String e = v.readTagName();
    		v.readNextChar();
    		v.readNextChar();
    		v.readWhiteSpace();
    		String f = v.readTagName();
    		assertEquals(f,"a9");
    		}
    		catch (FileNotFoundException e) { return;}
        }
    
    @Test
    public void testReferenceLengths() {
    	Validator v = new Validator();
    	assertEquals(v.entityReferenceLength('&', 'g', 't', ';', '0','0'),4);
    	assertEquals(v.entityReferenceLength('&', 'l', 't', ';', '0','0'),4);
    	assertEquals(v.entityReferenceLength('&', 'a', 'm', 'p', ';','0'),5);
    	assertEquals(v.entityReferenceLength('&', 'a', 'p', 'o', 's',';'),6);
    	assertEquals(v.entityReferenceLength('&', 'q', 'u', 'o', 't',';'),6);
    	assertEquals(v.entityReferenceLength('t','h','i','n','g','s'),0);
    }
    
    @Test
    public void testCheckReference() throws FileNotFoundException, IOException{
        try {
        Validator v = new Validator();
        PushbackReader file = new PushbackReader(new FileReader
        		       ("src/main/java/xmlprocessing/xmlprocessing/example4"),
        		       Validator.REC_LINE_LENGTH);
        v.setTargetFile(file);
        assertEquals(v.checkEntityReference(),4);
        v.readNextChar();
        v.readNextChar();
        v.readNextChar();
        v.readNextChar();
        assertEquals(v.checkEntityReference(),5);
        v.readNextChar();
        v.readNextChar();
        v.readNextChar();
        v.readNextChar();
        v.readNextChar();
        assertEquals(v.checkEntityReference(),6);
        }
        catch (FileNotFoundException e) { return;}
    }
    
    @Test
    public void testReadReference() throws FileNotFoundException, IOException{
		try {
		Validator v = new Validator();
		PushbackReader file = new PushbackReader(new FileReader
				       ("src/main/java/xmlprocessing/xmlprocessing/example4"),
				       Validator.REC_LINE_LENGTH);
		v.setTargetFile(file);
		int first = v.checkEntityReference();
		assertEquals(v.readEntityReference(first),"&lt;");
		int second = v.checkEntityReference();
		assertEquals(v.readEntityReference(second),"&amp;");
		int third = v.checkEntityReference();
		assertEquals(v.readEntityReference(third),"&quot;");
		}
		catch (FileNotFoundException e) { return;}
    }
  
    @Test
    public void testReadValue() throws FileNotFoundException, IOException{
        try {
        Validator v = new Validator();
        PushbackReader file = new PushbackReader(new FileReader
        		       ("src/main/java/xmlprocessing/xmlprocessing/example5"),
        		       Validator.REC_LINE_LENGTH);
        v.setTargetFile(file);
        assertEquals(v.readValue(false,'"'),"asdf");
        v.readNextChar();
        assertEquals(v.readValue(false,'"'),"a&lt;");
        //Next line will exit with error message, & must be
        //part of entity reference
        //assertEquals(v.readValue(false,'"'),"a&");
        }
        catch (FileNotFoundException e) { return;}
    }
    
   @Test
    public void testReadAttributeValue() throws FileNotFoundException, IOException{
        try {
        Validator v = new Validator();
        PushbackReader file = new PushbackReader(new FileReader
        		       ("src/main/java/xmlprocessing/xmlprocessing/example6"),
        		       Validator.REC_LINE_LENGTH);
        v.setTargetFile(file);
        //next line would exit program, due to < in attribute value 
        //assertEquals(v.readValue(true,'"'),"asdf");
        v.readNextChar();
        v.readNextChar();
        v.readNextChar();
        v.readNextChar();
        v.readNextChar();
        assertEquals(v.readValue(true,'"'),"a&lt;");
        //Next line will exit with error message, & must be
        //part of entity reference
        //assertEquals(v.readValue(),"a&");
        }
        catch (FileNotFoundException e) { return;}
    }
   
   @Test
   public void testReadAttribute() throws FileNotFoundException, IOException{
       try {
       Validator v = new Validator();
       PushbackReader file = new PushbackReader(new FileReader
       		       ("src/main/java/xmlprocessing/xmlprocessing/example7"),
       		       Validator.REC_LINE_LENGTH);
       v.setTargetFile(file);
       //next line exits program, attribute value must start with quote
       //assertEquals(v.readAttribute(),"abc=def");
       v.readNextChar();
       v.readNextChar();
       v.readNextChar();
       v.readNextChar();
       v.readNextChar();
       v.readNextChar();
       v.readNextChar();
       assertEquals(v.readAttribute(),"one='1'");
       //Next line will exit with error message, & must be
       //part of entity reference
       //assertEquals(v.readValue(),"a&");
       }
       catch (FileNotFoundException e) { return;}
   }
   
  @Test
   public void testReadTag() throws FileNotFoundException, IOException{
       try {
       Validator v = new Validator();
       PushbackReader file = new PushbackReader(new FileReader
       		       ("src/main/java/xmlprocessing/xmlprocessing/example8"),
       		       Validator.REC_LINE_LENGTH);
       v.setTargetFile(file);
       //next line exits program, tag must start with <
       //assertEquals(v.readTag("opening"),"x");
       v.readNextChar();
       //next line will exit program, closing tag must have /
       //assertEquals(v.readTag("closing"),"<ok>");
       assertEquals(v.readTag("opening"),"<ok>");
       //next line exits program, tag must start with name character
       //assertEquals(v.readTag("opening"),"< ok>");
       v.readNextChar();
       v.readNextChar();
       v.readNextChar();
       v.readNextChar();
       v.readNextChar();
       //System.out.println(v.readTag("closing"));
       assertEquals(v.readTag("closing"),"</ok>");
       assertEquals(v.readTag("opening"),"<ok >");
       assertEquals(v.readTag("opening"),"<a type=\"var\">");
       assertEquals(v.readTag("opening"),"<a type=\"var\"  >");
       //Next line exits program, closing tag can't have attributes
       //assertEquals(v.readTag("closing"),"</a type=\"var\">");
       }
       catch (FileNotFoundException e) { return;}
   }
  
  @Test
  public void testPageHasEntity() throws FileNotFoundException, IOException{
      try {
      Validator v = new Validator();
      PushbackReader file = new PushbackReader(new FileReader
      		       ("src/main/java/xmlprocessing/xmlprocessing/example9"),
      		       Validator.REC_LINE_LENGTH);
      v.setTargetFile(file);
      assertEquals(v.getCurrentColumn(),0);
      assertEquals(v.pageHasEntity("opening tag"),true);
      //repeat to ensure column hasn't changed, mark is in same position
      assertEquals(v.getCurrentColumn(),0);
      assertEquals(v.pageHasEntity("opening tag"),true);
      v.readOpeningTag();
      assertEquals(v.pageHasEntity("closing tag"),true);
      assertEquals(v.getCurrentColumn(),4);
      //again, check that column/mark havent changed
      assertEquals(v.pageHasEntity("closing tag"),true);
      assertEquals(v.getCurrentColumn(),4);
      v.readClosingTag();
      assertEquals(v.pageHasEntity("value"),true);
      assertEquals(v.getCurrentColumn(),9);
      //last time, check column/mark
      assertEquals(v.pageHasEntity("value"),true);
      assertEquals(v.getCurrentColumn(),9);
      }
      catch (IOException e) { e.printStackTrace();}
  }
  
  @Test
  public void testProcessTags() throws FileNotFoundException, IOException{
      try {
      Validator v = new Validator();
      PushbackReader file = new PushbackReader(new FileReader
      		       ("src/main/java/xmlprocessing/xmlprocessing/example10"),
      		       Validator.REC_LINE_LENGTH);
      v.setTargetFile(file);
      //next line exits program, tag must be opened before closing
      //v.processClosingTag();
      v.readClosingTag();
      v.processOpeningTag();
      v.processOpeningTag();
      //next line exits program, tag closed must be most recently opened
      //v.processClosingTag();
      v.readClosingTag();
      v.processClosingTag();
      v.processClosingTag();
      }
      catch (IOException e) { e.printStackTrace();}
  }
  
  @Test
  public void testTagsWithBreaks() throws FileNotFoundException, IOException{
      try {
      Validator v = new Validator();
      PushbackReader file = new PushbackReader(new FileReader
      		       ("src/main/java/xmlprocessing/xmlprocessing/example11"),
      		       Validator.REC_LINE_LENGTH);
      v.setTargetFile(file);
      assertEquals(v.readOpeningTag(),"<ok>");
      v.readWhiteSpace();
      assertEquals(v.readOpeningTag(),"<abc>");
      v.readWhiteSpace();
      assertEquals(v.readClosingTag(),"</abc>");
      v.readWhiteSpace();
      //assertEquals(v.readValue(false,'"'),"abcde");
      }
      catch (IOException e) { e.printStackTrace();}
  }
  
  @Test
  public void testParseTagName() throws FileNotFoundException, IOException{
      try {
      Validator v = new Validator();
      PushbackReader file = new PushbackReader(new FileReader
      		       ("src/main/java/xmlprocessing/xmlprocessing/example12"),
      		       Validator.REC_LINE_LENGTH);
      v.setTargetFile(file);
      String tag = v.readOpeningTag();
      String tagName = v.parseTagName(tag);
      assertEquals(tagName,"<ok>");
      //tag should hold the same values since characters after whitespace
      //should not be parsed
      tag = v.readOpeningTag();
      tagName = v.parseTagName(tag);
      assertEquals(tagName,"<ok>");
      }
      catch (IOException e) { e.printStackTrace();}
  }
}
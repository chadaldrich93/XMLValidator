package xmlprocessing.xmlprocessing;

import java.io.PushbackReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Stack;

//XML Validator
//Will cease validation when first error is encountered
//returning error message specifying line, column, and
//type of error encountered

//Validator requires all tags must start and end on the same line
public class Validator{
    
	static final int BACKSPACE = 10;
	static final int TAB = 11;
	static final int LINEFEED = 12;
	static final int VERTICAL_TAB = 13;
	static final int NEW_PAGE = 14;
	static final int CARRIAGE_RETURN = 15;
    static final int WHITESPACE = 32;
    static final int REC_LINE_LENGTH = 200;
    
    //column and line both start counting as 0
    private int currentColumn;
    private int currentLine;
    private char currentChar;
    
    private PushbackReader targetFile; 
    private Stack<String> nestedTags = new Stack<String>();

    public static void main(String[] args) throws FileNotFoundException, 
                                                  IOException{
        Validator v = new Validator();
        v.targetFile = new PushbackReader(new FileReader(args[0]),REC_LINE_LENGTH);
        v.processOpeningTag();
        v.readWhiteSpace();
        while (true){
            if (v.pageHasOpeningTag()) {
                v.processOpeningTag();
                v.readWhiteSpace();
            }
            if (v.pageHasClosingTag()) {
                v.processClosingTag();
                v.readWhiteSpace();
                if (v.nestedTags.empty())
                	break;
            }
	        if (v.pageHasValue()) {
                v.readValue(false,'"');
                v.readWhiteSpace();
	        }
        }
        System.out.println("Xml document is valid");
        v.targetFile.close();
        System.exit(0);    
    }
    
    int getCurrentColumn() {
    	return currentColumn;
    }
    
    int getCurrentLine() {
    	return currentLine;
    }
    
    char getCurrentChar() {
    	return currentChar;
    }
    
    void setTargetFile(PushbackReader file) {
    	targetFile = file;
    }
    
    boolean isNameFirstChar(char targetChar) {
        if ( ( ('A' <= targetChar) && (targetChar <= 'Z') ) ||
    	     ( ('a' <= targetChar) && (targetChar <= 'z') ) ||
    	     (  targetChar == '_')
    	   )
    	     return true;
        return false;
    }
    
    boolean isNameChar(char targetChar, boolean isFirstChar){
        if (isNameFirstChar(targetChar))
        	return true;
        if (!isFirstChar){
	        if ('0' <= targetChar && targetChar <= '9' || 
		        targetChar == ':'                      || 
                targetChar == '.' 
               )
		    return true;
	    }
        return false;
    }
    
    boolean isValueChar(char source){
        return ( (source != '&') && (source != '<'));
    }
    
    int entityReferenceLength(char firstChar, char secondChar, char thirdChar,
                              char fourthChar, char fifthChar, char sixthChar){
        //return value is size of char entity
        //0 means none was found
	    if (firstChar == '&' && secondChar == 'g' && thirdChar == 't' &&
	    	fourthChar == ';')
            return 4;
        if (firstChar == '&' && secondChar == 'l' && thirdChar == 't' &&
            fourthChar == ';')
        	return 4;
        if (firstChar == '&' && secondChar == 'a' && thirdChar == 'm' &&
            fourthChar == 'p' && fifthChar == ';')
            return 5;
        if (firstChar == '&' && secondChar == 'a' && thirdChar == 'p' &&
            fourthChar == 'o' && fifthChar == 's' && sixthChar == ';')
            return 6;
        if (firstChar == '&' && secondChar == 'q' && thirdChar == 'u' &&
            fourthChar == 'o' && fifthChar == 't' && sixthChar == ';')
            return 6;
        return 0;
    }

    char readNextChar() throws IOException{
        char nextChar = (char)targetFile.read();
        currentChar = nextChar;
        currentColumn += 1;
        return currentChar;
    }
    
    char checkNextChar() throws IOException{
    	char nextChar = (char)targetFile.read();
    	targetFile.unread(nextChar);
    	return nextChar;
    }
    
    String readWhiteSpace() throws IOException{
    	String whiteSpace = "";
        while ( (checkNextChar() == WHITESPACE)      || 
        		(checkNextChar() == BACKSPACE)       ||
        		(checkNextChar() == TAB)             ||
        		(checkNextChar() == LINEFEED)        ||
        		(checkNextChar() == VERTICAL_TAB)    ||
        		(checkNextChar() == NEW_PAGE)        ||
        		(checkNextChar() == CARRIAGE_RETURN) ||
        		(checkNextChar() == '\n')
        	  ) {
        	whiteSpace += readNextChar();
        	if ( currentChar == '\n'){
        		currentColumn = 0;
        		currentLine += 1;
        	}
        }
        return whiteSpace;
    }

    //all read functions return an empty string to indicate entity was not read
    String readName(String nameType) throws IOException{
    	String name = "";
        boolean isFirstChar = true;
        while (true){
            char nextChar = checkNextChar();
            boolean nextIsName = isNameChar(nextChar, isFirstChar);
            if (nextIsName){
            	name += readNextChar();
                isFirstChar = false;
                continue;
	        }
            else if ( ( (nextChar == '>') || (nextChar == ' ') 
            		  )  && 
            		    (nameType == "tag") 
            		)
                return name;
            else if ( (nextChar == '=') && (nameType == "attribute") )
            	return name;
	        else
	            return "";
        }
    }
    
    String readTagName() throws IOException{
        return readName("tag");
    }

    String readAttributeName() throws IOException{
       return readName("attribute");
    }
    
    int checkEntityReference() throws IOException{
    	int start = currentColumn;
    	char first = readNextChar();
    	char second = readNextChar();
    	char third = readNextChar();
    	char fourth = readNextChar();
    	char fifth = readNextChar();
    	char sixth = readNextChar();
    	char[] returnBuffer = {first, second, third, fourth, fifth, sixth};
    	targetFile.unread(returnBuffer);
    	currentColumn = start;
    	return entityReferenceLength(first,second,third,fourth,fifth,sixth);
    }
    
    String readEntityReference(int referenceLength) throws IOException{
    	String entityReference = "";
    	if (referenceLength >= 4) {
            char first  = readNextChar();
            char second = readNextChar();
            char third  = readNextChar();
            char fourth = readNextChar();
            entityReference = entityReference + first + second +
            		          third + fourth;
    	}
    	if (referenceLength >= 5) {
            char fifth = readNextChar();
            entityReference = entityReference + fifth;
    	}
    	if (referenceLength == 6) {
    		char sixth = readNextChar();
    		entityReference = entityReference + sixth;
    	}
        return entityReference;
    }
    
    //closingQuote character only matters if value being read is an attribute
    //must be ' or "
    String readValue(boolean isAttributeValue, char closingQuote)
    		         throws IOException{
        String value = "";
        int referenceLength;
        while (true){
            char nextChar = checkNextChar();
            if (isAttributeValue && (nextChar == closingQuote)) {
            	readNextChar();
            	return value;
            }
            else if (isValueChar(nextChar)) {
                readNextChar();
                value += currentChar;
                continue;
            }
            //< characters are not legal in values but would indicate the start
            //of a new tag, so < can't occur in attributes, but signifies 
            //end of other values
            else if (nextChar == '<') 
            	return (isAttributeValue)? "": value;
	        else if (nextChar == '&') {
	        	referenceLength = checkEntityReference();
                if (referenceLength == 0)
                	return "";
		        value += readEntityReference(referenceLength);
                continue;
	        }
	        else //value not properly terminated
	        	exitWithErrorMessage("unterminated value");
        }
    }
    
    String readAttributeValue() throws IOException{
    	String attributeValue = "";
    	char quote = (currentChar == '"')? '"' : '\'';
        attributeValue += quote;
        attributeValue += readValue(true, quote);
        attributeValue += quote;
        return attributeValue;
    }
    
    String readAttribute() throws IOException{
        String attribute = "";
        attribute += readAttributeName();
        attribute += readNextChar();
        if (currentChar != '=')
        	return "";
        readNextChar();
        if (currentChar != '"' && currentChar != '\'')
            return "";
        attribute += readAttributeValue();
        return attribute;
    }
    
    String readAttributesIntoTag(String tag) throws IOException{
	    while (checkNextChar() != '>') {
            tag += readAttribute();
            tag += readWhiteSpace();
        }
	    return tag;
    }
   
    //modify to make sure it doesnt go on forever
    void ensureTagDoesNotBeginWithWhiteSpace() throws IOException{
	    if (readWhiteSpace() != "")
	 	   exitWithErrorMessage("tag whitespace");
    }
   
    String readTag(String tagType) throws IOException{
	    String tag = "";
        if (checkNextChar() != '<') {
            return "";
        }
        tag += readNextChar();
        if (checkNextChar() == '/') {
    	    if (tagType == "opening") {
    	 	    restoreEntity(tag);
    	 	    return "";
    	    }
    	    else
    	        tag += readNextChar();
        }
        //tags can't have whitespace before the name
        ensureTagDoesNotBeginWithWhiteSpace();
        tag += readTagName();
        tag += readWhiteSpace();
        if ( (tagType == "closing") && (checkNextChar() != '>') ) {
    	    restoreEntity(tag);
    	    return "";
        }
        tag = readAttributesIntoTag(tag);
        tag += readNextChar();
        return tag;   
    }
    
    String parseTagName(String tag) {
        String tagName = "";
        boolean whitespaceInTag = false;
        for (int index = 0; index < tag.length(); index++) {
        	if (tag.charAt(index) != ' ')
        		tagName += tag.charAt(index);
        	else {
        		whitespaceInTag = true;
        		break;
        	}
        }
        //if tag had no whitespace, > will have been added
        //otherwise, it must be added
        if (whitespaceInTag)
        	tagName += '>';
        return tagName;
    }
    
    String readOpeningTag() throws IOException{
        return readTag("opening");
    }

    String readClosingTag() throws IOException{
        return readTag("closing");
    }
    
    boolean pageHasEntity(String entityType) throws IOException{
    	//read page to check for entity
        //reading page changes current column
        //so column is saved, then reset
        String entity = "";
        int startColumn = currentColumn;
    	switch (entityType){
    	case "opening tag":
    	    entity = readOpeningTag();
    	    break;
    	case "closing tag":
    		entity = readClosingTag();
    		break;
    	case "value":
    		entity = readValue(false,'"');
    		break;
    	}
        currentColumn = startColumn;
        restoreEntity(entity);
        return (entity != "");
    }
    
    //only used to check output during testing
    String checkEntity(String entityType) throws IOException{
        String entity = "";
        int startColumn = currentColumn;
	    switch (entityType){
	    case "opening tag":
	        entity = readOpeningTag();
	        break;
	    case "closing tag":
		    entity = readClosingTag();
		    break;
	    case "value":
		    entity = readValue(false,'"');
		    break;
	    }
        currentColumn = startColumn;
        restoreEntity(entity);
        return entity;
    }
    
    void restoreEntity(String entity) throws IOException{
    	char[] pushbackEntity;
        pushbackEntity = entity.toCharArray();
        targetFile.unread(pushbackEntity);
    }
    
    boolean pageHasOpeningTag() throws IOException{
        return pageHasEntity("opening tag");
    } 
    
    boolean pageHasClosingTag() throws IOException{
        return pageHasEntity("closing tag");
    }
    
    boolean pageHasValue() throws IOException{
       return pageHasEntity("value");
    }

    void popTag(String tagName){
        if (nestedTags.search(tagName) > 1)
            exitWithErrorMessage("invalid pop");
	    else if (nestedTags.search(tagName) == -1)
            exitWithErrorMessage("no tag");
        else //search(tagName) == 1
            nestedTags.pop(); 
    }
    
    void processOpeningTag() throws IOException{
        String tag = readOpeningTag();
        String tagName = parseTagName(tag);
        nestedTags.push(tagName);
    }

    void processClosingTag() throws IOException{
        String closingTag = readClosingTag();
        String tag = closingTag.replace("/","");
        popTag(tag);
    }

    void printErrorLocation(){
        System.out.println("At line " + currentLine + " " + 
                            "column " + currentColumn + " :");
    }
    
    void exitWithErrorMessage(String errorSymbol){
        printErrorLocation();
        switch (errorSymbol){
            case "invalid name":
                System.out.println("Invalid character for name");
                break;
	        case "invalid pop":
                System.out.println("Tag is improperly nested");
                break;
	        case "tag start":
                System.out.println("Tag must begin with opening bracket");
                break;
	        case "closing tag start":
	        	System.out.println("First character in closing tag must" +
	        			           " be forward slash");
                break;
	        case "closing tag end":
	        	System.out.println("Closing tag can't have attributes, must" +
	                               " end after tag name");
	        	break;
	        case "tag whitespace":
	        	System.out.println("Tag must not have whitespace before tag name");
	        	break;
	        case "tag end":
                System.out.println("Tag must end with closing bracket");
                break;
	        case "no tag":
                System.out.println("Tag was not found nested in stack");
                break;
            case "unfound reference":
                System.out.println("Entity reference not found");
                break;
            case "attribute format":
            	System.out.println("attribute consists of name, then '=', " + 
            			           "then value");
            	break;
            case "attribute start":
                System.out.println("Attribute must start with quotation mark");
                break;
	        case "value character":
                System.out.println("Illegal character found in value");
	        case "unterminated value":
	        	System.out.println("Value was not properly terminated");
                break;
	        case "unclosed root":
                System.out.println("Root element was not closed");
                break;
	        case "premature closure":
                System.out.println("Root element was closed before end");
                break;
	    }
        System.exit(1);
    }
}

package JackAnalyzer;

import java.io.File;
import java.io.PrintWriter;
import java.util.HashMap;



public class CompilationEngine {

    private PrintWriter xmlWriter;
    private final JackTokenizer tokenizer;
    private final PrintWriter vmWriter;
    private final SymbolTable symbolTable;
    private final HashMap<Character, String> xmlEscapeSymbols = new HashMap<Character, String>() {{
        put('&', "&amp;");
        put('<', "&lt;");
        put('>', "&gt;");
        put('"', "&quot;");
    }};

    public CompilationEngine(File inputFile, File xmlOutput,File vmOutput) throws Exception {

        this.xmlWriter = new PrintWriter(xmlOutput);
        this.vmWriter = new PrintWriter(vmOutput);
        this.symbolTable = new SymbolTable();
        this.tokenizer = new JackTokenizer(inputFile);
        compileClass();
    }

    public void compileClass() {
        // Implementation for compiling a class

            tokenizer.advance();
            xmlWriter.println("<class>");
            writeKeywordToken(JackTokenizer.Keyword.CLASS);

            tokenizer.advance();
            writeIdentifierToken();

            tokenizer.advance();
            writeSymbolToken('{');

            // Compile class variable declarations and subroutines
            tokenizer.advance();
            while (tokenizer.keyword() == JackTokenizer.Keyword.STATIC || tokenizer.keyword() == JackTokenizer.Keyword.FIELD) {
                compileClassVarDec();
                tokenizer.advance();
            }

           
            while(tokenizer.keyword() == JackTokenizer.Keyword.CONSTRUCTOR || 
                  tokenizer.keyword() == JackTokenizer.Keyword.FUNCTION || 
                  tokenizer.keyword() == JackTokenizer.Keyword.METHOD) {
                compileSubroutine();
                tokenizer.advance();
            }

            
            writeSymbolToken('}');

            // Write the closing </class> tag to the output file
            xmlWriter.println("</class>");

    }

    public void compileClassVarDec() {
            
        // Write the class variable declaration to the output file
        xmlWriter.println("<classVarDec>");
        writeKeywordToken(tokenizer.keyword());
       
        String kind = tokenizer.keyword().toString(); //Static or Field
        tokenizer.advance();


        if (JackTokenizer.isParameterTypeKeyword(tokenizer.keyword())) {
            writeKeywordToken(tokenizer.keyword());
        } else {
            writeIdentifierToken();
        }

        String type = tokenizer.tokenType() == JackTokenizer.TokenType.KEYWORD ? tokenizer.keyword().toString() : tokenizer.identifier();

        //Write the variable name to the output file
        tokenizer.advance();
        writeIdentifierToken();

        String name = tokenizer.identifier(); 

        symbolTable.define(name, type, SymbolTable.KIND.fromString(kind));
        tokenizer.advance();
        while(tokenizer.tokenType() == JackTokenizer.TokenType.SYMBOL && tokenizer.symbol() == ','){
            writeSymbolToken(',');
            tokenizer.advance();
            writeIdentifierToken();
            String nextName = tokenizer.identifier();
            symbolTable.define(nextName, type, SymbolTable.KIND.fromString(kind));
            tokenizer.advance();
        }

        writeSymbolToken(';');
               
        xmlWriter.println("</classVarDec>");
    }

    public void compileSubroutine() {

        xmlWriter.println("<subroutineDec>");
        //Wtite Field, Method or Constructor Keyword
        writeKeywordToken(tokenizer.keyword());
        
        //Return Type Keyword or Identifier
        tokenizer.advance();
        if (JackTokenizer.isParameterTypeKeyword(tokenizer.keyword())){
            writeKeywordToken(tokenizer.keyword());
        }else{
            writeIdentifierToken();
        }


        tokenizer.advance();
        //Subroutine Name Identifier
        writeIdentifierToken();
 
        tokenizer.advance();
        writeSymbolToken('(');
        xmlWriter.println("<parameterList>"); 
        tokenizer.advance();
        compileParameterList();
        xmlWriter.println("</parameterList>"); 
        writeSymbolToken(')');



  


        compileSubroutineBody();

        

        xmlWriter.println("</subroutineDec>");

    }

    public void compileParameterList() {

        if(!(JackTokenizer.isParameterTypeKeyword(tokenizer.keyword()) || tokenizer.tokenType() == JackTokenizer.TokenType.IDENTIFIER)){
            return;
        }
      
        if (JackTokenizer.isParameterTypeKeyword(tokenizer.keyword())){
           writeKeywordToken(tokenizer.keyword());
        } else{
            writeIdentifierToken();
        }
            
        tokenizer.advance();
        writeIdentifierToken();


        tokenizer.advance();
        while(tokenizer.tokenType() == JackTokenizer.TokenType.SYMBOL && tokenizer.symbol() == ','){
            
            writeSymbolToken(',');
            tokenizer.advance();
            if (JackTokenizer.isParameterTypeKeyword(tokenizer.keyword())){
                writeKeywordToken(tokenizer.keyword());
            } else if(tokenizer.tokenType() == JackTokenizer.TokenType.IDENTIFIER){
                writeIdentifierToken();
            }else{
                throw new IllegalArgumentException(
                        "Expected parameter type (int, char, boolean) or identifier, but found: " + tokenizer.getCurrentToken());
            }

            tokenizer.advance();
            writeIdentifierToken();

            tokenizer.advance();
        }
    }
    /**
     * Compiles a complete method function or constructor
     */
    public void compileSubroutineBody() {

      xmlWriter.println("<subroutineBody>");
      symbolTable.resetSubroutine(); // Reset the subroutine scope for each new subroutine
      tokenizer.advance();
      writeSymbolToken('{');
       
       tokenizer.advance();
       while (tokenizer.keyword() == JackTokenizer.Keyword.VAR) {
            compileVarDec();
            tokenizer.advance();
        }
        
        compileStatements();

        if (tokenizer.tokenType() == JackTokenizer.TokenType.SYMBOL && tokenizer.symbol() == '}') {
            writeSymbolToken('}');
        } else if (tokenizer.tokenType() == JackTokenizer.TokenType.KEYWORD && tokenizer.keyword() == JackTokenizer.Keyword.RETURN) {
            compileReturn();
        } else {
            throw new IllegalArgumentException(
                    "Expected '}' or 'return' in subroutine body, but found: " + tokenizer.getCurrentToken());
        }
        xmlWriter.println("</subroutineBody>");

    }

    public void compileVarDec() {
        // Write the variable declaration to the output file
        xmlWriter.println("<varDec>");
        writeKeywordToken(tokenizer.keyword());
        String kind = tokenizer.keyword().toString(); //Type of the variable

        tokenizer.advance();
        // Write the type to the output file
        if (JackTokenizer.isParameterTypeKeyword(tokenizer.keyword())) {
             writeKeywordToken(tokenizer.keyword());
        } else if (tokenizer.tokenType() == JackTokenizer.TokenType.IDENTIFIER) {
             writeIdentifierToken();
        } else {
            throw new IllegalArgumentException(
                            "Expected variable type (int, char, boolean) or identifier, but found: " + tokenizer.getCurrentToken());
        }

        String type = tokenizer.tokenType() == JackTokenizer.TokenType.KEYWORD ? tokenizer.keyword().toString() : tokenizer.identifier();

        tokenizer.advance();
        // Write the variable name to the output file
        writeIdentifierToken();

        String name = tokenizer.identifier();
        symbolTable.define(name, type, SymbolTable.KIND.fromString(kind));

        tokenizer.advance();
        
        while (tokenizer.tokenType() == JackTokenizer.TokenType.SYMBOL && tokenizer.symbol() == ',') {
            writeSymbolToken(',');
            tokenizer.advance();
            writeIdentifierToken();
            symbolTable.define(tokenizer.identifier(), type, SymbolTable.KIND.fromString(kind));
            tokenizer.advance();
        }
        writeSymbolToken(';');
        

        xmlWriter.println("</varDec>");

    }

    public void compileStatements() {
       xmlWriter.println("<statements>");
        while (tokenizer.hasMoreTokens()) {
            JackTokenizer.Keyword keyword = tokenizer.keyword();
            if (keyword == JackTokenizer.Keyword.LET) {
                compileLet();
            } else if (keyword == JackTokenizer.Keyword.IF) {
                compileIf();
            } else if (keyword == JackTokenizer.Keyword.WHILE) {
                compileWhile();
            } else if (keyword == JackTokenizer.Keyword.DO) {
                compileDo();
            } else if (keyword == JackTokenizer.Keyword.RETURN) {
                compileReturn();
            } else {
               
                // If the token is not a statement keyword,assume we've reached the end of statements
                break;
            }
            tokenizer.advance(); // Advance to the next token after compiling a statement
        }
        xmlWriter.println("</statements>");
    }


     /*
     * letStatement: 'let' varName ('[' expression ']')? '=' expression ';'
     */
    public void compileLet() {
       xmlWriter.println("<letStatement>");

        writeKeywordToken(JackTokenizer.Keyword.LET);

        tokenizer.advance();
        writeIdentifierToken();

        tokenizer.advance();

        if (tokenizer.tokenType() == JackTokenizer.TokenType.SYMBOL && tokenizer.symbol() == '[') {
            writeSymbolToken('[');
    
            tokenizer.advance();
            compileExpression();

            writeSymbolToken(']');
            tokenizer.advance();
        }

        writeSymbolToken('=');

        tokenizer.advance();
        compileExpression();

        writeSymbolToken(';');

        xmlWriter.println("</letStatement>");
    }

    /* 
    *  ifStatement Grammer : 'if' '(' expression ')' '{' statements '}'('else' '{' statements '}')?
    */
    public void compileIf() {
        // Implementation for compiling if statements
        xmlWriter.println("<ifStatement>");
        writeKeywordToken(JackTokenizer.Keyword.IF);

        tokenizer.advance();
        writeSymbolToken('(');

        tokenizer.advance();
        compileExpression();

        writeSymbolToken(')');

        tokenizer.advance();
        writeSymbolToken('{');
  
         
        tokenizer.advance();
        compileStatements();

      
        writeSymbolToken('}');

        if (tokenizer.tokenType() == JackTokenizer.TokenType.KEYWORD && tokenizer.keyword() == JackTokenizer.Keyword.ELSE) {
            writeKeywordToken(JackTokenizer.Keyword.ELSE);
            tokenizer.advance();
            writeSymbolToken('{');
           
            compileStatements();

            tokenizer.advance();
            writeSymbolToken('}');
        }

        xmlWriter.println("</ifStatement>");
    }

   /*
    * 
    * whileStatement: 'while' '(' expression ')' '{' statements '}'
    */
    public void compileWhile() {
        xmlWriter.println("<whileStatement>");
        writeKeywordToken(JackTokenizer.Keyword.WHILE);

        tokenizer.advance();
        writeSymbolToken('(');

        tokenizer.advance();
        compileExpression();

        writeSymbolToken(')');

        tokenizer.advance();
        writeSymbolToken('{');
     
        tokenizer.advance();  
        compileStatements();

        writeSymbolToken('}');

        xmlWriter.println("</whileStatement>");
    }



    /*
     * doStatement: 'do' subroutineCall ';'
     */
    public void compileDo() {
        xmlWriter.println("<doStatement>");
        writeKeywordToken(JackTokenizer.Keyword.DO);


        tokenizer.advance();
        writeIdentifierToken();

        tokenizer.advance();
        if (tokenizer.tokenType() == JackTokenizer.TokenType.SYMBOL && tokenizer.symbol() == '(') {
            writeSymbolToken('(');

            tokenizer.advance();
            compileExpressionList();

         
            writeSymbolToken(')');
        } else if (tokenizer.tokenType() == JackTokenizer.TokenType.SYMBOL && tokenizer.symbol() == '.') {
            writeSymbolToken('.');

            tokenizer.advance();
            writeIdentifierToken();
            
            tokenizer.advance();
            writeSymbolToken('(');
         
           tokenizer.advance();
           compileExpressionList();
       
           writeSymbolToken(')');
        } else {
            throw new IllegalArgumentException(
                    "Expected '(' or '.' after subroutine name, but found: " + tokenizer.getCurrentToken());
        }
       

        tokenizer.advance();
        writeSymbolToken(';');

        xmlWriter.println("</doStatement>");
         
    }

    public void compileReturn() {
       
        xmlWriter.println("<returnStatement>");
        writeKeywordToken(JackTokenizer.Keyword.RETURN);

        tokenizer.advance();
        if (tokenizer.tokenType() != JackTokenizer.TokenType.SYMBOL || tokenizer.symbol() != ';') {
            compileExpression();
        }

        writeSymbolToken(';');

        xmlWriter.println("</returnStatement>");
    }

    /*
     * expression: term (op term)*
     */
    public void compileExpression() {
        // Implementation for compiling expressions
        xmlWriter.println("<expression>");

        compileTerm();

      
        while (tokenizer.tokenType() == JackTokenizer.TokenType.SYMBOL && JackTokenizer.isOperator(tokenizer.symbol())) {
            writeSymbolToken(tokenizer.symbol());
            tokenizer.advance();
            compileTerm();
        }

        xmlWriter.println("</expression>");
    }

    /*
     * term Grammar:
     *   integerConstant | stringConstant | keywordConstant | varName |
     *   varName '[' expression ']' | subroutineCall |
     *   '(' expression ')' | unaryOp term
     */
    public void compileTerm() {
        xmlWriter.println("<term>");
        if (tokenizer.tokenType() == JackTokenizer.TokenType.INT_CONST || 
            tokenizer.tokenType() == JackTokenizer.TokenType.STRING_CONST) {
            writeConstantToken();
            tokenizer.advance();
        } else if (
            tokenizer.keyword() == JackTokenizer.Keyword.TRUE || 
            tokenizer.keyword() == JackTokenizer.Keyword.FALSE || 
            tokenizer.keyword() == JackTokenizer.Keyword.NULL || 
            tokenizer.keyword() == JackTokenizer.Keyword.THIS) {

            writeKeywordToken(tokenizer.keyword());
            tokenizer.advance();

        } else if (tokenizer.tokenType() == JackTokenizer.TokenType.IDENTIFIER) {
            writeIdentifierToken();
            tokenizer.advance();
            if (tokenizer.tokenType() == JackTokenizer.TokenType.SYMBOL && tokenizer.symbol() == '[') {
                writeSymbolToken('[');

                tokenizer.advance();
                compileExpression();

                
                writeSymbolToken(']');
                tokenizer.advance();
            }else if (tokenizer.tokenType() == JackTokenizer.TokenType.SYMBOL && (tokenizer.symbol() == '(' || tokenizer.symbol() == '.')) {
                
                writeSymbolToken(tokenizer.symbol());

                tokenizer.advance();
                writeIdentifierToken();

                tokenizer.advance();
                writeSymbolToken('(');

                tokenizer.advance();
                compileExpressionList();
                
                writeSymbolToken(')');
                tokenizer.advance();
            }
        } else if (tokenizer.tokenType() == JackTokenizer.TokenType.SYMBOL && tokenizer.symbol() == '(') {
            writeSymbolToken('(');

            tokenizer.advance();
            compileExpression();

           
            writeSymbolToken(')');

            tokenizer.advance();
        }else if (tokenizer.tokenType() == JackTokenizer.TokenType.SYMBOL && (tokenizer.symbol() == '-' || tokenizer.symbol() == '~')) {
            writeSymbolToken(tokenizer.symbol());
            tokenizer.advance();
            compileTerm();
        } else {
            throw new IllegalArgumentException(
                    "Unexpected token in term: " + tokenizer.getCurrentToken());
        }
         
         xmlWriter.println("</term>");
    }
       

    public void compileExpressionList() {
       
        xmlWriter.println("<expressionList>");
        

        if (tokenizer.tokenType() != JackTokenizer.TokenType.SYMBOL || tokenizer.symbol() != ')') {
            compileExpression();
            while (tokenizer.tokenType() == JackTokenizer.TokenType.SYMBOL && tokenizer.symbol() == ',') {
                writeSymbolToken(',');
                
                tokenizer.advance();
                compileExpression();
            }
        }
        xmlWriter.println("</expressionList>");
    }

    public void close(){
        if (xmlWriter != null) {
            xmlWriter.close();
        }
    }

   private void writeKeywordToken(JackTokenizer.Keyword expectedKeyword) {
         if( tokenizer.tokenType() == JackTokenizer.TokenType.KEYWORD && tokenizer.keyword() == expectedKeyword){
               xmlWriter.println("<keyword> " + tokenizer.keyword() + " </keyword>");
        }else{
            throw new IllegalArgumentException(
                    "Expected keyword: " + expectedKeyword + ", but found: " + tokenizer.getCurrentToken());
        }
    }

    private void writeIdentifierToken() {
        if (tokenizer.tokenType() == JackTokenizer.TokenType.IDENTIFIER) {
            xmlWriter.println("<identifier> " + tokenizer.identifier() + " </identifier>");
        } else {
            throw new IllegalArgumentException(
                    "Expected identifier, but found: " + tokenizer.getCurrentToken());
        }
    }

    private void writeSymbolToken(char expectedSymbol) {
        if (tokenizer.tokenType() == JackTokenizer.TokenType.SYMBOL && tokenizer.symbol() == expectedSymbol) {
            xmlWriter.println("<symbol> " + escapeXml(tokenizer.symbol()) + " </symbol>");
        } else {
            throw new IllegalArgumentException(
                    "Expected symbol: " + expectedSymbol + ", but found: " + tokenizer.getCurrentToken());
        }
    }

    private void writeConstantToken() {
        if (tokenizer.tokenType() == JackTokenizer.TokenType.INT_CONST) {
            xmlWriter.println("<integerConstant> " + tokenizer.intVal() + " </integerConstant>");
        } else if (tokenizer.tokenType() == JackTokenizer.TokenType.STRING_CONST) {
            xmlWriter.println("<stringConstant> " + tokenizer.stringVal() + " </stringConstant>");
        } else {
            throw new IllegalArgumentException(
                    "Expected constant (integer or string), but found: " + tokenizer.getCurrentToken());
        }
    }
    
    

    private String escapeXml(char value) {
        if(xmlEscapeSymbols.containsKey(value)) {
            return xmlEscapeSymbols.get(value);
        }
        return String.valueOf(value);
    }
}

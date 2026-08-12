package JackAnalyzer;

import java.io.File;
import java.io.PrintWriter;
import java.util.HashMap;

https://github.com/santvan582/-Nand-to-Tetris-Projects.git

public class CompilationEngine {

    private PrintWriter writer;
    private final JackTokenizer tokenizer;
    private final HashMap<Character, String> xmlEscapeSymbols = new HashMap<Character, String>() {{
        put('&', "&amp;");
        put('<', "&lt;");
        put('>', "&gt;");
        put('"', "&quot;");
    }};

    public CompilationEngine(File inputFile, File outputFile) throws Exception {

        this.writer = new PrintWriter(outputFile);
        this.tokenizer = new JackTokenizer(inputFile);
        compileClass();
        // Implementation for initializing the compilation engine

    }

    public void compileClass() {
        // Implementation for compiling a class

            tokenizer.advance();
            writer.println("<class>");
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
            writer.println("</class>");

    }

    public void compileClassVarDec() {
            
        // Write the class variable declaration to the output file
        writer.println("<classVarDec>");
        writeKeywordToken(tokenizer.keyword());

        tokenizer.advance();        

        if (JackTokenizer.isParameterTypeKeyword(tokenizer.keyword())) {
            writeKeywordToken(tokenizer.keyword());
        } else {
            writeIdentifierToken();
        }

        //Write the variable name to the output file
        tokenizer.advance();
        writeIdentifierToken();

        tokenizer.advance();
        while(tokenizer.tokenType() == JackTokenizer.TokenType.SYMBOL && tokenizer.symbol() == ','){
            writeSymbolToken(',');
            tokenizer.advance();
            writeIdentifierToken();
            tokenizer.advance();
        }

        writeSymbolToken(';');
               
        writer.println("</classVarDec>");
    }

    public void compileSubroutine() {

        writer.println("<subroutineDec>");
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
        writer.println("<parameterList>"); 
        tokenizer.advance();
        compileParameterList();
        writer.println("</parameterList>"); 
        writeSymbolToken(')');



  


        compileSubroutineBody();

        

        writer.println("</subroutineDec>");

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

        writer.println("<subroutineBody>");

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
        writer.println("</subroutineBody>");

    }

    public void compileVarDec() {
        // Write the variable declaration to the output file
        writer.println("<varDec>");
        writeKeywordToken(tokenizer.keyword());

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

        tokenizer.advance();
        // Write the variable name to the output file
        writeIdentifierToken();

        tokenizer.advance();
                
        while (tokenizer.tokenType() == JackTokenizer.TokenType.SYMBOL && tokenizer.symbol() == ',') {
            writeSymbolToken(',');
            tokenizer.advance();
            writeIdentifierToken();
            tokenizer.advance();
        }
        writeSymbolToken(';');
        

        writer.println("</varDec>");

    }

    public void compileStatements() {
       writer.println("<statements>");
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
        writer.println("</statements>");
    }


     /*
     * letStatement: 'let' varName ('[' expression ']')? '=' expression ';'
     */
    public void compileLet() {
       writer.println("<letStatement>");

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

        writer.println("</letStatement>");
    }

    /* 
    *  ifStatement Grammer : 'if' '(' expression ')' '{' statements '}'('else' '{' statements '}')?
    */
    public void compileIf() {
        // Implementation for compiling if statements
        writer.println("<ifStatement>");
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

        writer.println("</ifStatement>");
    }

   /*
    * 
    * whileStatement: 'while' '(' expression ')' '{' statements '}'
    */
    public void compileWhile() {
        writer.println("<whileStatement>");
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

        writer.println("</whileStatement>");
    }



    /*
     * doStatement: 'do' subroutineCall ';'
     */
    public void compileDo() {
        writer.println("<doStatement>");
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

        writer.println("</doStatement>");
         
    }

    public void compileReturn() {
       
        writer.println("<returnStatement>");
        writeKeywordToken(JackTokenizer.Keyword.RETURN);

        tokenizer.advance();
        if (tokenizer.tokenType() != JackTokenizer.TokenType.SYMBOL || tokenizer.symbol() != ';') {
            compileExpression();
        }

        writeSymbolToken(';');

        writer.println("</returnStatement>");
    }

    /*
     * expression: term (op term)*
     */
    public void compileExpression() {
        // Implementation for compiling expressions
        writer.println("<expression>");

        compileTerm();

      
        while (tokenizer.tokenType() == JackTokenizer.TokenType.SYMBOL && JackTokenizer.isOperator(tokenizer.symbol())) {
            writeSymbolToken(tokenizer.symbol());
            tokenizer.advance();
            compileTerm();
        }

        writer.println("</expression>");
    }

    /*
     * term Grammar:
     *   integerConstant | stringConstant | keywordConstant | varName |
     *   varName '[' expression ']' | subroutineCall |
     *   '(' expression ')' | unaryOp term
     */
    public void compileTerm() {
        writer.println("<term>");
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
         
         writer.println("</term>");
    }
       

    public void compileExpressionList() {
       
        writer.println("<expressionList>");
        

        if (tokenizer.tokenType() != JackTokenizer.TokenType.SYMBOL || tokenizer.symbol() != ')') {
            compileExpression();
            while (tokenizer.tokenType() == JackTokenizer.TokenType.SYMBOL && tokenizer.symbol() == ',') {
                writeSymbolToken(',');
                
                tokenizer.advance();
                compileExpression();
            }
        }
        writer.println("</expressionList>");
    }

    public void close(){
        if (writer != null) {
            writer.close();
        }
    }

   private void writeKeywordToken(JackTokenizer.Keyword expectedKeyword) {
         if( tokenizer.tokenType() == JackTokenizer.TokenType.KEYWORD && tokenizer.keyword() == expectedKeyword){
               writer.println("<keyword> " + tokenizer.keyword() + " </keyword>");
        }else{
            throw new IllegalArgumentException(
                    "Expected keyword: " + expectedKeyword + ", but found: " + tokenizer.getCurrentToken());
        }
    }

    private void writeIdentifierToken() {
        if (tokenizer.tokenType() == JackTokenizer.TokenType.IDENTIFIER) {
            writer.println("<identifier> " + tokenizer.identifier() + " </identifier>");
        } else {
            throw new IllegalArgumentException(
                    "Expected identifier, but found: " + tokenizer.getCurrentToken());
        }
    }

    private void writeSymbolToken(char expectedSymbol) {
        if (tokenizer.tokenType() == JackTokenizer.TokenType.SYMBOL && tokenizer.symbol() == expectedSymbol) {
            writer.println("<symbol> " + escapeXml(tokenizer.symbol()) + " </symbol>");
        } else {
            throw new IllegalArgumentException(
                    "Expected symbol: " + expectedSymbol + ", but found: " + tokenizer.getCurrentToken());
        }
    }

    private void writeConstantToken() {
        if (tokenizer.tokenType() == JackTokenizer.TokenType.INT_CONST) {
            writer.println("<integerConstant> " + tokenizer.intVal() + " </integerConstant>");
        } else if (tokenizer.tokenType() == JackTokenizer.TokenType.STRING_CONST) {
            writer.println("<stringConstant> " + tokenizer.stringVal() + " </stringConstant>");
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

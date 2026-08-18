package JackAnalyzer;

import java.io.File;
import java.io.PrintWriter;
import java.util.HashMap;

import JackAnalyzer.SymbolTable.KIND;
import JackAnalyzer.VMWriter.COMMAND;
import JackAnalyzer.VMWriter.SEGMENT;



public class CompilationEngine {

    private PrintWriter xmlWriter;
    private final JackTokenizer tokenizer;
    private final VMWriter vmWriter;
    private final SymbolTable symbolTable;
    private String className;
    private int labelCounter = 0; // Counter for generating unique labels
    private final HashMap<Character, String> xmlEscapeSymbols = new HashMap<Character, String>() {{
        put('&', "&amp;");
        put('<', "&lt;");
        put('>', "&gt;");
        put('"', "&quot;");
    }};

    public CompilationEngine(File inputFile, File xmlOutput,String vmOutput) throws Exception {

        this.xmlWriter = new PrintWriter(xmlOutput);
        this.vmWriter = new VMWriter(vmOutput);
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
            className = tokenizer.identifier();
        

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

            vmWriter.close(); // Close the VMWriter after finishing the compilation

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

        symbolTable.resetSubroutine(); // Reset the subroutine scope for each new subroutine
        JackTokenizer.Keyword subroutineType = tokenizer.keyword(); // Store the subroutine type
        
        String returnType = "";
        //Return Type Keyword or Identifier
        tokenizer.advance();
        if (JackTokenizer.isParameterTypeKeyword(tokenizer.keyword())){
            writeKeywordToken(tokenizer.keyword());
            returnType = tokenizer.keyword().toString(); 
        }else{
            writeIdentifierToken();
            returnType = tokenizer.identifier();
        }

        tokenizer.advance();
        //Subroutine Name Identifier
        writeIdentifierToken();

        String subroutineName = tokenizer.identifier();
        String fullSubroutineName = className + "." + subroutineName;
 

        tokenizer.advance();
        writeSymbolToken('(');
        xmlWriter.println("<parameterList>");

        tokenizer.advance();
        compileParameterList();
        
        
        xmlWriter.println("</parameterList>"); 
        writeSymbolToken(')');


        compileSubroutineBody(fullSubroutineName, subroutineType);

        

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

        String type = tokenizer.tokenType() == JackTokenizer.TokenType.KEYWORD ? tokenizer.keyword().toString() : tokenizer.identifier();
            
        tokenizer.advance();
        writeIdentifierToken();

        String name = tokenizer.identifier();
        symbolTable.define(name, type, SymbolTable.KIND.ARG);


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
            type = tokenizer.tokenType() == JackTokenizer.TokenType.KEYWORD ? tokenizer.keyword().toString() : tokenizer.identifier();

            //Parameter Name Identifier
            tokenizer.advance();
            writeIdentifierToken();
            name = tokenizer.identifier();
            symbolTable.define(name, type, SymbolTable.KIND.ARG);


            tokenizer.advance();
        }
    }
    /**
     * Compiles a complete method function or constructor
     */
    public void compileSubroutineBody(String funName,JackTokenizer.Keyword keyword) {

      xmlWriter.println("<subroutineBody>");
     
      tokenizer.advance();
      writeSymbolToken('{');
       
       tokenizer.advance();
       while (tokenizer.keyword() == JackTokenizer.Keyword.VAR) {
            compileVarDec();
            tokenizer.advance();
        }


         //Should  Write Main.main 2
        //Initialize the function in the VM output with the number of local variables
        vmWriter.writeFunction(funName,symbolTable.varCount(SymbolTable.KIND.VAR));

         //METHOD and CONSTRUCTOR load with this pointer else allocate memory for constructor
        if (keyword == JackTokenizer.Keyword.METHOD){
            vmWriter.writePush(VMWriter.SEGMENT.ARGUMENT, 0);
            vmWriter.writePop(VMWriter.SEGMENT.POINTER,0);
        }else if (keyword == JackTokenizer.Keyword.CONSTRUCTOR){
            vmWriter.writePush(VMWriter.SEGMENT.CONSTANT,symbolTable.varCount(SymbolTable.KIND.FIELD));
            vmWriter.writeCall("Memory.alloc", 1);
            vmWriter.writePop(VMWriter.SEGMENT.POINTER,0);
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
        symbolTable.define(name, type, SymbolTable.KIND.VAR);

        tokenizer.advance();
        
        while (tokenizer.tokenType() == JackTokenizer.TokenType.SYMBOL && tokenizer.symbol() == ',') {
            writeSymbolToken(',');
            tokenizer.advance();

            writeIdentifierToken();
            symbolTable.define(tokenizer.identifier(), type, SymbolTable.KIND.VAR);
            
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
                tokenizer.advance();
            } else if (keyword == JackTokenizer.Keyword.IF) {
                compileIf();
            } else if (keyword == JackTokenizer.Keyword.WHILE) {
                compileWhile();
                tokenizer.advance();
            } else if (keyword == JackTokenizer.Keyword.DO) {
                compileDo();
                tokenizer.advance();
            } else if (keyword == JackTokenizer.Keyword.RETURN) {
                compileReturn();
                tokenizer.advance();
            } else {
                // If the token is not a statement keyword,assume we've reached the end of statements
                break;
            }
            // Advance to the next token after compiling a statement
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
       
        String varName = tokenizer.identifier();


        tokenizer.advance();

        boolean isArrayAssignment = false;

        while (tokenizer.tokenType() == JackTokenizer.TokenType.SYMBOL && tokenizer.symbol() == '[') {
            writeSymbolToken('[');
    
            vmWriter.writePush(VMWriter.SEGMENT.getSegmentFromKind(symbolTable.kindOf(varName)), symbolTable.indexOf(varName));

            tokenizer.advance();
            compileExpression();

            vmWriter.writeArithmetic(VMWriter.COMMAND.ADD);
            writeSymbolToken(']');
            tokenizer.advance();

            // address (base+index) stays on the stack until the RHS is compiled below
            isArrayAssignment = true;
        }

        writeSymbolToken('=');

        tokenizer.advance();
        compileExpression();

        writeSymbolToken(';');

        if (isArrayAssignment) {
            vmWriter.writePop(VMWriter.SEGMENT.TEMP,0);
            vmWriter.writePop(VMWriter.SEGMENT.POINTER,1);
            vmWriter.writePush(VMWriter.SEGMENT.TEMP,0);
            vmWriter.writePop(VMWriter.SEGMENT.THAT,0);
        } else {
            vmWriter.writePop(VMWriter.SEGMENT.getSegmentFromKind(symbolTable.kindOf(varName)), symbolTable.indexOf(varName));
        }

        

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

        vmWriter.writeArithmetic(VMWriter.COMMAND.NOT);
        String elseLabel = label();
        vmWriter.writeIf(elseLabel);

        tokenizer.advance();
        writeSymbolToken('{');
  
         
        tokenizer.advance();
        compileStatements();

        writeSymbolToken('}');
        String endLabel = label();
        vmWriter.writeGoto(endLabel);
        vmWriter.writeLabel(elseLabel);
        tokenizer.advance();
        if (tokenizer.tokenType() == JackTokenizer.TokenType.KEYWORD && tokenizer.keyword() == JackTokenizer.Keyword.ELSE) {
            writeKeywordToken(JackTokenizer.Keyword.ELSE);
            tokenizer.advance();
            writeSymbolToken('{');
            
            tokenizer.advance();
            compileStatements();
             
            writeSymbolToken('}');
            tokenizer.advance();
        }
        vmWriter.writeLabel(endLabel);
    }

   /*
    * 
    * whileStatement: 'while' '(' expression ')' '{' statements '}'
    */
    public void compileWhile() {
        xmlWriter.println("<whileStatement>");
        writeKeywordToken(JackTokenizer.Keyword.WHILE);
        String startLabel = label();

        vmWriter.writeLabel(startLabel);

        tokenizer.advance();
        writeSymbolToken('(');

        tokenizer.advance();
        compileExpression();

        writeSymbolToken(')');
        vmWriter.writeArithmetic(VMWriter.COMMAND.NOT);
        String endLabel = label();
        vmWriter.writeIf(endLabel);

        tokenizer.advance();
        writeSymbolToken('{');
     
        tokenizer.advance();  
        compileStatements();

        writeSymbolToken('}');
        vmWriter.writeGoto(startLabel);
        vmWriter.writeLabel(endLabel);

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
         String objName = tokenizer.identifier();
        //Store Outuput 
        String name1 = symbolTable.typeOf(tokenizer.identifier());
        

        tokenizer.advance();

        if (tokenizer.tokenType() == JackTokenizer.TokenType.SYMBOL && tokenizer.symbol() == '(') {
            writeSymbolToken('(');
            vmWriter.writePush(VMWriter.SEGMENT.POINTER,0);

            tokenizer.advance();
            int nArgs = compileExpressionList() + 1;

         
            writeSymbolToken(')');
            vmWriter.writeCall(className + '.' + objName, nArgs);
           
        } else if (tokenizer.tokenType() == JackTokenizer.TokenType.SYMBOL && tokenizer.symbol() == '.') {
            writeSymbolToken('.');

            tokenizer.advance();
            writeIdentifierToken();
           
            //Store Println
            String name2 = tokenizer.identifier();
            
            tokenizer.advance();
            writeSymbolToken('(');
         
           tokenizer.advance();
           int nArgs = compileExpressionList() ; // Add 1 for the object reference
       
           writeSymbolToken(')');
           String fullName = name1 + "." + name2;
           KIND mobjKind = symbolTable.kindOf(objName);
           if (mobjKind != KIND.NONE) {
             nArgs++; // Increment nArgs for the object reference
             vmWriter.writePush(SEGMENT.getSegmentFromKind(mobjKind), symbolTable.indexOf(objName));
           }
           
           vmWriter.writeCall(fullName, nArgs);
        } else {
            throw new IllegalArgumentException(
                    "Expected '(' or '.' after subroutine name, but found: " + tokenizer.getCurrentToken());
        }
       

        tokenizer.advance();
        writeSymbolToken(';');
        vmWriter.writePop(VMWriter.SEGMENT.TEMP,0);
        
        xmlWriter.println("</doStatement>");
         
    }

    public void compileReturn() {
       
        xmlWriter.println("<returnStatement>");
        writeKeywordToken(JackTokenizer.Keyword.RETURN);

        tokenizer.advance();
        if (tokenizer.tokenType() != JackTokenizer.TokenType.SYMBOL || tokenizer.symbol() != ';') {
            compileExpression();
        }else{
            //no expression push 0 to stack
            vmWriter.writePush(VMWriter.SEGMENT.CONSTANT,0);
        }
       
        writeSymbolToken(';');
        
        vmWriter.writeReturn();
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
            //Store the operator for VM command
            VMWriter.COMMAND vmCommand = VMWriter.COMMAND.getCommandFromOp(tokenizer.symbol());
            
            tokenizer.advance();
            compileTerm();
            
            vmWriter.writeArithmetic(vmCommand);
            
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
            if (tokenizer.tokenType() == JackTokenizer.TokenType.INT_CONST) {
                vmWriter.writePush(VMWriter.SEGMENT.CONSTANT,tokenizer.intVal());
            }else{
               String str = tokenizer.stringVal();
               vmWriter.writePush(VMWriter.SEGMENT.CONSTANT,str.length());
               vmWriter.writeCall("String.new",1);
               for (int i = 0; i < str.length(); i++){
                    vmWriter.writePush(VMWriter.SEGMENT.CONSTANT,(int)str.charAt(i));
                    vmWriter.writeCall("String.appendChar",2);
                }
            }   
           
            writeConstantToken();
            tokenizer.advance();
        } else if (
            tokenizer.keyword() == JackTokenizer.Keyword.TRUE || 
            tokenizer.keyword() == JackTokenizer.Keyword.FALSE || 
            tokenizer.keyword() == JackTokenizer.Keyword.NULL || 
            tokenizer.keyword() == JackTokenizer.Keyword.THIS) {

            if (tokenizer.keyword() == JackTokenizer.Keyword.TRUE) {
                vmWriter.writePush(VMWriter.SEGMENT.CONSTANT, 0);
                vmWriter.writeArithmetic(VMWriter.COMMAND.NOT);
            } else if (tokenizer.keyword() == JackTokenizer.Keyword.FALSE || tokenizer.keyword() == JackTokenizer.Keyword.NULL) {
                vmWriter.writePush(VMWriter.SEGMENT.CONSTANT, 0);
            } else if (tokenizer.keyword() == JackTokenizer.Keyword.THIS) {
                vmWriter.writePush(VMWriter.SEGMENT.POINTER, 0);
            }
            writeKeywordToken(tokenizer.keyword());
            tokenizer.advance();
        } else if (tokenizer.tokenType() == JackTokenizer.TokenType.IDENTIFIER) {
            writeIdentifierToken();
            //get Memory
            String varName = tokenizer.identifier();

            tokenizer.advance();
            if (tokenizer.tokenType() == JackTokenizer.TokenType.SYMBOL && tokenizer.symbol() == '[') {
                writeSymbolToken('[');
                vmWriter.writePush(SEGMENT.getSegmentFromKind(symbolTable.kindOf(varName)),symbolTable.indexOf(varName));

                tokenizer.advance();
                compileExpression();

                
                writeSymbolToken(']');
                tokenizer.advance();

                //base+offset
                vmWriter.writeArithmetic(VMWriter.COMMAND.ADD);

                //pop into 'that' pointer
                vmWriter.writePop(VMWriter.SEGMENT.POINTER,1);
                //push *(base+index) onto stack
                vmWriter.writePush(VMWriter.SEGMENT.THAT,0);
            }else if (tokenizer.tokenType() == JackTokenizer.TokenType.SYMBOL && (tokenizer.symbol() == '(' || tokenizer.symbol() == '.')) {
                
                writeSymbolToken(tokenizer.symbol());

                tokenizer.advance();
                writeIdentifierToken();

                String name2 = tokenizer.identifier();


                tokenizer.advance();
                writeSymbolToken('(');

                tokenizer.advance();
                int nArgs = compileExpressionList();
                vmWriter.writeCall(varName + '.' + name2, nArgs);
                writeSymbolToken(')');
                tokenizer.advance();
            }else{
               vmWriter.writePush(SEGMENT.getSegmentFromKind(symbolTable.kindOf(varName)),symbolTable.indexOf(varName));
            }
        } else if (tokenizer.tokenType() == JackTokenizer.TokenType.SYMBOL && tokenizer.symbol() == '(') {
            writeSymbolToken('(');

            tokenizer.advance();
            compileExpression();

           
            writeSymbolToken(')');

            tokenizer.advance();
        }else if (tokenizer.tokenType() == JackTokenizer.TokenType.SYMBOL && (tokenizer.symbol() == '-' || tokenizer.symbol() == '~')) {
            char ngCommand = tokenizer.symbol(); //Save the symbol for VM command
            writeSymbolToken(tokenizer.symbol());
            tokenizer.advance();
            compileTerm();
            if (ngCommand == '-'){
                vmWriter.writeArithmetic(VMWriter.COMMAND.NEG);
            }else {
                vmWriter.writeArithmetic(VMWriter.COMMAND.NOT);
            }
        } else {
            throw new IllegalArgumentException(
                    "Unexpected token in term: " + tokenizer.getCurrentToken());
        }
         
         xmlWriter.println("</term>");
    }
       

    public int compileExpressionList() {
        int nArgs = 0;
       
        xmlWriter.println("<expressionList>");
        

        if (tokenizer.tokenType() != JackTokenizer.TokenType.SYMBOL || tokenizer.symbol() != ')') {
            compileExpression();
            nArgs++;
            while (tokenizer.tokenType() == JackTokenizer.TokenType.SYMBOL && tokenizer.symbol() == ',') {
                writeSymbolToken(',');
                nArgs++;
                tokenizer.advance();
                compileExpression();
            }
        }
        xmlWriter.println("</expressionList>");
        return nArgs;
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

   private String label() {
        
        String newLable =  className + "_" + labelCounter;
        labelCounter++;
        return newLable;
    }


}

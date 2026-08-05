package JackAnalyzer;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;
import java.util.ArrayList;

public class JackTokenizer {

    private File mFile;
    private String fileContent;
    static String keyWords = "class|constructor|function|method|field|static|var|int|char|boolean|void|true|false|null|this|let|do|if|else|while|return";
    static String symbols = "[\\&\\*\\+\\(\\)\\.\\/\\,\\-\\]\\;\\~\\}\\|\\{\\>\\=\\[\\<]";
    static String intReg = "[0-9]+";
    static String strReg = "\"[^\"\n]*\"";
    static String idReg = "[\\w_]+";
    static String operators = "[\\&\\*\\+\\-\\~\\|\\>\\=\\<]";
    private ArrayList<String> tokens = new ArrayList<>();
    private String currentToken = "";

    public enum TokenType {
        KEYWORD,
        SYMBOL,
        IDENTIFIER,
        INT_CONST,
        STRING_CONST

    }

    public enum Keyword {
        CLASS,
        METHOD,
        FUNCTION,
        CONSTRUCTOR,
        INT,
        BOOLEAN,
        CHAR,
        VOID,
        VAR,
        STATIC,
        FIELD,
        LET,
        DO,
        IF,
        ELSE,
        WHILE,
        RETURN,
        TRUE,
        FALSE,
        NULL,
        THIS;

        public String toString() {
            return this.name().toLowerCase();
        }

        static Keyword fromString(String keyword) {
            switch (keyword) {
                case "class":
                    return CLASS;
                case "method":
                    return METHOD;
                case "function":
                    return FUNCTION;
                case "constructor":
                    return CONSTRUCTOR;
                case "int":
                    return INT;
                case "boolean":
                    return BOOLEAN;
                case "char":
                    return CHAR;
                case "void":
                    return VOID;
                case "var":
                    return VAR;
                case "static":
                    return STATIC;
                case "field":
                    return FIELD;
                case "let":
                    return LET;
                case "do":
                    return DO;
                case "if":
                    return IF;
                case "else":
                    return ELSE;
                case "while":
                    return WHILE;
                case "return":
                    return RETURN;
                case "true":
                    return TRUE;
                case "false":
                    return FALSE;
                case "null":
                    return NULL;
                case "this":
                    return THIS;
                default:
                    return null;
            }
        }





    }

    public JackTokenizer(File jackFile) {
        this.mFile = jackFile;
        openFile(); // Initialize the tokenizer by opening the file
    }

    private void openFile() {

        try {
            Scanner scanner = new Scanner(mFile);
            String input = "";
            StringBuilder content = new StringBuilder();
            boolean inBlockComment = false;
            while (scanner.hasNextLine()) {

                input = scanner.nextLine();
                // Remove comments from a line of code
                input = removeComments(input);

                if (input.contains("/*") && input.contains("*/")) {

                    input = input.substring(0, input.indexOf("/*")) + input.substring(input.indexOf("*/") + 2);

                } else if (input.contains("/*")) {
                    inBlockComment = true;
                    input = input.substring(0, input.indexOf("/*"));
                }

                else if (input.contains("*/")) {
                    inBlockComment = false;
                    input = input.substring(input.indexOf("*/") + 2);
                }

                if (!inBlockComment && !input.isEmpty()) {
                    content.append(input).append("\n");
                }
            }

            scanner.close();

            fileContent = content.toString();

            System.out.println("File content: \n" + fileContent);
            distinguishTokens(fileContent); // Call the method to distinguish tokens after reading the file content

        } catch (FileNotFoundException e) {
            System.out.println("File not found: " + mFile.getAbsolutePath());
        }

    }

    private String removeComments(String line) {
        int position = line.indexOf("//");

        if (position != -1) {

            line = line.substring(0, position);

        }
        return line;
    }

    private void distinguishTokens(String content) {

        // Implement tokenization logic here
        boolean inString = false;
        StringBuilder currentString = new StringBuilder();

        for (char c : content.toCharArray()) {
            // Process each character to identify tokens
            if (c == '"') {
                inString = !inString; // Toggle string state
                currentString.append(c);
                if (!inString) {
                    // End of string token
                    tokens.add(currentString.toString());
                    currentString.setLength(0); // Reset current token
                }
            } else if (inString) {
                currentString.append(c); // Append character to string token
            } else if (Character.isWhitespace(c)) {
                if (currentString.length() > 0) {
                    tokens.add(currentString.toString());
                    // System.out.println("String not type Token: " + currentToken.toString());
                    currentString.setLength(0); // Reset current token
                }
            } else if (isSymbol(c)) {
                //When a symbol is encountered, add the current token (if any) and the symbol as separate tokens
                if (currentString.length() > 0) {
                    tokens.add(currentString.toString());
                    // System.out.println("Symbol current Token in : " + currentToken.toString());
                    currentString.setLength(0); // Reset current token
                }
                tokens.add(String.valueOf(c));
                // System.out.println("Symbol Token: " + c);
            } else {
                currentString.append(c); // Append character to token
            }
        }

    }

    private boolean isSymbol(char character) {
        return symbols.indexOf(character) != -1;
    }

    public boolean hasMoreTokens() {
        return !tokens.isEmpty();
    }

    public void advance() {
        if (hasMoreTokens()) {
            currentToken = tokens.remove(0);
        } else {
            throw new IllegalStateException("No more tokens available.");
        }
    }

    public TokenType tokenType() {
        if (currentToken.matches(keyWords)) {
            return TokenType.KEYWORD;
        } else if (currentToken.matches(symbols)) {
            return TokenType.SYMBOL;
        } else if (currentToken.matches(intReg)) {
            return TokenType.INT_CONST;
        } else if (currentToken.matches(strReg)) {
            return TokenType.STRING_CONST;
        } else if (currentToken.matches(idReg)) {
            return TokenType.IDENTIFIER;
        } else {
            throw new IllegalStateException("Unknown token type: " + currentToken);
        }
    }

    public Keyword keyword() {
        return Keyword.fromString(currentToken);
    }

    static boolean isParameterTypeKeyword(Keyword keyword) {
        if (keyword == Keyword.INT || keyword == Keyword.BOOLEAN || keyword == Keyword.CHAR || keyword == Keyword.VOID) {
            return true;
        } else {
            return false;
        }
    }

    static boolean isOperator(char symbol) {
        return String.valueOf(symbol).matches(operators);
    }

    public char symbol() {
        if(tokenType() != TokenType.SYMBOL) {
            throw new IllegalStateException("Current token is not a symbol.");
        }
        return currentToken.charAt(0);
    }

    public String identifier() {
        if(tokenType() != TokenType.IDENTIFIER) {
            throw new IllegalStateException("Current token is not an identifier.");
        }
        return currentToken;
    }

    public int intVal() {
        if(tokenType() != TokenType.INT_CONST) {
            throw new IllegalStateException("Current token is not an integer constant.");
        }
        return Integer.parseInt(currentToken);
    }

    public String stringVal() {
        if(tokenType() != TokenType.STRING_CONST) {
            throw new IllegalStateException("Current token is not a string constant.");
        }
        return currentToken.substring(1, currentToken.length() - 1); // Remove the enclosing double quotes
    }

    public String getCurrentToken() {
        return currentToken;
    }
}

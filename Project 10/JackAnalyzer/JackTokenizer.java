package JackAnalyzer;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class JackTokenizer {
 

    private File mFile;
    String fileContent;
    private String symbols = "{}()[].,;+-*/&|<>=~";
    
   
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
            //Remove comments from a line of code
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

    private String removeComments(String line){
        int position = line.indexOf("//");

        if (position != -1){

            line = line.substring(0, position);

        }
        return line;
    } 
    
    private void distinguishTokens(String content) {

        // Implement tokenization logic here
        boolean inString = false;
        StringBuilder currentToken = new StringBuilder();

        for(char c : content.toCharArray()) {
            // Process each character to identify tokens
            if(c == '"') {
                inString = !inString; // Toggle string state
                currentToken.append(c);
                if(!inString) {
                    // End of string token
                    System.out.println("String Token: " + currentToken.toString());
                    currentToken.setLength(0); // Reset current token
                }
            } else if(inString) {
                currentToken.append(c); // Append character to string token
            } else if(Character.isWhitespace(c)) {
                if(currentToken.length() > 0) {
                    System.out.println("Token: " + currentToken.toString());
                    currentToken.setLength(0); // Reset current token
                }
            } else if(isSymbol(c)) {
                if(currentToken.length() > 0) {
                    System.out.println("Token: " + currentToken.toString());
                    currentToken.setLength(0); // Reset current token
                }
                System.out.println("Symbol Token: " + c);
            } else {
                currentToken.append(c); // Append character to token
            }
        

            

        }



        
    }

    private boolean isSymbol(char character){
        return symbols.indexOf(character) != -1;
    }
    
    
}

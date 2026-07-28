package JackAnalyzer;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class Tokenizer {
 

    private File mFile;
    private String line;
   
    public Tokenizer(File jackFile) {
        this.mFile = jackFile;
        openFile(); // Initialize the tokenizer by opening the file
    }

    private void openFile() {

      try {
      Scanner scanner = new Scanner(mFile);

      StringBuilder content = new StringBuilder();
      boolean inBlockComment = false;
        while (scanner.hasNextLine()) {
        
            line = scanner.nextLine();
            //Remove comments from a line of code
            line = removeComments(line);
            
             if (line.contains("/*") && line.contains("*/")) {

                  line = line.substring(0, line.indexOf("/*")) + line.substring(line.indexOf("*/") + 2);
                
            } else if (line.contains("/*")) {
                inBlockComment = true;
                line = line.substring(0, line.indexOf("/*"));
            }

            else if (line.contains("*/")) {
                inBlockComment = false;
                line = line.substring(line.indexOf("*/") + 2);
            }
                

            if (!inBlockComment && !line.isEmpty()) {
                content.append(line).append("\n");
            }
        }

        scanner.close();     

        
        System.out.println("File content: \n" + content.toString().trim());

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


    // private String removeCommentBlock(String content) {
    //     StringBuilder result = new StringBuilder();
        

    //     String[] lines = content.split("\n");
    //     for (String line : lines) {
    //         if (line.contains("/*")) {
    //             inBlockComment = true;
    //             line = line.substring(0, line.indexOf("/*"));
    //         }
    //         if (line.contains("*/")) {
    //             inBlockComment = false;
    //             line = line.substring(line.indexOf("*/") + 2);
    //         }
    //         if (!inBlockComment && !line.trim().isEmpty()) {
    //             result.append(line).append("\n");
    //         }
    //     }

    //     return result.toString().trim();
    // }
    



    
    
}

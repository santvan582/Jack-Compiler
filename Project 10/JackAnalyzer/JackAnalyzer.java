package JackAnalyzer;


import java.io.File;        

public class JackAnalyzer {
    public static void main(String[] args) {
        System.out.println("Starting JackAnalyzer...");

        if (args.length != 1) {
            System.out.println("No input file or directory provided. Please provide a .jack file or a directory containing .jack files.");

        }else {

            String inputFile = args[0];
            File file = new File(inputFile);

            if (file.isDirectory()) {
                File[] files = file.listFiles((dir, name) -> name.endsWith(".jack"));
                if (files != null) {
                    for (File jackFile : files) {
                        new Tokenizer(jackFile);
                    }
                }
            } else if (file.isFile() && inputFile.endsWith(".jack")) {
                new Tokenizer(file);
            } else {
                System.out.println("Invalid input for path " + inputFile );
            }
        }
    }
    
} 
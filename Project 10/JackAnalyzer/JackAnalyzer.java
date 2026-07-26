package JackAnalyzer;


import java.io.File;
import java.io.FileNotFoundException; 
import java.util.Scanner;         

public class JackAnalyzer {
    public static void main(String[] args) { 

         if (args.length != 1){

            System.out.println("No input file or directory provided. Please provide a .jack file or a directory containing .jack files.");

        }else {

            String inputFile = args[0];
            File file = new File(inputFile);

            if (file.isDirectory()) {
                File[] files = file.listFiles((dir, name) -> name.endsWith(".jack"));
                if (files != null) {
                    for (File jackFile : files) {
                        processFile(jackFile);
                    }
                }
            } else if (file.isFile() && inputFile.endsWith(".jack")) {
                processFile(file);
            } else {
                System.out.println("Invalid input. Please provide a .jack file or a directory containing .jack files.");
            }
        }
    }

    private static void processFile(File jackFile) {
        try {
            Scanner scanner = new Scanner(jackFile);
            StringBuilder content = new StringBuilder();
            while (scanner.hasNextLine()) {
                content.append(scanner.nextLine()).append("\n");
            }
            scanner.close();

            // Here you would typically call the CompilationEngine or other processing classes
            // For demonstration, we will just print the content of the file
            System.out.println("Processing file: " + jackFile.getName());
            System.out.println(content.toString());

        } catch (FileNotFoundException e) {
            System.out.println("File not found: " + jackFile.getAbsolutePath());
        }
    }

   
    
    
} 
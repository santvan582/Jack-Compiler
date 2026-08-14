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
                        CompilationEngine compilationEngine = null;
                        try {
                            System.out.println("For File Name : " + jackFile.getName()+ "Absolute path is file: " + jackFile.getAbsolutePath());
                            File xmlOutput = new File(jackFile.getAbsolutePath().replace(".jack", ".xml"));
                            String vmOutput = jackFile.getAbsolutePath().replace(".jack", ".vm");
                            compilationEngine = new CompilationEngine(jackFile, xmlOutput, vmOutput);
                        } catch (Exception e) {
                            System.out.println("Error processing file: " + jackFile.getName());
                            e.printStackTrace();
                        }finally {
                          if(compilationEngine != null){
                              compilationEngine.close(); // Ensure the writer is closed after processing each file
                              compilationEngine = null; // Reset the compilation engine for the next file
                          }
                        }
                       


                    }
                }
            } else if (file.isFile() && inputFile.endsWith(".jack")) {
            
                CompilationEngine compilationEngine = null;
                try {
                    System.out.println("For File Name : " + file.getName()+ "Absolute path is file: " + file.getAbsolutePath());
                    File outputFile = new File(file.getAbsolutePath().replace(".jack", ".xml"));

                    String vmOutput = file.getAbsolutePath().replace(".jack", ".vm");

                    compilationEngine = new CompilationEngine(file, outputFile, vmOutput);
                } catch (Exception e) {
                    System.out.println("Error processing file: " + file.getName());
                    e.printStackTrace();
                    if(compilationEngine != null){
                    compilationEngine.close(); // Ensure the writer is closed after processing each file
                    compilationEngine = null; // Reset the compilation engine for the next file
                    }
                }finally {
                    if(compilationEngine != null){
                        compilationEngine.close(); // Ensure the writer is closed after processing each file
                            compilationEngine = null; // Reset the compilation engine for the next file
                        }
                }
            } else {
                System.out.println("Invalid input for path " + inputFile );
            }
        }
    }
    
} 
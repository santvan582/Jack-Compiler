package JackAnalyzer;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;


public class VMWriter {
    
    private PrintWriter printWriter;

   //Constructor that opens the output file and gets ready to write into it
    public VMWriter(String vmOutputFile) {
        try {
            this.printWriter = new PrintWriter(vmOutputFile);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    //Write VM push command
    public void writePush(SEGMENT segment, int index) {
        printWriter.println("   "+"push"+ " " + segment.getSegmentInString() + " " + index);
    }

    //Write VM pop command
    public void writePop(SEGMENT segment, int index) {
        printWriter.println("   "+"pop"+ " " + segment.getSegmentInString() + " " + index);
    }
  
    //Write VM arithmetic-logical command
    public void writeArithmetic(COMMAND command) {
        if(command == COMMAND.MULTIPLY) {
            printWriter.println("   "+"call Math.multiply 2");
        } else if(command == COMMAND.DIVIDE) {
            printWriter.println("   "+"call Math.divide 2");
        } else {
            printWriter.println("   "+command.getCommandInString());
        }
    }

    //Write VM label command
    public void writeLabel(String label) {
        printWriter.println("label" + " " + label);
    }
    
    //Write VM goto command
    public void writeGoto(String label) {
        printWriter.println("   "+"goto" + " " + label);
    }

    //Write VM if-goto command
    public void writeIf(String label) {
        printWriter.println("   "+"if-goto" + " " + label);
    }

    public void writeCall(String name, int nArgs) {
        printWriter.println("   "+"call" + " " + name + " " + nArgs);
    }

    public void writeFunction(String name, int nLocals) {
        printWriter.println("function" + " " + name + " " + nLocals);
    }

    public void writeReturn() {
        printWriter.println("   "+"return");
    }

    //Closes the output file
    public void close() {
        printWriter.close();
    }

    enum SEGMENT {
        CONSTANT, 
        ARGUMENT, 
        LOCAL, 
        STATIC, 
        THIS, 
        THAT, 
        POINTER, 
        TEMP;


   
        public String getSegmentInString() {
            switch (this) {
                case CONSTANT:
                    return "constant";
                case ARGUMENT:
                    return "argument";
                case LOCAL:
                    return "local";
                case STATIC:
                    return "static";
                case THIS:
                    return "this";
                case THAT:
                    return "that";
                case POINTER:
                    return "pointer";
                case TEMP:
                    return "temp";
                default:
                    throw new IllegalArgumentException("Invalid segment: " + this);
            }
        }


        public static SEGMENT getSegmentFromKind(SymbolTable.KIND kind) {
            switch (kind) {
                case STATIC:
                    return STATIC;
                case FIELD:
                    return THIS; // 'this' segment is used for field variables
                case ARG:
                    return ARGUMENT;
                case VAR:
                    return LOCAL;    
                default:
                    throw new IllegalArgumentException("Invalid kind: " + kind);
            }
        }

    }

    enum COMMAND {
        ADD, 
        SUB, 
        NEG, 
        EQ, 
        GT, 
        LT, 
        AND, 
        OR, 
        NOT,
        MULTIPLY,
        DIVIDE;

        public String getCommandInString() {
            switch (this) {
                case ADD:
                    return "add";
                case SUB:
                    return "sub";
                case NEG:
                    return "neg";
                case EQ:
                    return "eq";
                case GT:
                    return "gt";
                case LT:
                    return "lt";
                case AND:
                    return "and";
                case OR:
                    return "or";
                case NOT:
                    return "not";
                default:
                    throw new IllegalArgumentException("Invalid command: " + this);
            }
        }

            public static COMMAND getCommandFromOp(char symbol) {
            switch (symbol) {
                case '~':
                    return NOT;
                case '+':
                    return ADD;
                case '-':
                    return SUB;
                case '*':
                    return MULTIPLY; // Multiplication is handled by a function call
                case '/':
                    return DIVIDE; // Division is handled by a function call
                case '&':
                    return AND;
                case '|':
                    return OR;
                case '<':
                    return LT;
                case '>':
                    return GT;
                case '=':
                    return EQ;
                default:
                    throw new IllegalArgumentException("Invalid symbol: " + symbol);
            }
        }

        
    }



}

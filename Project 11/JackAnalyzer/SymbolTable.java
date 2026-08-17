package JackAnalyzer;

import java.util.HashMap;

public class SymbolTable {


    private HashMap<String,Symbol> classSymbols;//for STATIC, FIELD
    private HashMap<String,Symbol> subroutineSymbols;//for ARG, VAR

    
    
  public SymbolTable() {
      classSymbols = new HashMap<String,Symbol>();
      subroutineSymbols = new HashMap<String,Symbol>();

      classSymbols.put("STATIC", new Symbol("STATIC", KIND.STATIC, 0));
      classSymbols.put("FIELD", new Symbol("FIELD", KIND.FIELD, 0));
      subroutineSymbols.put("ARG", new Symbol("ARG", KIND.ARG, 0));
      subroutineSymbols.put("VAR", new Symbol("VAR", KIND.VAR, 0));
  }

 

 
 //Add New Symbol to the Table 
  public void define(String name, String type, KIND kind) {
      if (kind == KIND.STATIC || kind == KIND.FIELD) {
          Symbol symbol = classSymbols.get(kind.toString());
          classSymbols.put(name, new Symbol(type, kind, symbol.getIndex()));
          classSymbols.put(kind.toString(), new Symbol(kind.toString(), kind, symbol.getIndex() + 1));
      } else if (kind == KIND.ARG || kind == KIND.VAR) {
          Symbol symbol = subroutineSymbols.get(kind.toString());
          subroutineSymbols.put(name, new Symbol(type, kind, symbol.getIndex()));
          subroutineSymbols.put(kind.toString(), new Symbol(kind.toString(), kind, symbol.getIndex() + 1));
      }

    //   System.out.println("<----------------------------------------------------------->");
    //   System.out.println("New State After Update Symbol Table: ");
      for(String key : classSymbols.keySet()) {
          Symbol symbol = classSymbols.get(key);
        //   System.out.println("Class Symbol - Name: " + key + ", Type: " + symbol.getType() + ", Kind: " + symbol.getKind() + ", Index: " + symbol.getIndex());
      }
      for(String key : subroutineSymbols.keySet()) {
          Symbol symbol = subroutineSymbols.get(key);
        //   System.out.println("Subroutine Symbol - Name: " + key + ", Type: " + symbol.getType() + ", Kind: " + symbol.getKind() + ", Index: " + symbol.getIndex());
      }

  }


  //Returns the number of variables of the given kind already defined in the current scope
  public int varCount(KIND kind) {
      if (kind == KIND.STATIC || kind == KIND.FIELD) {
          return classSymbols.get(kind.toString()).getIndex();
      } else if (kind == KIND.ARG || kind == KIND.VAR) {
          return subroutineSymbols.get(kind.toString()).getIndex();
      }
      return 0;
  }


  //Returns the kind of the named identifier in the current scope. If the identifier is unknown, returns NONE.
  public KIND kindOf(String name) {
      if (classSymbols.containsKey(name)) {
          return classSymbols.get(name).getKind();
      } else if (subroutineSymbols.containsKey(name)) {
          return subroutineSymbols.get(name).getKind();
      }
      return KIND.NONE;
  }

  
  //Returns the type of the named identifier in the current scope
  public String typeOf(String name) {
      if (classSymbols.containsKey(name)) {
          return classSymbols.get(name).getType();
      } else if (subroutineSymbols.containsKey(name)) {
          return subroutineSymbols.get(name).getType();
      }
      return name;
  }


  //Return Index of Named Variable
  public int indexOf(String name) {
      if (classSymbols.containsKey(name)) {
          return classSymbols.get(name).getIndex();
      } else if (subroutineSymbols.containsKey(name)) {
          return subroutineSymbols.get(name).getIndex();
      }
      return -1;
  }

  public String getMethodName(String name) {
    
    for (String key : classSymbols.keySet()) {
        if (key.compareToIgnoreCase(name) == 0) {
            return key;
        }
    }
    for (String key : subroutineSymbols.keySet()) {
        if (key.compareToIgnoreCase(name) == 0) {
            return key;
        }
    }
      return name;
  }



 //Empties the subroutine symbol table and resets all four index to 0
  public void reset() {
      subroutineSymbols.clear();
      classSymbols.put("STATIC", new Symbol("STATIC", KIND.STATIC, 0));
      classSymbols.put("FIELD", new Symbol("FIELD", KIND.FIELD, 0));
      subroutineSymbols.put("ARG", new Symbol("ARG", KIND.ARG, 0));
      subroutineSymbols.put("VAR", new Symbol("VAR", KIND.VAR, 0));
  }

  //Clears ARG and VAR when starting a new subroutine
  public void resetSubroutine() {
      subroutineSymbols.clear();
    //    System.out.println("<----------------------------------------------------------->");
    //    System.out.println("\nResetting Subroutine Symbol Table for New Subroutine\n");
    //    System.out.println("<----------------------------------------------------------->");
      subroutineSymbols.put("ARG", new Symbol("ARG", KIND.ARG, 0));
      subroutineSymbols.put("VAR", new Symbol("VAR", KIND.VAR, 0));
  }


  enum KIND {
        STATIC,
        FIELD,
        ARG,
        VAR,
        NONE;


        static KIND fromString(String kind) {
            switch (kind.toUpperCase()) {
                case "STATIC":
                    return STATIC;
                case "FIELD":
                    return FIELD;
                case "ARG":
                    return ARG;
                case "VAR":
                    return VAR;
                default:
                    return NONE;
            }
        }
    }


  private class Symbol {
        private String type;
        private KIND kind;
        private int index;

        public Symbol(String type, KIND kind, int index) {
            this.type = type;
            this.kind = kind;
            this.index = index;
        }

        public String getType() {
            return type;
        }

        public KIND getKind() {
            return kind;
        }

        public int getIndex() {
            return index;
        }
    }  
}

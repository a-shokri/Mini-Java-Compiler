package java_cup.runtime;

/**
 * Default Implementation for SymbolFactory, creates
 * plain old Symbols
 *
 * @version last updated 27-03-2006
 * @author Michael Petter
 */

/* *************************************************
  class DefaultSymbolFactory

  interface for creating new symbols  
 ***************************************************/
public class DefaultSymbolFactory implements SymbolFactory {
    // Factory methods
    /**
     * DefaultSymbolFactory for CUP.
     * Users are strongly encoraged to use ComplexSymbolFactory instead, since
     * it offers more detailed information about Symbols in source code.
     * Yet since migrating has always been a critical process, You have the
     * chance of still using the oldstyle Symbols.
     *
     * @deprecated as of CUP v11a
     * replaced by the new java_cup.runtime.ComplexSymbolFactory
     */
    //@deprecated 
    public DefaultSymbolFactory(){
    }
    public java_cup.runtime.Symbol newSymbol(String name , int id, java_cup.runtime.Symbol left, java_cup.runtime.Symbol right, Object value){
        return new java_cup.runtime.Symbol(id,left,right,value);
    }
    public java_cup.runtime.Symbol newSymbol(String name, int id, java_cup.runtime.Symbol left, java_cup.runtime.Symbol right){
        return new java_cup.runtime.Symbol(id,left,right);
    }
    public java_cup.runtime.Symbol newSymbol(String name, int id, int left, int right, Object value){
        return new java_cup.runtime.Symbol(id,left,right,value);
    }
    public java_cup.runtime.Symbol newSymbol(String name, int id, int left, int right){
        return new java_cup.runtime.Symbol(id,left,right);
    }
    public java_cup.runtime.Symbol startSymbol(String name, int id, int state){
        return new java_cup.runtime.Symbol(id,state);
    }
    public java_cup.runtime.Symbol newSymbol(String name, int id){
        return new java_cup.runtime.Symbol(id);
    }
    public java_cup.runtime.Symbol newSymbol(String name, int id, Object value){
        return new Symbol(id,value);
    }
}

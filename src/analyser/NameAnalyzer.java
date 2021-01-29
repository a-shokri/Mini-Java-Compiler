package analyser;

import analyser.errors.AnalysisError;
import analyser.errors.name.*;
import analyser.errors.type.ExpectedTypeNotFoundError;
import analyser.errors.type.TypeAnalysisError;
import ast.*;

import java.util.*;

public class NameAnalyzer extends Tree implements Visitor<List<AnalysisError>> {

    private SymbolTable symbolTable = new SymbolTable();
    public List<AnalysisError> errorList = new ArrayList<>();
    public Program program;
    private ArrayList<String> methodsList = new ArrayList<>();
    boolean onlyNameAnalysis = false;


    public NameAnalyzer(Program program, boolean onlyNameAnalysis) {
        this.program = program;
        this.onlyNameAnalysis = onlyNameAnalysis;
        symbolTable = new SymbolTable();
    }

    public void nameAnalysis() {

        //Tree astTree = parser.parseTree();
        //SymbolTable symbolTable = new SymbolTable();
        errorList = program.accept(this);

    }

    @Override
    public List<AnalysisError> visit(Program n) {

        List<AnalysisError> errors = new ArrayList<>();
        List<ClassSymbol> classSymbolList = new ArrayList<>();

        try {
            classSymbolList.add(new ClassSymbol(n.mainClass, symbolTable));
        } catch (DupplicateClassDefinitionError dupplicateClassDefinitionError) {
            errors.add(dupplicateClassDefinitionError);
        }

        // Collecting Classes Symbols, regardless of their extending part, methods and global variables.
        for (ClassDeclaration classDeclaration : n.getClassList()) {
            try {
                classSymbolList.add(new ClassSymbol(classDeclaration, symbolTable));
            } catch (DupplicateClassDefinitionError dupplicateClassDefinitionError) {
                errors.add(dupplicateClassDefinitionError);
            }
        }

        // Gathering information about the extending part of each class and adding them to symbol of each class
        for (ClassSymbol classSymbol : classSymbolList) {
            errors.addAll(classSymbol.applyExtendedClass());
        }

        // Checking cycle in inheritance. If there be any, program returns error and does not continue.
        List<AnalysisError> interitanceCycleErrorList = checkExtendsCycle(classSymbolList, n.classList);
        errors.addAll(interitanceCycleErrorList);

        if (!interitanceCycleErrorList.isEmpty())
            return errors;


        // Creating symbols for the methods and global variables of each class and adding them to SymbolTable
        for (ClassSymbol classSymbol : classSymbolList) {
            errors.addAll(classSymbol.applyMethodAndVariable());
        }


        // Checking field overloading (field duplication is handled before)
        List<AnalysisError> fieldOverloadingErrorList = checkFieldOverriding(classSymbolList);
        errors.addAll(fieldOverloadingErrorList);
        if (!fieldOverloadingErrorList.isEmpty())
            return errors;

        // Checking method overloading and overriding with mistmatch number of arguments and type of them
        List<AnalysisError> methodOverloadingAndOverridingErrorList = checkMethodOverloadingAndOverriding(classSymbolList);
        errors.addAll(methodOverloadingAndOverridingErrorList);
        if (!methodOverloadingAndOverridingErrorList.isEmpty())
            return errors;


        for (ClassSymbol classSymbol : classSymbolList) {

            if (classSymbol.getClassDeclaration() != null) { // It is not the main class
                errors.addAll(classSymbol.getClassDeclaration().accept(this));
            } else {
                errors.addAll(classSymbol.getMainClass().accept(this));
            }
        }

        return errors;


    }


    private List<AnalysisError> checkMethodOverloadingAndOverriding(List<ClassSymbol> classSymbolList) {
        List<AnalysisError> errors = new ArrayList<>();
        for (ClassSymbol classSymbol : classSymbolList)

            for (MethodSymbol methodSymbol : classSymbol.methodSymbols) {

                MethodSymbol preDefinedMethodSymbol = null;

                try {
                    preDefinedMethodSymbol = (MethodSymbol) symbolTable.findCorrespondingSymbolInAncestorClasses(methodSymbol.methodIdenifier != null ? methodSymbol.methodIdenifier : methodSymbol.methodDeclaration.methodName, false, true);
                } catch (IdentifierNotDefinedError identifierNotDefinedError) {
                }

                if (preDefinedMethodSymbol != null && !preDefinedMethodSymbol.equals(methodSymbol)) {
                    boolean overloaded = false;
                    if (methodSymbol.getArgumentNumber() != preDefinedMethodSymbol.getArgumentNumber()) {
                        overloaded = true;
                    } else if(!onlyNameAnalysis){ // Here, number of arguments are the same, however type of arguments and the return type of the methods should be checked
                        for (int i = 0; i < methodSymbol.getMethodDeclaration().typeIdentifiers.size(); i++) {
                            Map<Type, Identifier> methodVar = methodSymbol.getMethodDeclaration().typeIdentifiers.get(i);
                            Map<Type, Identifier> prevDefinedMethodVar = preDefinedMethodSymbol.getMethodDeclaration().typeIdentifiers.get(i);
                            Type.TypeName methodVarTypeName = methodVar.keySet().iterator().next().typeName;
                            Type.TypeName prevDefinedMethodVarTypeName = prevDefinedMethodVar.keySet().iterator().next().typeName;

                            if (!methodVarTypeName.equals(prevDefinedMethodVarTypeName)) {
                                overloaded = true;
                                break;
                            }
                            if (methodVarTypeName.equals(Type.TypeName.IDENTIFIER)) {
                                if (!methodVar.values().iterator().next().getSymbol().equals(prevDefinedMethodVar.values().iterator().next().getSymbol())) {
                                    overloaded = true;
                                    break;
                                }
                            }
                        }
                        // Checking return type
                        if (!methodSymbol.methodDeclaration.methodType.typeName.equals(preDefinedMethodSymbol.methodDeclaration.methodType.typeName)) {
                            overloaded = true;
                        }
                        if (methodSymbol.methodDeclaration.methodType.typeName.equals(Type.TypeName.IDENTIFIER)) {
                            if (!methodSymbol.methodDeclaration.methodType.getSymbol().equals(preDefinedMethodSymbol.methodDeclaration.methodType.getSymbol())) {
                                overloaded = true;
                                break;
                            }
                        }
                    }
                    if (overloaded) {
                        // the method is already defined in it's ancestors and their argument number are not the same
                        Identifier identifier = methodSymbol.methodIdenifier != null ? methodSymbol.methodIdenifier : methodSymbol.methodDeclaration.methodName;
                        errors.add(new MethodDefinitionOverloading(identifier.idName, identifier.getLine(),
                                identifier.getColumn(), preDefinedMethodSymbol));
                    }
                }

            }

        return errors;

    }

    private List<AnalysisError> checkFieldOverriding(List<ClassSymbol> classSymbolList) {
        List<AnalysisError> errors = new ArrayList<>();
        for (ClassSymbol classSymbol : classSymbolList)
            for (VarSymbol varSymbol : classSymbol.globalVariableSymbols) {
                VarSymbol preDefinedVarSymbol = null;

                try {
                    preDefinedVarSymbol = (VarSymbol) symbolTable.findCorrespondingSymbolInAncestorClasses(varSymbol.varIdentifier, true, false);
                } catch (IdentifierNotDefinedError identifierNotDefinedError) {
                }

                if (preDefinedVarSymbol != null && !preDefinedVarSymbol.equals(varSymbol)) // the variable is already defined in this class or it's ancestors
                    errors.add(new DuplicateVariableDefinitionError(varSymbol.varIdentifier.idName, varSymbol.varIdentifier.getLine(),
                            varSymbol.varIdentifier.getColumn(), preDefinedVarSymbol));
            }
        return errors;
    }

    private List<AnalysisError> checkExtendsCycle(List<ClassSymbol> classSymbolList, List<ClassDeclaration> classDeclarationList) {
        List<AnalysisError> errors = new ArrayList<>();
        Set<ClassSymbol> classNameSet = new HashSet<>();

        for (ClassSymbol classSymbol : classSymbolList) {

            classNameSet.clear();
            ClassSymbol tmp = classSymbol;
            classNameSet.add(classSymbol);

            while (tmp.extendedClassSymbol != null && !classNameSet.contains(tmp.extendedClassSymbol)) {
                classNameSet.add(tmp.extendedClassSymbol);
                tmp = tmp.extendedClassSymbol;
            }

            if (tmp.extendedClassSymbol != null) {

                Identifier tmpClassDeclarationIdentifier = null;

                for (ClassDeclaration classDeclaration : classDeclarationList) {
                    if (classDeclaration.extendsIdentifier != null && classDeclaration.extendsIdentifier.getSymbol().equals(classSymbol))
                        tmpClassDeclarationIdentifier = classDeclaration.extendsIdentifier;
                }

                errors.add(new ClassDefinitionInheritanceCycleError(tmp, classSymbol, tmpClassDeclarationIdentifier));

            }

        }

        return errors;
    }

    @Override
    public List<AnalysisError> visit(Expression n) {

        List<AnalysisError> errors = new ArrayList<>();

        if (n.t != null) errors.addAll(n.t.accept(this));
        if (n.a != null) errors.addAll(n.a.accept(this));


        if(onlyNameAnalysis)
            return errors;

        if( !errors.isEmpty() )
            // There are some name checking errors. Those errors should be fixed before doing type checking. Therefore, we
            // return errors here.
            return errors;

        // Type checking
        if (n.a != null) { // It is a boolean expression (b1 | b2)
            if (/*n.a.getNodeType() == null || */!n.a.getNodeType().typeName.equals(Type.TypeName.BOOLEAN))
                errors.add(new ExpectedTypeNotFoundError(Type.TypeName.BOOLEAN, n.a.getNodeType(), n));
            if (/*n.t.getNodeType() == null || */!n.t.getNodeType().typeName.equals(Type.TypeName.BOOLEAN))
                errors.add(new ExpectedTypeNotFoundError(Type.TypeName.BOOLEAN, n.t.getNodeType(), n));
        }

        // t is either boolean or not. In both cases, the type of the expression will be the type of t
        n.setNodeType(n.t.getNodeType());


        return errors;

    }

    @Override
    public List<AnalysisError> visit(MethodDeclaration methodDeclaration) {

        List<AnalysisError> errors = new ArrayList<>();
        try {

            MethodSymbol methodSymbol = symbolTable.getMethodSymbol(((ClassDeclaration) methodDeclaration.getParent()).classIdentifier, methodDeclaration.methodName, methodDeclaration.typeIdentifiers.size());

            /*methodsList.add(methodDeclaration.methodName.idName);
            for (int i = 0; i < methodsList.index(); i++) {
                if (methodsList.get(i).equals(methodDeclaration.methodName.idName))
                    errors.add(new DuplicateMethodError(methodDeclaration.methodName.idName, methodDeclaration.methodName.getLine(),
                            methodDeclaration.methodName.getColumn(), methodSymbol));
            }*/

            errors.addAll(methodSymbol.applyMethodVariables());

        } catch (ClassDefinitionNotFoundError classDefinitionNotFoundError) {
            errors.add(classDefinitionNotFoundError);
        } catch (MethodDefinitionNotFoundError methodDefinitionNotFoundError) {
            errors.add(methodDefinitionNotFoundError);
        }

        if (methodDeclaration.statements != null)
            for (Statement statement : methodDeclaration.statements)
                errors.addAll(statement.accept(this));

        errors.addAll(methodDeclaration.returnExpression.accept(this));

        return errors;

    }

    @Override
    public List<AnalysisError> visit(A a) {

        List<AnalysisError> errors = new ArrayList<>();

        if (a.t != null) errors.addAll(a.t.accept(this));
        if (a.a != null) errors.addAll(a.a.accept(this));

        if(onlyNameAnalysis)
            return errors;

        if( !errors.isEmpty() )
            // There are some name checking errors. Those errors should be fixed before doing type checking. Therefore, we
            // return errors here.
            return errors;

        // Type checking
        if (a.a != null) { // It is a boolean expression (b1 | b2)
            if (/*n.a.getNodeType() == null || */!a.a.getNodeType().typeName.equals(Type.TypeName.BOOLEAN))
                errors.add(new ExpectedTypeNotFoundError(Type.TypeName.BOOLEAN, a.a.getNodeType(), a));
            if (/*n.t.getNodeType() == null || */!a.t.getNodeType().typeName.equals(Type.TypeName.BOOLEAN))
                errors.add(new ExpectedTypeNotFoundError(Type.TypeName.BOOLEAN, a.t.getNodeType(), a));
        }

        a.setNodeType(a.t.getNodeType());

        return errors;

    }

    @Override
    public List<AnalysisError> visit(B b) {

        List<AnalysisError> errors = new ArrayList<>();

        if (b.f != null) errors.addAll(b.f.accept(this));
        if (b.b != null) errors.addAll(b.b.accept(this));

        if(onlyNameAnalysis)
            return errors;

        if( !errors.isEmpty() )
            // There are some name checking errors. Those errors should be fixed before doing type checking. Therefore, we
            // return errors here.
            return errors;

        // Type checking
        if (b.b != null) { // It is a boolean expression (b1 | b2)
            if (/*n.a.getNodeType() == null || */!b.b.getNodeType().typeName.equals(Type.TypeName.BOOLEAN))
                errors.add(new ExpectedTypeNotFoundError(Type.TypeName.BOOLEAN, b.b.getNodeType(), b));
            if (/*n.t.getNodeType() == null || */!b.f.getNodeType().typeName.equals(Type.TypeName.BOOLEAN))
                errors.add(new ExpectedTypeNotFoundError(Type.TypeName.BOOLEAN, b.f.getNodeType(), b));
        }

        b.setNodeType(b.f.getNodeType());

        return errors;

    }

    @Override
    public List<AnalysisError> visit(C c) {

        List<AnalysisError> errors = new ArrayList<>();

        if (c.g != null) errors.addAll(c.g.accept(this));
        if (c.c != null) errors.addAll(c.c.accept(this));

        if(onlyNameAnalysis)
            return errors;

        if( !errors.isEmpty() )
            // There are some name checking errors. Those errors should be fixed before doing type checking. Therefore, we
            // return errors here.
            return errors;

        // Type checking
        if (c.c != null) { // It is a boolean expression (e1 == e2) or (e1 < e2)
            // The case of "Comparison operator" which mentioned in the assignment web page
            if (c.c.getO().equals(C.OperatorType.EQUAL)) {
                // If both are primitives, they have to be the same primitive type, otherwise
                if (!c.g.getNodeType().typeName.equals(Type.TypeName.IDENTIFIER) && !c.c.getNodeType().typeName.equals(Type.TypeName.IDENTIFIER)) {
                    errors.addAll(simpleTypeCheck(c.g.getNodeType(), c.c.getNodeType(), c));
                }
                // If one is primitive and the other one is class type, it is a type Error
                else if (c.g.getNodeType().typeName.equals(Type.TypeName.IDENTIFIER) && !c.c.getNodeType().typeName.equals(Type.TypeName.IDENTIFIER) ||
                        !c.g.getNodeType().typeName.equals(Type.TypeName.IDENTIFIER) && c.c.getNodeType().typeName.equals(Type.TypeName.IDENTIFIER)
                        ) {
                    errors.add(new ExpectedTypeNotFoundError(c.g.getNodeType(), c.c.getNodeType(), c));
                }
                // In the case that both are classes we do not type check them in equality

            } else { // It is other comparisons (>, <, <=, >=). Therefore, the operands must be of int or int[] and also both operands should have the same type.
                if (c.g.getNodeType().typeName.equals(Type.TypeName.INT) || c.g.getNodeType().typeName.equals(Type.TypeName.INT_ARRAY)) {
                    errors.addAll(simpleTypeCheck(c.g.getNodeType(), c.c.getNodeType(), c));
                } else {
                    Type.TypeName[] tn = {Type.TypeName.INT, Type.TypeName.INT_ARRAY};
                    errors.add(new ExpectedTypeNotFoundError(tn, c.c.getNodeType(), c));
                }

            }
            c.setNodeType(new Type(Type.TypeName.BOOLEAN));
        } else
            c.setNodeType(c.g.getNodeType());

        return errors;

    }

    @Override
    public List<AnalysisError> visit(ClassDeclaration classDeclaration) {

        List<AnalysisError> errors = new ArrayList<>();

        // As we collected methods and global variables information before (in program method), we just simply call method declarations here.
        for (MethodDeclaration methodDeclaration : classDeclaration.methodDeclarations) {
            errors.addAll(methodDeclaration.accept(this));
        }

        methodsList.clear();

        return errors;

    }

    @Override
    public List<AnalysisError> visit(D d) {

        List<AnalysisError> errors = new ArrayList<>();

        if (d.h != null) errors.addAll(d.h.accept(this));
        if (d.d != null) errors.addAll(d.d.accept(this));

        if(onlyNameAnalysis)
            return errors;

        if( !errors.isEmpty() )
            // There are some name checking errors. Those errors should be fixed before doing type checking. Therefore, we
            // return errors here.
            return errors;


        // Type checking
        if (d.d != null) { // It is a plus or minus expression (e1 + e2 or e1 - e2)
            if (d.d.getO().equals(D.OperatorType.MINUS)) { // simply check if operands are int or int[]
                if (d.h.getNodeType().typeName.equals(Type.TypeName.INT) || d.h.getNodeType().typeName.equals(Type.TypeName.INT_ARRAY)) {
                    errors.addAll(simpleTypeCheck(d.d.getNodeType(), d.h.getNodeType(), d));
                } else {
                    Type.TypeName[] tn = {Type.TypeName.INT, Type.TypeName.INT_ARRAY};
                    errors.add(new ExpectedTypeNotFoundError(tn, d.h.getNodeType(), d));
                }
                d.setNodeType(d.h.getNodeType());
            } else { // In the case that the operator is +, if one of the operands is String, then the type will be string, otherwise int
                if (d.h.getNodeType().typeName.equals(Type.TypeName.INT) || d.h.getNodeType().typeName.equals(Type.TypeName.INT_ARRAY) ||
                        d.h.getNodeType().typeName.equals(Type.TypeName.STRING) || d.h.getNodeType().typeName.equals(Type.TypeName.STRING_ARR)) {
                    if (d.h.getNodeType().typeName.equals(Type.TypeName.STRING) || d.h.getNodeType().typeName.equals(Type.TypeName.STRING_ARR))
                        d.setNodeType(d.h.getNodeType());
                    else if (d.d.getNodeType().typeName.equals(Type.TypeName.STRING) || d.d.getNodeType().typeName.equals(Type.TypeName.STRING_ARR))
                        d.setNodeType(d.d.getNodeType());
                    else
                        d.setNodeType(d.h.getNodeType());
                } else {
                    Type.TypeName[] tn = {Type.TypeName.INT, Type.TypeName.INT_ARRAY, Type.TypeName.STRING, Type.TypeName.STRING_ARR};
                    errors.add(new ExpectedTypeNotFoundError(tn, d.h.getNodeType(), d));
                }
            }
        }

        if (d.getNodeType() == null) // There were errors within the above type checkings
            d.setNodeType(d.h.getNodeType());
        return errors;

    }

    @Override
    public List<AnalysisError> visit(F f) {

        List<AnalysisError> errors = new ArrayList<>();
        if (f.g != null) errors.addAll(f.g.accept(this));
        if (f.c != null) errors.addAll(f.c.accept(this));

        if(onlyNameAnalysis)
            return errors;

        if( !errors.isEmpty() )
            // There are some name checking errors. Those errors should be fixed before doing type checking. Therefore, we
            // return errors here.
            return errors;

        // Type checking
        if (f.c != null) { // It is a boolean expression (e1 == e2) or (e1 < e2)
            // The case of "Comparison operator" which mentioned in the assignment web page
            if (f.c.getO().equals(C.OperatorType.EQUAL)) {
                // If both are primitives, they have to be the same primitive type, otherwise
                if (!f.g.getNodeType().typeName.equals(Type.TypeName.IDENTIFIER) && !f.c.getNodeType().typeName.equals(Type.TypeName.IDENTIFIER)) {
                    errors.addAll(simpleTypeCheck(f.g.getNodeType(), f.c.getNodeType(), f));
                }
                // If one is primitive and the other one is class type, it is a type Error
                else if (f.g.getNodeType().typeName.equals(Type.TypeName.IDENTIFIER) && !f.c.getNodeType().typeName.equals(Type.TypeName.IDENTIFIER) ||
                        !f.g.getNodeType().typeName.equals(Type.TypeName.IDENTIFIER) && f.c.getNodeType().typeName.equals(Type.TypeName.IDENTIFIER)
                        ) {
                    errors.add(new ExpectedTypeNotFoundError(f.g.getNodeType(), f.c.getNodeType(), f));
                }
                // In the case that both are classes we do not type check them in equality

            } else { // It is other comparisons (>, <, <=, >=). Therefore, the operands must be of int or int[] and also both operands should have the same type.
                if (f.g.getNodeType().typeName.equals(Type.TypeName.INT) || f.g.getNodeType().typeName.equals(Type.TypeName.INT_ARRAY)) {
                    errors.addAll(simpleTypeCheck(f.g.getNodeType(), f.c.getNodeType(), f));
                } else {
                    Type.TypeName[] tn = {Type.TypeName.INT, Type.TypeName.INT_ARRAY};
                    errors.add(new ExpectedTypeNotFoundError(tn, f.c.getNodeType(), f));
                }

            }
            f.setNodeType(new Type(Type.TypeName.BOOLEAN));
        } else
            f.setNodeType(f.g.getNodeType());

        return errors;

    }

    @Override
    public List<AnalysisError> visit(G g) {

        List<AnalysisError> errors = new ArrayList<>();
        if (g.h != null) errors.addAll(g.h.accept(this));
        if (g.d != null) errors.addAll(g.d.accept(this));

        if(onlyNameAnalysis)
            return errors;

        if( !errors.isEmpty() )
            // There are some name checking errors. Those errors should be fixed before doing type checking. Therefore, we
            // return errors here.
            return errors;

        // Type checking
        if (g.d != null) { // It is a plus or minus expression (e1 + e2 or e1 - e2)
            if (g.d.getO().equals(D.OperatorType.MINUS)) { // simply check if operands are int or int[]
                if (g.h.getNodeType().typeName.equals(Type.TypeName.INT) || g.h.getNodeType().typeName.equals(Type.TypeName.INT_ARRAY)) {
                    errors.addAll(simpleTypeCheck(g.d.getNodeType(), g.h.getNodeType(), g));
                } else {
                    Type.TypeName[] tn = {Type.TypeName.INT, Type.TypeName.INT_ARRAY};
                    errors.add(new ExpectedTypeNotFoundError(tn, g.h.getNodeType(), g));
                }
                g.setNodeType(g.h.getNodeType());
            } else { // In the case that the operator is +, if one of the operands is String, then the type will be string, otherwise int
                if (g.h.getNodeType().typeName.equals(Type.TypeName.INT) || g.h.getNodeType().typeName.equals(Type.TypeName.INT_ARRAY) ||
                        g.h.getNodeType().typeName.equals(Type.TypeName.STRING) || g.h.getNodeType().typeName.equals(Type.TypeName.STRING_ARR)) {
                    if (g.h.getNodeType().typeName.equals(Type.TypeName.STRING) || g.h.getNodeType().typeName.equals(Type.TypeName.STRING_ARR))
                        g.setNodeType(g.h.getNodeType());
                    else if (g.d.getNodeType().typeName.equals(Type.TypeName.STRING) || g.d.getNodeType().typeName.equals(Type.TypeName.STRING_ARR))
                        g.setNodeType(g.d.getNodeType());
                    else
                        g.setNodeType(g.h.getNodeType());
                } else {
                    Type.TypeName[] tn = {Type.TypeName.INT, Type.TypeName.INT_ARRAY, Type.TypeName.STRING, Type.TypeName.STRING_ARR};
                    errors.add(new ExpectedTypeNotFoundError(tn, g.h.getNodeType(), g));
                }
            }
        }

        if (g.getNodeType() == null) // There were errors within the above type checkings
            g.setNodeType(g.h.getNodeType());

        return errors;

    }

    @Override
    public List<AnalysisError> visit(H h) {

        List<AnalysisError> errors = new ArrayList<>();
        if (h.q != null) errors.addAll(h.q.accept(this));
        if (h.j != null) errors.addAll(h.j.accept(this));

        if(onlyNameAnalysis)
            return errors;

        if( !errors.isEmpty() )
            // There are some name checking errors. Those errors should be fixed before doing type checking. Therefore, we
            // return errors here.
            return errors;

        // Type checking
        if (h.j != null) { // It is a mult or div expression (e1 * e2 or e1 / e2)
            // simply check if operands are int or int[]
            if (h.j.getNodeType().typeName.equals(Type.TypeName.INT) || h.j.getNodeType().typeName.equals(Type.TypeName.INT_ARRAY)) {
                errors.addAll(simpleTypeCheck(h.j.getNodeType(), h.j.getNodeType(), h));
            } else {
                Type.TypeName[] tn = {Type.TypeName.INT, Type.TypeName.INT_ARRAY};
                errors.add(new ExpectedTypeNotFoundError(tn, h.j.getNodeType(), h));
            }

        }

        h.setNodeType(h.q.getNodeType());
        return errors;

    }

    @Override
    public List<AnalysisError> visit(J j) {

        List<AnalysisError> errors = new ArrayList<>();
        if (j.q != null) errors.addAll(j.q.accept(this));
        if (j.j != null) errors.addAll(j.j.accept(this));

        if(onlyNameAnalysis)
            return errors;

        if( !errors.isEmpty() )
            // There are some name checking errors. Those errors should be fixed before doing type checking. Therefore, we
            // return errors here.
            return errors;

        // Type checking
        if (j.j != null) { // It is a mult or div expression (e1 * e2 or e1 / e2)
            // simply check if operands are int or int[]
            if (j.j.getNodeType().typeName.equals(Type.TypeName.INT) || j.j.getNodeType().typeName.equals(Type.TypeName.INT_ARRAY)) {
                errors.addAll(simpleTypeCheck(j.j.getNodeType(), j.j.getNodeType(), j));
            } else {
                Type.TypeName[] tn = {Type.TypeName.INT, Type.TypeName.INT_ARRAY};
                errors.add(new ExpectedTypeNotFoundError(tn, j.j.getNodeType(), j));
            }

        }

        j.setNodeType(j.q.getNodeType());

        return errors;

    }

    @Override
    public List<AnalysisError> visit(MainClass mainClass) {

        List<AnalysisError> errors = new ArrayList<>();


        for (Statement statement : mainClass.statements)
            errors.addAll(statement.accept(this));

        return errors;

    }

    @Override
    public List<AnalysisError> visit(StringLiteral stringLiteral) {
        return null;
    }

    @Override
    public List<AnalysisError> visit(T t) {

        List<AnalysisError> errors = new ArrayList<>();
        if (t.f != null) errors.addAll(t.f.accept(this));
        if (t.b != null) errors.addAll(t.b.accept(this));

        if(onlyNameAnalysis)
            return errors;

        if( !errors.isEmpty() )
            // There are some name checking errors. Those errors should be fixed before doing type checking. Therefore, we
            // return errors here.
            return errors;

        // Type checking
        if (t.b != null) { // It is a boolean expression (b1 && b2)
            if (/*t.f.getNodeType() == null || */!t.f.getNodeType().typeName.equals(Type.TypeName.BOOLEAN))
                errors.add(new ExpectedTypeNotFoundError(Type.TypeName.BOOLEAN, t.f.getNodeType(), t));
            if (/*t.b.getNodeType() == null || */!t.b.getNodeType().typeName.equals(Type.TypeName.BOOLEAN))
                errors.add(new ExpectedTypeNotFoundError(Type.TypeName.BOOLEAN, t.b.getNodeType(), t));
        }

        // f is either boolean or not. In both cases, the type of the expression will be the type of f
        t.setNodeType(t.f.getNodeType());

        return errors;

    }

    @Override
    public List<AnalysisError> visit(Type type) {
        return null;
    }

    List<AnalysisError> declareVariable(Type type, Identifier identifier) {

        List<AnalysisError> errors = new ArrayList<>();
/*        Identifier varIden = identifier;
        Symbol.EMJType emjType = Symbol.EMJType.STRING;
        case INT:
        emjType = Symbol.EMJType.INT;
        break;
        case STRING:
        emjType = Symbol.EMJType.STRING;
        break;
        case BOOLEAN:
        emjType = Symbol.EMJType.BOOLEAN;
        break;
        case INT_ARRAY:
        emjType = Symbol.EMJType.INT_ARR;
        break;
        case STRING_ARR:
        emjType = Symbol.EMJType.STRING_ARR;
        case IDENTIFIER:
        emjType = Symbol.EMJType.CLASS;

    }

    Symbol classSymbol = null;
    // Check whether the class is defined before or not
        if (emjType.equals(Symbol.EMJType.CLASS)) {
        classSymbol = symbolTable.getSymbol(type.identifier.idName);
        if (classSymbol == null)
            errors.add(new ClassDefinitionNotFoundError(type.identifier.idName,
                    type.identifier.getLine(), type.identifier.getColumn()));
    }

    Symbol symbol = new Symbol(varIden.idName, Symbol.SymbolType.VAR_NAME, emjType,
            varIden.getLine(), varIden.getColumn());
        symbol.setTypeSymbol(classSymbol);
        symbolTable.addSymbol(varIden.idName, symbol);
        varIden.setSymbol(symbolTable.getSymbol(varIden.idName));

 */

        return errors;

    }

    @Override
    public List<AnalysisError> visit(VarDeclaration varDeclaration) {
        return declareVariable(varDeclaration.type, varDeclaration.identifier);
    }

    @Override
    public List<AnalysisError> visit(Identifier identifier) {

        List<AnalysisError> errors = new ArrayList<>();
        try {
            Symbol symbol = symbolTable.findCorrespondingSymbol(identifier, true, false, false);
            identifier.setSymbol(symbol);
        } catch (IdentifierNotDefinedError identifierNotDefinedError) {
            errors.add(identifierNotDefinedError);
        }

/*        Symbol symbol = symbolTable.getSymbol(identifier.idName);

        if (symbol == null)
            errors.add(new IdentifierNotDefinedError(identifier.idName, identifier.getLine(), identifier.getColumn()));

        identifier.setSymbol(symbol);
*/
        return errors;

    }

    @Override
    public List<AnalysisError> visit(IntLiteral intLiteral) {
        return null;
    }

    @Override
    public List<AnalysisError> visit(Block block) {

        List<AnalysisError> errors = new ArrayList<>();


        //       symbolTable.newLevel(null);
        List<Statement> statements = new ArrayList<>();

        if (block.getSingleStatement() != null)
            statements.add(block.getSingleStatement());
        else
            statements.addAll(block.getBody());

        for (Statement statement : statements)
            errors.addAll(statement.accept(this));

        //     symbolTable.levelReturn();

        return errors;

    }

    @Override
    public List<AnalysisError> visit(Skip skip) {
        return null;
    }

    @Override
    public List<AnalysisError> visit(IfThenElse ifThenElse) {

        List<AnalysisError> errors = new ArrayList<>();
        errors.addAll(ifThenElse.expr.accept(this));

        if (ifThenElse.then != null)
            errors.addAll(ifThenElse.then.accept(this));

        if (ifThenElse.elze != null)
            errors.addAll(ifThenElse.elze.accept(this));

        if(onlyNameAnalysis)
            return errors;

        if( !errors.isEmpty() )
            // There are some name checking errors. Those errors should be fixed before doing type checking. Therefore, we
            // return errors here.
            return errors;

        errors.addAll(typeCheck(ifThenElse.expr.getNodeType(), Type.TypeName.BOOLEAN, ifThenElse));

        ifThenElse.setNodeType(new Type(Type.TypeName.VOID));
        return errors;

    }

    @Override
    public List<AnalysisError> visit(While aWhile) {

        List<AnalysisError> errors = new ArrayList<>();
        errors.addAll(aWhile.expr.accept(this));
        errors.addAll(aWhile.body.accept(this));

        if(onlyNameAnalysis)
            return errors;

        if( !errors.isEmpty() )
            // There are some name checking errors. Those errors should be fixed before doing type checking. Therefore, we
            // return errors here.
            return errors;

        errors.addAll(typeCheck(aWhile.expr.getNodeType(), Type.TypeName.BOOLEAN, aWhile));
        aWhile.setNodeType(new Type(Type.TypeName.VOID));

        return errors;

    }

    @Override
    public List<AnalysisError> visit(Print print) {

        List<AnalysisError> errors = new ArrayList<>();
        errors.addAll(print.expression.accept(this));

        if(onlyNameAnalysis)
            return errors;
        if( !errors.isEmpty() )
            // There are some name checking errors. Those errors should be fixed before doing type checking. Therefore, we
            // return errors here.
            return errors;

        Type.TypeName[] typeNames = {Type.TypeName.STRING, Type.TypeName.INT, Type.TypeName.BOOLEAN};
        errors.addAll(typeCheck(print.expression.getNodeType(), typeNames, print));

        print.setNodeType(new Type(Type.TypeName.VOID));
        return errors;

    }

    @Override
    public List<AnalysisError> visit(Assign assign) {

        List<AnalysisError> errors = new ArrayList<>();
        Symbol symbol = null;
        try {
            symbol = symbolTable.findCorrespondingSymbol(assign.identifier, true, false, false);
            assign.identifier.setSymbol(symbol);
        } catch (IdentifierNotDefinedError identifierNotDefinedError) {
            errors.add(identifierNotDefinedError);
        }

        errors.addAll(assign.expression.accept(this));

        if(onlyNameAnalysis)
            return errors;

        if( !errors.isEmpty() )
            // There are some name checking errors. Those errors should be fixed before doing type checking. Therefore, we
            // return errors here.
            return errors;

        if (!isSubTypeOf(assign.expression.getNodeType(), assign.identifier.getSymbol().getType()))
            errors.add(new ExpectedTypeNotFoundError(assign.identifier.getSymbol().getType(),
                    assign.expression.getNodeType(), assign));
        assign.setNodeType(new Type(Type.TypeName.VOID));
/*        Symbol symbol = symbolTable.getSymbol(assign.identifier.idName);

        if (symbol == null)
            errors.add(new IdentifierNotDefinedError(assign.identifier.idName, assign.identifier.getLine(),
                    assign.identifier.getColumn()));

        assign.identifier.setSymbol(symbol);

        errors.addAll(assign.expression.accept(this));
*/
        return errors;

    }

    @Override
    public List<AnalysisError> visit(ArrayAssign arrayAssign) {

        List<AnalysisError> errors = new ArrayList<>();
        Symbol symbol = null;

        try {
            symbol = symbolTable.findCorrespondingSymbol(arrayAssign.identifier, true, false, false);
            arrayAssign.identifier.setSymbol(symbol);
        } catch (IdentifierNotDefinedError identifierNotDefinedError) {
            errors.add(identifierNotDefinedError);
        }

        errors.addAll(arrayAssign.index.accept(this));
        errors.addAll(arrayAssign.value.accept(this));

        if(onlyNameAnalysis)
            return errors;

        if( !errors.isEmpty() )
            // There are some name checking errors. Those errors should be fixed before doing type checking. Therefore, we
            // return errors here.
            return errors;

        errors.addAll(typeCheck(arrayAssign.index.getNodeType(), Type.TypeName.INT, arrayAssign));
        errors.addAll(typeCheck(arrayAssign.value.getNodeType(), Type.TypeName.INT, arrayAssign));

        arrayAssign.setNodeType(new Type(Type.TypeName.VOID));
/*        Symbol symbol = symbolTable.getSymbol(arrayAssign.identifier.idName);

        if (symbol == null)
            errors.add(new IdentifierNotDefinedError(arrayAssign.identifier.idName, arrayAssign.identifier.getLine(),
                    arrayAssign.identifier.getColumn()));

        arrayAssign.identifier.setSymbol(symbol);

        errors.addAll(arrayAssign.index.accept(this));
        errors.addAll(arrayAssign.value.accept(this));
*/
        return errors;

    }

    @Override
    public List<AnalysisError> visit(Sidef sidef) {

        List<AnalysisError> errors = new ArrayList<>();
        errors.addAll(sidef.expression.accept(this));

        if(onlyNameAnalysis)
            return errors;

        if( !errors.isEmpty() )
            // There are some name checking errors. Those errors should be fixed before doing type checking. Therefore, we
            // return errors here.
            return errors;

        sidef.setNodeType(new Type(Type.TypeName.VOID));

        return errors;

    }

    @Override
    public List<AnalysisError> visit(NewQ newQ) {

        List<AnalysisError> errors = new ArrayList<>();

        switch (newQ.type) {

            case IDENTIFIER:
                Symbol symbol = null;
                try {
                    symbol = symbolTable.findCorrespondingSymbol(((Identifier) newQ.content.get(0)), true, false, false);
                    ((Identifier) newQ.content.get(0)).setSymbol(symbol);
                    if( !onlyNameAnalysis )
                        ((Identifier) newQ.content.get(0)).setNodeType(symbol.getType());
                    if (newQ.content.get(1) != null) { // Something like a.foo() which a is an instance of a class and foo is the method
                        errors.addAll(((Y) newQ.content.get(1)).accept(this));
                        if ( !onlyNameAnalysis && ((Y) newQ.content.get(1)).getNodeType() != null)
                            newQ.setNodeType(((Y) newQ.content.get(1)).getNodeType());
                    }


                    // If there are some name checking errors we do not go through type checking because there are some identifiers without Symbol!
                    if (errors.isEmpty() && !onlyNameAnalysis && newQ.getNodeType() == null)// If it is a simple identifier, then the type of newQ would be the type of this identifier
                        newQ.setNodeType(((Identifier) newQ.content.get(0)).getNodeType());
                } catch (IdentifierNotDefinedError identifierNotDefinedError) {
                    errors.add(identifierNotDefinedError);
                }

                break;
            case THIS:
                ClassSymbol classSymbol = (ClassSymbol) symbolTable.findCorrespondingClass(newQ);
                newQ.setSymbol(classSymbol);
                if (newQ.content.get(0) != null) {
                    errors.addAll(((Y) newQ.content.get(0)).accept(this));
                    // Something line x = this.foo();
                    // If there are some name checking errors we do not go through type checking because there are some identifiers without Symbol!
                    if (errors.isEmpty() && !onlyNameAnalysis && ((Y) newQ.content.get(0)).getNodeType() != null)
                        newQ.setNodeType(((Y) newQ.content.get(0)).getNodeType());
                }

                // If there are some name checking errors we do not go through type checking because there are some identifiers without Symbol!
                if (errors.isEmpty() && !onlyNameAnalysis && newQ.getNodeType() == null)// Something like x = this; so, the type of the newQ would be the type of this (the class type)
                    newQ.setNodeType(new Type(Type.TypeName.IDENTIFIER));
                break;
            case NOT_EXP:
                errors.addAll(((Expression) newQ.content.get(0)).accept(this));
                if (newQ.content.get(1) != null) { // Something like !x.foo()
                    errors.addAll(((Y) newQ.content.get(1)).accept(this));

                    // If there are some name checking errors we do not go through type checking because there are some identifiers without Symbol!
                    if (errors.isEmpty() && !onlyNameAnalysis && ((Y) newQ.content.get(1)).getNodeType() != null) {
                        newQ.setNodeType(((Y) newQ.content.get(1)).getNodeType());
                        errors.addAll(typeCheck(((Y) newQ.content.get(1)).getNodeType(), Type.TypeName.BOOLEAN, newQ));
                    }
                    // If there are some name checking errors we do not go through type checking because there are some identifiers without Symbol!
                } else if(errors.isEmpty() && !onlyNameAnalysis ){ // Something like !x
                    newQ.setNodeType(((Expression) newQ.content.get(0)).getNodeType());
                    errors.addAll(typeCheck(((Expression) newQ.content.get(0)).getNodeType(), Type.TypeName.BOOLEAN, newQ));
                }

                break;
            case LPR_EXP_RPR:
                errors.addAll(((Expression) newQ.content.get(0)).accept(this));
                if (newQ.content.get(1) != null) {// Something like (x).foo()
                    errors.addAll(((Y) newQ.content.get(1)).accept(this));
                    // If there are some name checking errors we do not go through type checking because there are some identifiers without Symbol!
                    if(errors.isEmpty() && !onlyNameAnalysis )
                        newQ.setNodeType(((Y) newQ.content.get(1)).getNodeType());
                }
                // If there are some name checking errors we do not go through type checking because there are some identifiers without Symbol!
                else if(errors.isEmpty() && !onlyNameAnalysis ){ // Like (x+2)
                    newQ.setNodeType(((Expression) newQ.content.get(0)).getNodeType());
                }
                break;
            case NEW_IDENTIFIER:
                try {
                    symbol = symbolTable.getClassSymbol((Identifier) newQ.content.get(0));// findCorrespondingSymbol(((Identifier) newQ.content.get(0)), false, false, true);
                    ((Identifier) newQ.content.get(0)).setSymbol(symbol);
                    ((Identifier) newQ.content.get(0)).setNodeType( symbol.getType() );

                    // Something like new B().foo()
                    if (newQ.content.size() > 1 && newQ.content.get(1) != null) {

                        for (int i = 1; i < newQ.content.size(); i++) {
                            if (newQ.content.get(i) != null)
                                errors.addAll(((Y) newQ.content.get(i)).accept(this));
                        }
                        // If there are some name checking errors we do not go through type checking because there are some identifiers without Symbol!
                        if(errors.isEmpty() && !onlyNameAnalysis)
                            newQ.setNodeType(((Y) newQ.content.get(1)).getNodeType());
                    }
                    // If there are some name checking errors we do not go through type checking because there are some identifiers without Symbol!
                    else if(errors.isEmpty() && !onlyNameAnalysis )//Like B b = new B();
                        newQ.setNodeType(((Identifier) newQ.content.get(0)).getNodeType());
                } catch (ClassDefinitionNotFoundError classDefinitionNotFoundError) {
                    errors.add(classDefinitionNotFoundError);
                }


                break;
            case NEW_INT_ARR:
                errors.addAll(((Expression) newQ.content.get(0)).accept(this));
                // If there are some name checking errors we do not go through type checking because there are some identifiers without Symbol!
                if(errors.isEmpty() && !onlyNameAnalysis )
                    typeCheck(((Expression) newQ.content.get(0)).getNodeType(), Type.TypeName.INT, newQ);
                if (newQ.content.get(1) != null) {// I have no Idea what it would be!
                    errors.addAll(((Y) newQ.content.get(1)).accept(this));
                    // If there are some name checking errors we do not go through type checking because there are some identifiers without Symbol!
                    if(errors.isEmpty() && !onlyNameAnalysis)
                        newQ.setNodeType(((Y) newQ.content.get(1)).getNodeType());
                }
                else if(!onlyNameAnalysis){ // Like new int[x+1]
                    newQ.setNodeType(new Type(Type.TypeName.INT_ARRAY));
                }
                break;

            case TRUE:
            case FALSE:
            case BOOL_LIT:
                if(!onlyNameAnalysis)
                    newQ.setNodeType(new Type(Type.TypeName.BOOLEAN));
                break;
            case STRING_LIT:
                if(!onlyNameAnalysis)
                    newQ.setNodeType(new Type(Type.TypeName.STRING));
                break;
            case INT_LIT:
                if(!onlyNameAnalysis)
                    newQ.setNodeType(new Type(Type.TypeName.INT));
                break;

        }

        return errors;

    }

    @Override
    public List<AnalysisError> visit(Y y) {

        List<AnalysisError> errors = new ArrayList<>();

        switch (y.type) {

            case DOT_LENGTH_Y: // Like s.length. the type would be int
                if (y.content.size() > 0)
                    errors.addAll(((Y) (y.content.get(0))).accept(this));
                if(!onlyNameAnalysis)
                    y.setNodeType(new Type(Type.TypeName.INT));

                break;
            case LBR_EXP_RBR_Y:
                if (y.content.size() > 0) {

                    if (y.content.get(0) instanceof Expression)
                        errors.addAll(((Expression) (y.content.get(0))).accept(this));
                    else if (y.content.get(0) instanceof Y)
                        errors.addAll(((Y) (y.content.get(0))).accept(this));

                }
                // If there are some name checking errors we do not go through type checking because there are some identifiers without Symbol!
                if(errors.isEmpty() && !onlyNameAnalysis)
                    typeCheck(((Expression) y.content.get(0)).getNodeType(), Type.TypeName.INT, y);

                // After the following line, parent (newQ) will know his type
                switch (((Tree) ((NewQ) y.getParent()).content.get(0)).getSymbol().getType().typeName) {
                    case INT_ARRAY:
                        break;
                    default:
                        errors.add(new ExpectedTypeNotFoundError(Type.TypeName.INT_ARRAY,
                                ((Tree) ((NewQ) y.getParent()).content.get(0)).getSymbol().getType(),
                                ((Tree) ((NewQ) y.getParent()).content.get(0))));

                }
                // Only arrays of int could be initialized and used in EMJ
                if(!onlyNameAnalysis)
                    y.setNodeType(new Type(Type.TypeName.INT));

                break;
            case DOT_IDENTIFIER_EXPRESSIONLIST_Y:
                Symbol idSymbol = null;
                List<Expression> expressionList = (List<Expression>) y.content.get(1);

                if (expressionList != null)
                    for (Expression expression : expressionList)
                        errors.addAll(expression.accept(this));

                Identifier methodName = (Identifier) y.content.get(0);
                NewQ grandParent = (NewQ) methodName.getParent().getParent();
                ClassSymbol classSymbol = null;


                if (grandParent.getType().equals(NewQ.TYPE.THIS)) {
                    classSymbol = (ClassSymbol) grandParent.getSymbol();
                } else if (grandParent.getType().equals(NewQ.TYPE.NEW_IDENTIFIER)) {
                    classSymbol = (ClassSymbol) ((Identifier) (((NewQ) methodName.getParent().getParent()).content.get(0))).getSymbol();
                } else if (grandParent.getType().equals(NewQ.TYPE.IDENTIFIER)) {
                    classSymbol = ((VarSymbol) ((Identifier) (((NewQ) methodName.getParent().getParent()).content.get(0))).getSymbol()).getTypeClass();
                }

                ClassSymbol tmp = classSymbol;

                while (tmp != null) {

                    for (MethodSymbol methodSymbol : tmp.methodSymbols) {
                        // Check whether the found method is the method (arguments number and type)
                        if (methodSymbol.getSymbolName().equals(methodName.idName)
                                && methodSymbol.getArgumentNumber() == (expressionList == null ? 0 : expressionList.size())) {
                            if(errors.isEmpty() && !onlyNameAnalysis) {
                                boolean matches = true;
                                for (int i = 0; i < (expressionList != null ? expressionList.size() : 0); i++) {
                                    Map<Type, Identifier> map = methodSymbol.getMethodDeclaration().typeIdentifiers.get(i);
                                    List<TypeAnalysisError> errors1 = simpleTypeCheck(expressionList.get(i).getNodeType(), map.keySet().iterator().next(), y);
                                    if (errors1.size() > 0) {
                                        errors.addAll(errors1);
                                        matches = false;
                                    }
                                }
                                if(matches) {
                                    idSymbol = methodSymbol;
                                    break;
                                }

                            }
                            else {
                                idSymbol = methodSymbol;
                                break;
                            }
                        }
                        if( idSymbol != null )
                            break;

                    }

                    tmp = tmp.getExtendedClassSymbol();

                }

                if (idSymbol == null) {
                    List<Type> methodArgType = new ArrayList<>();
                    for(Expression e : expressionList)
                        methodArgType.add( e.getNodeType() );
                    errors.add(new MethodDefinitionNotFoundError(methodName.idName, methodArgType,
                            methodName.getLine(), methodName.getColumn(), classSymbol));
                }

                ((Identifier) y.content.get(0)).setSymbol(idSymbol);


                // If for example the method is not defined in the class, the method has not a symbol assigned to it at this point.
                if (errors.isEmpty() && (!onlyNameAnalysis) && ((MethodSymbol) ((Tree) (y.content.get(0))).getSymbol()) != null) {
                    // It is like b.foo( x, 3 ). in this y, we have .foo( x, 3), so in order to set the NodeType of y, we
                    // use it's symbol and the type assigned to that symbol.
                    // TODO: bellow line should be moved in where we initiating method type.
                    Type t = ((MethodSymbol) ((Tree) (y.content.get(0))).getSymbol()).methodDeclaration.methodType;
                    if( t.identifier != null && idSymbol != null)
                        t.identifier.setSymbol( ((MethodSymbol) idSymbol).getTypeClass() );

                    y.setNodeType(t);
                }
                break;

        }

        return errors;

    }

    protected boolean isSubTypeOf(Type type1, Type type2) {
        if (type1.typeName == null || (type1.typeName.equals(Type.TypeName.IDENTIFIER) && type1.identifier == null) ||
                type2.typeName == null || (type2.typeName.equals(Type.TypeName.IDENTIFIER) && type2.identifier == null) ||
                (type1.typeName.equals(Type.TypeName.IDENTIFIER) && !type2.typeName.equals(Type.TypeName.IDENTIFIER)) ||
                (!type1.typeName.equals(Type.TypeName.IDENTIFIER) && type2.typeName.equals(Type.TypeName.IDENTIFIER))
                )
            return false;
        if (type1.typeName.equals(Type.TypeName.IDENTIFIER)) {
            if (type1.identifier.getSymbol() == null || type2.identifier.getSymbol() == null)
                return false;
            boolean isSubType = false;
            isSubType = type1.identifier.getSymbol().equals(type2.identifier.getSymbol());
            ClassSymbol tmp = (ClassSymbol)type1.identifier.getSymbol();
            while( !isSubType && tmp.getExtendedClassSymbol() != null ) {
                tmp = tmp.extendedClassSymbol;
                isSubType = tmp.equals(type2.identifier.getSymbol());
            }
            return isSubType;
        }
        return type1.typeName.equals(type2.typeName);
    }

    protected List<TypeAnalysisError> simpleTypeCheck(Type type1, Type type2, Tree astNode) {
        List<TypeAnalysisError> errors = new ArrayList<>();
/*        if( type1 == null && type2 != null || type1 != null && type2 == null) {
            errors.add(new ExpectedTypeNotFoundError(type1, type2));
            return errors;
        }
        if( type1 == null && type2 == null )
            return errors;*/
        if (!type1.typeName.equals(type2.typeName)) {
            errors.add(new ExpectedTypeNotFoundError(type2, type1, astNode));
            return errors;
        }
        if (type1.typeName.equals(Type.TypeName.IDENTIFIER))
            if (! isSubTypeOf( type1, type2 ) ) {
                errors.add(new ExpectedTypeNotFoundError(type1, type2, astNode));
                return errors;
            }

        return errors;
    }

    protected List<TypeAnalysisError> typeCheck(Type type, Type.TypeName typeName, Tree astNode) {
        Type.TypeName[] typeNames = {typeName};
        return typeCheck(type, typeNames, astNode);
    }

    protected List<TypeAnalysisError> typeCheck(Type type, Type.TypeName[] typeNames, Tree astNode) {
        List<TypeAnalysisError> errors = new ArrayList<>();
/*        if( type == null ) {
            errors.add(new ExpectedTypeNotFoundError(type1, type2));
            return errors;
        }
        if( type1 == null && type2 == null )
            return errors;*/
        boolean typeChecked = false;
        for (Type.TypeName typeName : typeNames)
            if (type.typeName.equals(typeName))
                typeChecked = true;
        if (!typeChecked) {
            errors.add(new ExpectedTypeNotFoundError(typeNames, type, astNode));
        }

        return errors;
    }

    @Override
    public List<AnalysisError> visit(NameAnalyzer nameAnalyzer) {
        return nameAnalyzer.errorList;
    }

    @Override
    public <R> R accept(Visitor<R> v) {
        return v.visit(this);
    }

    public SymbolTable getSymbolTable() {
        return symbolTable;
    }


}

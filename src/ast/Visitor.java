package ast;

import analyser.NameAnalyzer;
import codegen.CodeGenerator;

public interface Visitor<R> {

    R visit(Program n);

    R visit(Expression n);

    R visit(MethodDeclaration methodDeclaration);

    R visit(A a);

    R visit(B b);

    R visit(C c);

    R visit(ClassDeclaration classDeclaration);

    R visit(D d);

    R visit(F f);

    R visit(G g);

    R visit(H h);

    R visit(J j);

    R visit(MainClass mainClass);

    R visit(StringLiteral stringLiteral);

    R visit(T t);

    R visit(Type type);

    R visit(VarDeclaration varDeclaration);

    R visit(Identifier identifier);

    R visit(IntLiteral intLiteral);

    R visit(Block block);

    R visit(Skip skip);

    R visit(IfThenElse ifThenElse);

    R visit(While aWhile);

    R visit(Print print);

    R visit(Assign assign);

    R visit(ArrayAssign arrayAssign);

    R visit(Sidef sidef);

    R visit(NewQ newQ);

    R visit(Y y);

    R visit(NameAnalyzer nameAnalyzer);

}

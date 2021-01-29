package ast;

import lexer.CommonConstants;

import java.util.List;

public class Program extends Tree {

    public MainClass mainClass;
    public List<ClassDeclaration> classList;

    public Program(MainClass mainClass, List<ClassDeclaration> classList) {

        super(CommonConstants.AstNodeType.PROGRAM);

        if (mainClass != null)
            mainClass.setParent(this);

        if (classList != null)
            for (ClassDeclaration classDeclaration : classList)
                classDeclaration.setParent(this);

        this.mainClass = mainClass;
        this.classList = classList;

    }

    public MainClass getMainClass() {
        return mainClass;
    }

    public void setMainClass(MainClass mainClass) {
        this.mainClass = mainClass;
    }

    public List<ClassDeclaration> getClassList() {
        return classList;
    }

    public void setClassList(List<ClassDeclaration> classList) {
        this.classList = classList;
    }

    public int getClassDeclarationListSize() {
        return classList.size();
    }

    public ClassDeclaration getClassDeclaration(int index) {
        return index < classList.size() ? classList.get(index) : null;
    }

    @Override
    public int getLine() {
        return getMainClass().getLine();
    }

    @Override
    public <R> R accept(Visitor<R> v) {
        return v.visit(this);
    }

}

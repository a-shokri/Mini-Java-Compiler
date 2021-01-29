package misc;

import ast.Type;

public abstract class TypeBinding {

    protected Type type;

    public TypeBinding(Type type) {
        this.type = type;
    }

    public Type type() {
        return type;
    }

}

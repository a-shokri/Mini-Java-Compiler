package analyser.errors;

public abstract class AnalysisError extends Throwable {

    private int line;
    private int column;

    public int getLine() {
        return line;
    }

    public void setLine(int line) {
        this.line = line;
    }

    public int getColumn() {
        return column;
    }

    public void setColumn(int column) {
        this.column = column;
    }

    public String getError() {
        return getLine() + (getColumn()>0? ":" + getColumn():",") + " error: " + getSpecificErrorMessage();
    }

    protected abstract String getSpecificErrorMessage();

}

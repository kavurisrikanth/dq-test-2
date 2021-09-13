package gqltosql.schema;

public class DMessage {
    private String name;
    private DParam[] params;
    private int index;
    
    public DMessage(String name, int index, DParam[] params) {
        this.name = name;
        this.index = index;
        this.params = params;
    }

    public DMessage(String name, int index, int numParams) {
        this.name = name;
        this.index = index;
        this.params = new DParam[numParams];
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public DParam[] getParams() {
        return params;
    }

    public void setParams(DParam[] params) {
        this.params = params;
    }

    public void addParam(int index, DParam param) {
        this.params[index] = param;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }
}

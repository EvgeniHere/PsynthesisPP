package psynthesispp.preset;


public class HexagonTupleFormatException extends IllegalArgumentException {
    public HexagonTupleFormatException(String msg) {
        super(msg);
    }

    public HexagonTupleFormatException(String msg, Throwable e) {
        super(msg, e);
    }
    
    //------------------------------------------------------
    private static final long serialVersionUID = 1L;
}

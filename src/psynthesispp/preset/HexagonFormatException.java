package psynthesispp.preset;


public class HexagonFormatException extends IllegalArgumentException {
    public HexagonFormatException(String msg) {
        super(msg);
    }

    public HexagonFormatException(String msg, Throwable e) {
        super(msg, e);
    }
    
    //------------------------------------------------------
    private static final long serialVersionUID = 1L;
}

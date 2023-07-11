package psynthesispp.preset;


/**
 * Eine {@code ArgumentParserException} wird geworfen, 
 * wenn beim Einlesen der Programmargumente ein Fehler auftritt.
 *
 * @author Dominick Leppich
 */
public class ArgumentParserException extends Exception {
    public ArgumentParserException(final String msg) {
        super(msg);
    }

    public ArgumentParserException(final String msg, final Throwable cause) {
        super(msg, cause);
    }
    
    //------------------------------------------------------
    private static final long serialVersionUID = 1L;
}

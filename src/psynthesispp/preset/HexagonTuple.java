package psynthesispp.preset;


import java.io.*;
import java.util.*;

public class HexagonTuple implements Serializable {
    
    /** max number of hexagons, base for hash code */
    public static final int BASE = Hexagon.NEXT_HASH;
    
    /** max hash code plus 1 */
    public static final int NEXT_HASH = BASE * BASE;

    //------------------------------------------------------
    public HexagonTuple(final Hexagon from, final Hexagon to) {
        hex = new Hexagon[] {from, to};
    }
    
    public HexagonTuple(final HexagonTuple tuple) {
	this(tuple.getFrom(), tuple.getTo());
    }

    //------------------------------------------------------
    public Hexagon getFrom() {
        return hex[0];
    }
    
    public Hexagon getTo() {
        return hex[1];
    }
    
    public Hexagon[] toArray() {
        return new Hexagon[] {getFrom(), getTo()};
    }

    //------------------------------------------------------
    public int hashCode() {
        return getFrom().hashCode() * HexagonTuple.BASE + getTo().hashCode();
    }
    
    public boolean equals(final Object o) {
        if (o == null)
            return false;
        if (!(o instanceof HexagonTuple))
            return false;
        HexagonTuple t = (HexagonTuple) o;
	return getFrom().equals(t.getFrom()) && getTo().equals(t.getTo());
    }

    public String toString() {
        return "[" + getFrom().toString() + "," + getTo().toString() + "]";
    }

    //------------------------------------------------------
    protected int compareByHash(final HexagonTuple s) {
	if(s == null)
	    return -1;
	    
	if (equals(s) || hashCode() == s.hashCode())
	    return 0;
    
	if (hashCode() < s.hashCode())
	    return -1;
    
        return 1;
    }

    //------------------------------------------------------
    private Hexagon[] hex;

    // static ==============================================
   
    //------------------------------------------------------
    protected static Hexagon[] parse(final String string) {
	if (string == null)
	    throw new HexagonFormatException("cannot parse empty string!");
	
	String msg = "cannot parse string \""
	             + string
	             + "\"! correct format is: "
	             + "[(C0,R0),(C1,R1)]";

	String str = string.trim();

        if (str.equals(""))
            throw new HexagonTupleFormatException("cannot parse empty string");

        if (!str.startsWith("[") || !str.endsWith("]"))
            throw new HexagonTupleFormatException(msg);

        // the splitting needs to be merged afterwards
        String[] parts = str.substring(1, str.length()-1).split(",");

        if (parts.length != 4)
            throw new HexagonTupleFormatException(msg);

        try {
            // Merge splitted substrings accordingly
            return new Hexagon[] {Hexagon.parse(parts[0] + ',' + parts[1]),
				  Hexagon.parse(parts[2] + ',' + parts[3])};
        } catch (HexagonFormatException e) {
            throw new HexagonTupleFormatException("wrong hexagon format! ", e);
        }
    }
    
    //------------------------------------------------------
    private static final long serialVersionUID = 1L;
}

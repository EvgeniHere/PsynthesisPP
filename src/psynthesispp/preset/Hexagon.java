package psynthesispp.preset;


import java.io.Serializable;

public class Hexagon implements Serializable, Comparable<Hexagon> {
    
    /** max number of columns/rows, base for hash code */
    public static final int BASE = 11;
    
    /** max hash code plus 1 */
    public static final int NEXT_HASH = BASE * BASE;

    // -----------------------------------------------------
    private int column;
    private int row;
    
    // -----------------------------------------------------
    public Hexagon(final int column, final int row) {
		if (column < 0 || column >= BASE || row < 0 || row >= BASE)
		    throw new HexagonFormatException("format: 0 <= column/row <= " + (BASE-1));
	
        this.column = column;
        this.row = row;
    }

    //------------------------------------------------------
    public int getColumn() {
        return column;
    }

    public int getRow() {
        return row;
    }

    public int[] toArray() {
    	return new int[] {column, row};
    }

    // -----------------------------------------------------
    public int hashCode() {
        return column * BASE + row;
    }

    public boolean equals(final Object o) {
        if (o == null)
            return false;
        if (!(o instanceof Hexagon))
            return false;
        Hexagon h = (Hexagon) o;
        return column == h.getColumn() && row == h.getRow();
    }

    public String toString() {
        return "(" + column + "," + row + ")";
    }

    //------------------------------------------------------
    public int compareTo(final Hexagon h) {
		// correct if hash code is unique only
		if(h == null)
		    return -1;
		    
		if (equals(h) || hashCode() == h.hashCode())
		    return 0;
	    
		if (hashCode() < h.hashCode())
		    return -1;
    
        return 1;
    }

    // static ==============================================
    
    // -----------------------------------------------------
    public static Hexagon parse(final String string) {
		if (string == null)
		    throw new HexagonFormatException("cannot parse empty string!");
		
		String msg = "cannot parse string \""
		             + string
		             + "\"! correct format is: (COLUMN,ROW)";
	
		String str = string.trim();
	
        if (str.equals(""))
            throw new HexagonFormatException("cannot parse empty string!");

        if (!str.startsWith("(") || !str.endsWith(")"))
            throw new HexagonFormatException(msg);

        String[] parts = str.substring(1, str.length()-1).split(",");

        if (parts.length != 2)
            throw new HexagonFormatException(msg);
	
        try {
            return new Hexagon(Integer.parseInt(parts[0].trim()),
			    Integer.parseInt(parts[1].trim()));
        } catch (NumberFormatException e) {
            throw new HexagonFormatException("wrong number format! ", e);
        }
    }

    //------------------------------------------------------
    private static final long serialVersionUID = 1L;
}

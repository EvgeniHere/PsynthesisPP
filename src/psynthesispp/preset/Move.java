package psynthesispp.preset;


import java.io.*;
import java.util.*;

public class Move implements Serializable {

    //------------------------------------------------------
    public Move(final MoveType type) {
        if (type != MoveType.Empty &&
	    type != MoveType.Surrender &&
	    type != MoveType.End)
            throw new IllegalArgumentException("constructors for empty/surrender/end moves only");

        this.type = type;
    }
    
    public Move(final Hexagon hex) {
        type = MoveType.Prepare;
        prepare = hex;
    }
    
    public Move(final Integer list[]) {
        type = MoveType.Activate;
        activate = new ArrayList<Integer>(Arrays.asList(list)); 
    }
    
    public Move(final HexagonTuple list[]) {
        type = MoveType.Plant;
        plant = new ArrayList<HexagonTuple>(Arrays.asList(list));
    }
    
    public Move(final Hexagon list[]) {
        type = MoveType.Grow;
        grow =  new ArrayList<Hexagon>(Arrays.asList(list));
    }


    //------------------------------------------------------
    public MoveType getType() {
        return type;
    }

    public Hexagon getPrepare() {
        if (type != MoveType.Prepare)
            throw new IllegalStateException("cannot be called on type " + type);
        return prepare;
    }
    
    public ArrayList<Integer> getActivate() {
        if (type != MoveType.Activate)
            throw new IllegalStateException("cannot be called on type " + type);
        return activate;
    }
  
    public ArrayList<HexagonTuple> getPlant() {
        if (type != MoveType.Plant)
            throw new IllegalStateException("cannot be called on type " + type);
        return plant;
    }

    public ArrayList<Hexagon> getGrow() {
        if (type != MoveType.Grow)
            throw new IllegalStateException("cannot be called on type " + type);
        return grow;
    }

    
    public String toString() {

	String str = "";
	
	switch (type) {
	case End:
	    return "end";
	case Surrender:
	    return "Surrender";
	case Empty:
	    return "empty";
	case Prepare:
	    return "prepare > " + prepare.toString();
	case Activate:
	    for (Integer i: activate)
		str=(str=="") ? i.toString() : str+"+"+i.toString();
	    str="activate> "+str;
	    break;
	case Plant:
	    for (HexagonTuple h: plant)
		str=(str=="") ? h.toString() : str+"+"+h.toString();
	    str="plant   > "+str;
	    break;
	case Grow:
	    for (Hexagon h: grow)
		str=(str=="") ? h.toString() : str+"+"+h.toString();
	    str="grow    > "+str;
	    break;
	default:
	    str="error";
	}
	return str;
    }
   
    //------------------------------------------------------
    private MoveType type;
    private Hexagon prepare;
    private ArrayList<Integer> activate;
    private ArrayList<HexagonTuple> plant;
    private ArrayList<Hexagon> grow;

    // static ==============================================

    //------------------------------------------------------
    private static Integer[] parseActivate(final String string) {
	if (string == null)
	    throw new MoveFormatException("cannot parse null string!");
	
	String str = string.trim();
        if (str.equals(""))
            return new Integer[0];

	ArrayList<Integer> list = new ArrayList<Integer>();
        String[] parts = str.split("\\+");
       
	    for(String p:  parts)
		list.add(Integer.parseInt(p.trim())); 
	
	return list.toArray(new Integer[0]);
    }

    //------------------------------------------------------
    private static HexagonTuple[] parsePlant(final String string) {
	if (string == null)
	    throw new HexagonTupleFormatException("cannot parse null string!");
	
	ArrayList<HexagonTuple> list = new ArrayList<HexagonTuple>();
	String str = string.trim();
        if (str.equals(""))
            return new HexagonTuple[0];
	
	String[] parts = str.split("\\+");
	for(String p:  parts) {
	    Hexagon[] h = HexagonTuple.parse(p.trim());
	    list.add(new HexagonTuple(h[0], h[1]));
	}
	
	return list.toArray(new HexagonTuple[0]);
    }

    //------------------------------------------------------
    private static Hexagon[] parseGrow(final String string) {
	if (string == null)
	    throw new HexagonFormatException("cannot parse null string!");
	
	ArrayList<Hexagon> list = new ArrayList<Hexagon>();
	String str = string.trim();
        if (str.equals(""))
            return new Hexagon[0];
	
	String[] parts = str.split("\\+");
	for(String p:  parts)
	    list.add(Hexagon.parse(p.trim())); 
	
	return list.toArray(new Hexagon[0]);
    }
    
    //------------------------------------------------------
    private static Move parse(final String string) {
	if (string == null)
	    throw new MoveFormatException("cannot parse null string!");

	String str = string.trim().toLowerCase();
	
        if (str.equals(""))
	    return new Move(MoveType.Empty);

	if (str.startsWith("end"))
	    return new Move(MoveType.End);
 
	if (str.startsWith("surrender"))
	    return new Move(MoveType.Surrender);

	return null;
    }
	
    
    //------------------------------------------------------
    public static Move parse(final String string, final MoveType type) {
	if (string == null)
	    throw new MoveFormatException("cannot parse null string!");
	
	switch (type) {
	case End:
	    throw new MoveFormatException("cannot request move type \"End\"!");
	case Surrender:
	    throw new MoveFormatException("cannot request move type \"Surrender\"!");
	}
	
	String msg = "parsing of \""
	             + string
	             + "\" failed! correct formats are:\n"
	             + "prepare > (COLUMN,ROW)\n"
	             + "activate> S+T1+T2+....+Tk\n" 
	             + "plant   > [(C0,R0),(C1,R1)]+...\n"
	             + "grow    > (C0,R0)+...";

	String str = string.trim().toLowerCase();
	
	if (str.startsWith("end"))
	    return new Move(MoveType.End);
 
	if (str.startsWith("surrender"))
	    return new Move(MoveType.Surrender);

	if (type == MoveType.Prepare)
	    return new Move(Hexagon.parse(str));
	
        if (str.equals(""))
	    return new Move(MoveType.Empty);

	try {
	    switch (type) {
	    case Activate:
		return new Move(parseActivate(str));
	    case Plant:
		return new Move(parsePlant(str));
	    case Grow:
		return new Move(parseGrow(str));
	    default:
		new MoveFormatException("unknow move type!"); 
	    }
	} catch (NumberFormatException e) {
	    throw new MoveFormatException("wrong number format!\n"+msg);
	} catch (HexagonTupleFormatException e) {
	    throw new MoveFormatException("wrong hexagon tuple format!\n"+msg);
	} catch (HexagonFormatException e) {
	    throw new MoveFormatException("wrong hexagon format!\n"+msg);
	}

	// unreachable
	return null;
    }
    
    //------------------------------------------------------
    private static final long serialVersionUID = 1L;
}
    

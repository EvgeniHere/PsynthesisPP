package psynthesispp.preset;


import java.util.HashMap;

/**
 * Ein simpler Parser fuer Kommandozeilen Parameter.
 * <h1>Verwendung</h1>
 * <p>
 * Erzeuge innerhalb deiner ausfuehrbaren Klasse eine Instanz dieser Klasse und
 * uebergib im Konstruktor die Kommandozeilenargumente.
 * Verwende diesen ArgumentParser um auf Kommandozeilen Parameter zu reagieren.
 * </p>
 * <p>
 * Kommandozeilen Parameter sind entweder Schalter oder Einstellungen.
 * </p>
 * <h2>Schalter</h2>
 * <p>
 * Ein Schalter ist entweder ein- oder ausgeschaltet. Dementsprechend kann sein
 * Zustand in einem {@code boolean} abgelegt werden. Schalter sind zu Beginn
 * ausgeschaltet. Ein Schalter wird ueber den Parameter {@code --SCHALTERNAME}
 * aktiviert. Ein Schalter kann ueber Kommandozeilen Parameter nicht deaktiviert
 * werden, da er zu Beginn ohnehin deaktiviert ist.
 * </p>
 * <h2>Einstellungen</h2>
 * <p>
 * Eine Einstellung hat einen Namen und einen Wert. Ein gutes Beispiel ist hier
 * die Spielfeldgroesse. Der Name dieser Einstellung ist {@code size} und der Wert
 * kann eine Zahl zwischen {@code 6} und {@code 26} sein. Der Typ einer
 * Einstellung richtet sich nach der Einstellung. Die Einstellung {@code size}
 * zum Beispiel ist ein {@code int}. Einstellungen werden auf der Kommandozeile
 * mit {@code -NAME WERT} gesetzt.
 * </p>
 * <p>
 * Wird ein Schalter oder eine Einstellung abgefragt die nicht eingelesen wurde,
 * wird eine {@link ArgumentParserException} geworfen, auf die sinnvoll reagiert
 * werden muss.
 * </p>
 * <p>
 * Alle Schalter und Einstellungen in dieser Klasse duerfen nicht geaendert werden.
 * Es ist jedoch erlaubt weitere Schalter oder Einstellungen hinzuzufuegen, dies ist
 * im Quellcode kenntlich gemacht.
 * </p>
 *
 * @author Dominick Leppich
 */
public class ArgumentParser {
    /** Map zur Speicherung der Parameter */
    private HashMap<String, Object> params;

    // ------------------------------------------------------------

    /**
     * Erzeuge einen neuen ArgumentParser aus einem String-Array mit Parametern.
     * Hier sollte einfach das {@code args} Argument der {@code main}-Methode
     * weitergerreicht werden.
     *
     * @param args
     *         Argumente
     *
     * @throws ArgumentParserException
     *         wenn das Parsen der Argumente fehlschlaegt
     */
    public ArgumentParser(final String[] args) throws ArgumentParserException {
        params = new HashMap<>();
        parseArgs(args);
    }
    // ------------------------------------------------------------

    /**
     * Parse die Argumente.
     *
     * @param args
     *         Argumente
     *
     * @throws ArgumentParserException
     *         wenn das Parsen der Argumente fehlschlaegt
     */
    private void parseArgs(final String[] args) throws ArgumentParserException {
        // Index to parse
        int index = 0;

        try {
            while (index < args.length) {
                // Check if argument is a flag or setting
                if (args[index].startsWith("--")) {
                    addFlag(args[index].substring(2));
                    index += 1;
                } else if (args[index].startsWith("-")) {
                    addSetting(args[index].substring(1), args[index + 1]);
                    index += 2;
                } else
                    throw new ArgumentParserException("Error parsing: " + args[index]);
            }
        } catch (IndexOutOfBoundsException e) {
            throw new ArgumentParserException("Missing parameter", e);
        }
    }

    /**
     * Fuege einen Schalter hinzu.
     *
     * @param flag
     *         Schalte
     *
     * @throws ArgumentParserException
     *         wenn der Schalter nicht existiert
     */
    private void addFlag(final String flag) throws ArgumentParserException {
        // Check if a param with this name already exists
        if (params.containsKey(flag))
            throw new ArgumentParserException("Param already exists: " + flag);

        params.put(flag, Boolean.TRUE);
    }

    /**
     * Fuege eine Einstellung hinzu.
     *
     * @param key
     *         Name
     * @param value
     *         Wert
     *
     * @throws ArgumentParserException
     *         wenn die Einstellung nicht existiert oder der Wert ein ungueltiges
     *         Format hat
     */
    private void addSetting(final String key, final String value) throws ArgumentParserException {
        // Check if a param with this name already exists
        if (params.containsKey(key))
            throw new ArgumentParserException("Param already exists: " + key);

        if (value.startsWith("-"))
            throw new ArgumentParserException("Setting value wrong format: " + value);

        params.put(key, value);
    }

    // ------------------------------------------------------------

    /**
     * Pruefe ob ein Parameter gesetzt ist.
     *
     * @param parameter
     *         Zu pruefender Parameter
     *
     * @return wahr, wenn der Parameter gesetzt wurde
     */
    public boolean isSet(final String parameter) {
        return params.containsKey(parameter);
    }

    /**
     * Gib den Wert eines Schalters zurueck.
     *
     * @param flag
     *         Name des Schalters
     *
     * @return Wert
     *
     * @throws ArgumentParserException
     *         wenn der Schalter den falschen Typ hat (falls eine Einstellung
     *         versucht wird als Schalter auszulesen)
     */
    protected boolean getFlag(final String flag) throws ArgumentParserException {
        if (!params.containsKey(flag))
            return false;

        Object o = params.get(flag);
        if (!(o instanceof Boolean))
            throw new ArgumentParserException("This is not a flag");

        return (Boolean) params.get(flag);
    }

    /**
     * Gib den Wert einer Einstellung als {@link Object} zurueck.
     *
     * @param key
     *         Name der Einstellung
     *
     * @return Wert als {@link Object}.
     *
     * @throws ArgumentParserException
     *         wenn die Einstellung nicht existiert
     */
    protected Object getSetting(final String key) throws ArgumentParserException {
        if (!params.containsKey(key))
            throw new ArgumentParserException("Setting " + key + " not " + "defined");

        return params.get(key);
    }

    // ------------------------------------------------------------

    /**
     * Interpretiere einen Spielertypen
     *
     * @param type
     *         Eingelesener Typ
     *
     * @return Spielertyp als {@link PlayerType}
     *
     * @throws ArgumentParserException
     *         wenn der eingelese Typ nicht passt
     */
    protected PlayerType parsePlayerType(final String type) throws ArgumentParserException {
        switch (type) {
            case "human":
                return PlayerType.Human;
            case "random":
                return PlayerType.RandomAI;
            case "simple":
                return PlayerType.SimpleAI;
            case "extended":
                return PlayerType.ExtendedAI;
            case "upgraded":
                return PlayerType.UpgradedAI;
            case "enhanced":
                return PlayerType.EnhancedAI;
            case "advanced":
                return PlayerType.AdvancedAI;
            case "remote":
                return PlayerType.Remote;

            default:
                throw new ArgumentParserException("Unknown player type: " + type);
        }
    }

    // ------------------------------------------------------------

    public int getSize() throws ArgumentParserException {
    	if (!isSet("size"))
    		return 3;

        return Integer.parseInt((String) getSetting("size"));
    }

    public PlayerType getRed() throws ArgumentParserException {
    	if (!isSet("red"))
    		return PlayerType.Human;

        return parsePlayerType((String) getSetting("red"));
    }

    public PlayerType getBlue() throws ArgumentParserException {
    	if (!isSet("blue"))
    		return PlayerType.Human;

        return parsePlayerType((String) getSetting("blue"));
    }

    public PlayerType getSimRed() throws ArgumentParserException {
    	if (!isSet("simRed"))
    		return PlayerType.SimpleAI;

        return parsePlayerType((String) getSetting("simRed"));
    }

    public PlayerType getSimBlue() throws ArgumentParserException {
    	if (!isSet("simBlue"))
    		return PlayerType.SimpleAI;

        return parsePlayerType((String) getSetting("simBlue"));
    }

    public int getDelay() throws ArgumentParserException {
    	if (!isSet("delay"))
    		return 0;

        return Integer.parseInt((String) getSetting("delay"));
    }

    public boolean isDebug() throws ArgumentParserException {
        return getFlag("debug");
    }

    public int getGamesCounter() throws ArgumentParserException {
    	if (!isSet("numGames"))
    		return 1;

    	return Integer.parseInt((String) getSetting("numGames"));
    }

    public int getDifficultyRed() throws ArgumentParserException {
    	if (!isSet("diffRed"))
    		return 1;

    	switch((String) getSetting("diffRed")) {
    	case "easy":
    		return 0;
    	case "medium":
    		return 1;
    	case "hard":
    		return 2;
    	case "waitingSimulator":
    		return 20;
    	case "impossible":
    		return 9001;
    	}

    	return 0;
    }

    public int getDifficultyBlue() throws ArgumentParserException {
    	if (!isSet("diffBlue"))
    		return 1;

    	switch((String) getSetting("diffBlue")) {
    	case "easy":
    		return 0;
    	case "medium":
    		return 1;
    	case "hard":
    		return 2;
    	case "waitingSimulator":
    		return 20;
    	case "impossible":
    		return 9001;
    	}

    	return 0;
    }

    // ********************************************************************
    //  Hier koennen weitere Schalter und Einstellungen ergaenzt werden...
    // ********************************************************************


}

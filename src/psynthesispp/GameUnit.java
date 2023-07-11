package psynthesispp;

import psynthesispp.preset.Hexagon;
import psynthesispp.preset.PlayerColor;

/**
 * GameUnit-Klasse als Allgemeines Spielobjekt auf dem Spielfeld mit Referenz auf ein Hexagon-Objekt und dem HexagonType
 *
 * @author Jannik
 */
public class GameUnit implements Cloneable {
	int size = -1;
	private Hexagon hexagon;
	private PlayerColor playerOwner;

	/**
	 * Erstellt ein Spielbrett-Object mit einem zugehoerigen Hexagon, dem Besitzer des Objektes und der Groesse der Pflanze
	 *
	 * @param y y-Index des Hexagons
	 * @param x x-Index des Hexagons
	 * @param size Groesse der Pflanze (bei keiner Pflanze -1)
	 * @param playerOwner Besitzer des Objektes
	 */
	public GameUnit(int y, int x, int size, PlayerColor playerOwner) {
		this.hexagon = new Hexagon(y, x);
		this.playerOwner = playerOwner;
		this.size = size;
	}

	/**
	 * Gibt eine DeepCopy von GameUnit zurueck
	 *
	 * @return Kopie von GameUnit
	 */
	@Override
	public GameUnit clone() {
		return new GameUnit(getY(), getX(), size, playerOwner);
	}

	/**
	 * Gibt Hexagon der GameUnit zurueck
	 *
	 * @return Hexagon auf dem das GameUnit steht
	 */
	public Hexagon getHexagon() {
		return hexagon;
	}

	/**
	 * Gibt Groesse des Baumes zurueck
	 *
	 * @return Groesse des Baumes
	 */
	public int getSize() {
		return size;
	}

	/**
	 * Gibt Besitzer der GameUnit-Einheit zurueck
	 *
	 * @return GameUnit-Besitzer
	 */
	public PlayerColor getPlayerOwner() {
		return playerOwner;
	}

	/**
	 * Gibt y-Koordinate der GameUnit-Einheit zurueck
	 *
	 * @return y-Koordinate
	 */
	public int getY() {
		return getHexagon().getColumn();
	}

	/**
	 * Gibt x-Koordinate der GameUnit-Einheit zurueck
	 *
	 * @return x-Koordinate
	 */
	public int getX() {
		return getHexagon().getRow();
	}

	/**
	 * Gibt String-Repraesentation zurueck
	 */
	@Override
	public String toString() {
		switch (size) {
		case -1:
			return "[-:-]";
		case 0:
			return (playerOwner == PlayerColor.Red) ? "[S+0]" : "[S-0]";
		default:
			return (playerOwner == PlayerColor.Red) ? "[B+" + size + "]" : "[B-" + size + "]";
		}
	}

	/**
	 * Setzt die Groesse der Pflanze auf der GameUnit
	 *
	 * @param size Groesse der Pflanze
	 */
	public void setSize(int size) {
		this.size = size;
	}

	/**
	 * Erhöhe die Groesse um 1
	 */
	public void grow() {
		size++;
	}

	/**
	 * Gibt zurueck, ob auf dem Hexagon ein Baum/Samen ist
	 *
	 * @return true, wenn Baum/Hexagon drauf ist / false, wenn nicht
	 */
	public boolean isTree() {
		return size >= 0;
	}
}

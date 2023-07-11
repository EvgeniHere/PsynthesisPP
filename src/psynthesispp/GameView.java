package psynthesispp;

import psynthesispp.preset.MoveType;
import psynthesispp.preset.PlayerColor;
import psynthesispp.preset.Status;
import psynthesispp.preset.Viewer;

/**
 * Read-Only Darstellung des Spielbretts
 *
 * @author Jannik
 */
public class GameView implements Viewer {

	private int k;
	private int round;
	private int sunPos;
	private Status status;
	private boolean gameOver;
	private int sunRevolutions;
	private boolean[][] usedField;
	private PlayerColor turnColor;
	private GameUnit[][] gamefield;
	private Inventory inventoryRed;
	private int[] numTreesCompleted;
	private Inventory inventoryBlue;
	private MoveType phaseRed;
	private MoveType phaseBlue;

	/**
	 * DeepCopy der Darstellung des Spielbretts
	 *
	 * @param size Spielbrettgroesse
	 * @param round Runde auf dem Spielbrett
	 * @param gamefield GameUnits des Spielbretts
	 * @param turnColor Farbe des Spielers, der momentan an der Reihe ist
	 * @param phaseRed Phase, in der sich Spieler Rot befindet
	 * @param phaseBlue Phase, in der sich Spieler Blau befindet
	 * @param inventoryRed Inventar des roten Spielers
	 * @param inventoryBlue Inventar des blauen Spielers
	 * @param status Status des Spielbretts
	 * @param gameOver Spiel beendet
	 * @param sunPos momentane Sonnenpositionen
	 * @param sunRevolutions Anzahl der vergangenen Sonnenumläufe
	 * @param numTreesCompleted Anzahl der Baeume, die aufgelöst wurden, verteilt auf deren Abstand zur Mitte
	 * @param usedField in diesem Zug benutzte GameUnits
	 */
	public GameView(int size, int round, GameUnit[][] gamefield, PlayerColor turnColor,
			MoveType phaseRed, MoveType phaseBlue, Inventory inventoryRed, Inventory inventoryBlue,
			Status status, boolean gameOver, int sunPos, int sunRevolutions, int[] numTreesCompleted, boolean[][] usedField) {
		this.k = size;
		this.round = round;
		this.status = status;
		this.sunPos = sunPos;
		this.gameOver = gameOver;
		this.phaseRed = phaseRed;
		this.phaseBlue = phaseBlue;
		this.turnColor = turnColor;
		this.usedField = new boolean[usedField.length][usedField[0].length];
		for (int i = 0; i < usedField.length; i++) {
			for (int j = 0; j < usedField[i].length; j++) {
				this.usedField[i][j] = usedField[i][j];
			}
		}
		this.sunRevolutions = sunRevolutions;
		this.inventoryRed = inventoryRed.clone();
		this.inventoryBlue = inventoryBlue.clone();
		this.numTreesCompleted = numTreesCompleted.clone();
		this.gamefield = new GameUnit[gamefield.length][gamefield[0].length];
		for (int i = 0; i < gamefield.length; i++) {
			for (int j = 0; j < gamefield[i].length; j++) {
				GameUnit curUnit = gamefield[i][j];

				if (curUnit == null)
					continue;

				if (curUnit.isTree()) {
					GameUnit curTree = gamefield[i][j];
					GameUnit treeCopy = curTree.clone();
					this.gamefield[i][j] = treeCopy;
				} else {
					this.gamefield[i][j] = gamefield[i][j].clone();
				}
			}
		}
	}

	/**
	 * Gibt Spielbrettgroesse zurueck
	 */
	@Override
	public int getSize() {
		return k;
	}

	/**
	 * Gibt Runde des Spielbretts zurueck
	 */
	@Override
	public int getRound() {
		return round;
	}

	/**
	 * Gibt Position der Sonne zurueck
	 */
	@Override
	public int getSunPos() {
		return sunPos;
	}

	/**
	 * Gibt Status des Spielbretts zurueck
	 */
	@Override
	public Status getStatus() {
		return status;
	}

	/**
	 * Gibt zurueck, ob das Spiel beendet ist
	 */
	@Override
	public boolean isGameOver() {
		return gameOver;
	}

	/**
	 * Gibt Farbe des Spielers, der an der Reihe ist, zurueck
	 */
	@Override
	public PlayerColor getTurnColor() {
		return turnColor;
	}

	/**
	 * Gibt die GameUnits des Spielbretts zurueck
	 */
	@Override
	public GameUnit[][] getField() {
		return gamefield;
	}

	/**
	 * Gibt die vergangenen Sonnenumlaeufe zurueck
	 */
	@Override
	public int getSunRevolutions() {
		return sunRevolutions;
	}

	/**
	 * Gibt die in diesem Zug benutzten GameUnits zurueck
	 */
	@Override
	public boolean[][] getUsedField() {
		return usedField;
	}

	/**
	 * Gibt Anzahl der Baeume, die aufgeloest wurden, verteilt auf deren Abstand zur Mitte zurueck
	 */
	@Override
	public int[] getNumTreesCompleted() {
		return numTreesCompleted;
	}

	/**
	 * Gibt Phase des gewuenschten Spielers zurueck
	 *
	 * @param playerColor Farbe des gewuenschten Spielers
	 */
	@Override
	public MoveType getPhaseOf(PlayerColor playerColor) {
		if (playerColor == PlayerColor.Red)
			return phaseRed;
		else
			return phaseBlue;
	}

	/**
	 * Gibt Inventar des gewuenschten Spielers zurueck
	 *
	 * @param playerColor Farbe des gewuenschten Spielers
	 */
	@Override
	public Inventory getInventoryOf(PlayerColor playerColor) {
		if (playerColor == PlayerColor.Red)
			return inventoryRed;
		else
			return inventoryBlue;
	}

	/**
	 * Gibt das Inventar des Spielers, der gerade an der Reihe ist, zurueck
	 *
	 * @return Inventar des Spielers, der gerade an der Reihe ist
	 */
	public Inventory getCurrentInventory() {
		return getInventoryOf(turnColor);
	}

	/**
	 * Gib true zurueck, falls y und x ausserhalb des Spielfeld-Arrays liegen
	 *
	 * @param y 1. Spielfeld-Array-Index
	 * @param x 2. Spielfeld-Array-Index
	 * @return boolean y und x liegen nicht im Index-Bereich des Spielfeld-Arrays
	 */
	public boolean outOfBounds(int y, int x) {
		return (y < 0 || x < 0 || y >= gamefield.length || x >= gamefield[y].length);
	}

	/**
	 * Gibt true zurueck, falls y und x ausserhalb des Spielfeld-Arrays liegen oder das Array an der Stelle einen leeren Inhalt hat
	 *
	 * @param y 1. Spielfeld-Array-Index
	 * @param x 2. Spielfeld-Array-Index
	 * @return y und x liegen nicht im Bereich des Spielfeld-Arrays oder das Array hat an der Stelle leeren Inhalt
	 */
	public boolean isNull(int y, int x) {
		return outOfBounds(y, x) || gamefield[y][x] == null;
	}

	/**
	 * Fuegt ein GameUnit-Objekt dem Spielfeld hinzu
	 *
	 * @param newTree ein GameUnit-Objekt
	 */
	public void addUnit(GameUnit newTree) {
		int treeY = newTree.getY();
		int treeX = newTree.getX();

		if (isNull(treeY, treeX))
			return;

		gamefield[treeY][treeX] = newTree;
	}
}

package psynthesispp;
import java.util.ArrayList;

import psynthesispp.preset.Hexagon;
import psynthesispp.preset.HexagonTuple;
import psynthesispp.preset.Move;
import psynthesispp.preset.MoveType;
import psynthesispp.preset.Playable;
import psynthesispp.preset.PlayerColor;
import psynthesispp.preset.Status;
import psynthesispp.preset.Viewable;
import psynthesispp.preset.Viewer;

/**
 * Koordiniert Spielverlauf mit Spielfeld, Inventaren und Phasen fuer jeden Spieler und Sonnenrotation
 *
 * @author evgen
 */
public class Spielbrett implements Playable, Viewable, Cloneable {

	private int n;
	private int k;

	private Status status;
	private int round;
	private int sunPos;
	private int sunRevolutions;
	private GameUnit[][] gamefield;
	private boolean[][] usedField;
	private boolean gameOver;
	private PlayerColor turnColor;
	private int[] numTreesCompleted;

	private final int neighbourOffsetYX[][] = {{-1, -1},  {0, -1}, {+1, 0}, {+1, +1},  {0, +1}, {-1, 0}}; //Unten-Links, Links, Oben-Links, Oben-Rechts, Rechts, Unten-Rechts
	private final int sunshineDirYX[][] = neighbourOffsetYX;

	private Inventory inventoryRed;
	private Inventory inventoryBlue;

	private MoveType phaseRed;
	private MoveType phaseBlue;

	/**
	 * Initialisiere Spielbrett mit der uebergebenen Groesse
	 *
	 * @param size Groesse des Spielbretts
	 */
	public Spielbrett(int size) {
		init(size);
	}

	/**
	 * Weist die Anfangswerte dem Spielbrett zu
	 *
	 * @param size Groesse des Spielbretts
	 */
	private void init(int size) {
		k = size;
		n = k * 2 + 1;
		round = 0;
		sunPos = 0;
		sunRevolutions = 0;
		gameOver = false;
		status = Status.Ok;

		if (k < 1 || k > 5)
			System.exit(0);

		gamefield = new GameUnit[n][n];

		for (int i = 0; i < gamefield.length; i++) {
			for (int j = 0; j < gamefield[i].length; j++) {
				gamefield[i][j] = new GameUnit(i, j, -1, null);
			}
		}

		for (int i = 0; i < k; i++) {
			for (int j = 0; j < k-i; j++) {
				gamefield[i][n - j - 1] = null;
				gamefield[n - i - 1][j] = null;
			}
		}

		phaseRed = MoveType.Prepare;
		phaseBlue = MoveType.Prepare;

		inventoryRed = new Inventory(k);
		inventoryBlue = new Inventory(k);

		turnColor = PlayerColor.Red;

		resetUsedField();

		numTreesCompleted = new int[k+1];
	}

	/**
	 * Erstellt eine neue Read-Only Darstellung des Spielbretts und gibt diese zurueck
	 *
	 * @return Read-Only Darstellung des Spielbretts
	 */
	@Override
	public Viewer viewer() {
		return new GameView(k, round, gamefield, turnColor, phaseRed, phaseBlue, inventoryRed, inventoryBlue, status, gameOver, sunPos, sunRevolutions, numTreesCompleted, usedField);
	}

	/**
	 * DeepCopy des Spielbretts
	 *
	 * @return Kopie des Spielbretts
	 */
	@Override
	public Spielbrett clone() {
		GameView spielbrettViewer = (GameView)viewer();
		return new Spielbrett(spielbrettViewer);
	}

	/**
	 * Erstellt ein Spielbrett anhand der Werte des Viewers
	 *
	 * @param viewer Read-Only Darstellung eines Spielbretts
	 */
	public Spielbrett (Viewer viewer) {
		this.k = viewer.getSize();
		this.round = viewer.getRound();
		this.status = viewer.getStatus();

		this.sunPos = viewer.getSunPos();
		this.sunRevolutions = viewer.getSunRevolutions();
		this.gameOver = viewer.isGameOver();

		this.turnColor = viewer.getTurnColor();
		this.phaseRed = viewer.getPhaseOf(PlayerColor.Red);
		this.phaseBlue = viewer.getPhaseOf(PlayerColor.Blue);

		boolean[][] newUsedField = viewer.getUsedField();
		this.usedField = new boolean[newUsedField.length][newUsedField[0].length];

		for (int i = 0; i < usedField.length; i++) {
			for (int j = 0; j < usedField[i].length; j++) {
				this.usedField[i][j] = newUsedField[i][j];
			}
		}

		this.inventoryRed = viewer.getInventoryOf(PlayerColor.Red).clone();
		this.inventoryBlue = viewer.getInventoryOf(PlayerColor.Blue).clone();

		int[] originalNumTreesCompleted = viewer.getNumTreesCompleted();
		this.numTreesCompleted = new int[originalNumTreesCompleted.length];
		System.arraycopy(originalNumTreesCompleted, 0, this.numTreesCompleted, 0, numTreesCompleted.length);

		GameUnit[][] originalGameField = viewer.getField();
		this.gamefield = new GameUnit[originalGameField.length][originalGameField[0].length];

		for (int i = 0; i < gamefield.length; i++) {
			for (int j = 0; j < gamefield[i].length; j++) {
				GameUnit curUnit = originalGameField[i][j];

				if (curUnit == null)
					continue;

				this.gamefield[i][j] = curUnit.clone();
			}
		}
	}

	/**
	 * setzt alle Felder des Spielbretts zurueck auf unbenutzt
	 */
	private void resetUsedField() {
		usedField = new boolean[gamefield.length][gamefield[0].length];
	}

	/**
	 * Berechnet die erzeugte Energie auf einem Unit und gibt diese zurueck
	 *
	 * @param originHex Hexagon, dessen Energy berechnet werden soll
	 * @param sunPos Richtungsindex der Sonneinstrahlung
	 * @return produzierte Energy des Baumes auf originHex
	 */
	public int getProducedEnergyOf(Hexagon originHex, int sunPos) {
		int origUnitY = originHex.getColumn();
		int origUnitX = originHex.getRow();

		if (isNull(origUnitY, origUnitX))
			return 0;

		GameUnit originTree = gamefield[origUnitY][origUnitX];

		if (!originTree.isTree())
			return 0;

		int maxTreeSize = k;
		int testUnitY = origUnitY;
		int testUnitX = origUnitX;

		for (int i = 0; i < maxTreeSize; i++) {
			testUnitY += sunshineDirYX[sunPos][0];
			testUnitX += sunshineDirYX[sunPos][1];

			if (outOfBounds(testUnitY, testUnitX))
				return originTree.getSize();

			if (isNull(testUnitY, testUnitX))
				return originTree.getSize();

			GameUnit testUnit = gamefield[testUnitY][testUnitX];

			if (!testUnit.isTree())
				continue;

			GameUnit testTree = testUnit;
			if (testTree.getSize() > i) {
				return 0;
			}
		}

		return originTree.getSize();
	}

	/**
	 * Dreht die Sonne um eine Position weiter (Erhoeht die Sonnenposition um 1)
	 */
	private void rotateSun() {
		sunPos++;
		if (sunPos > 5) {
			sunRevolutions++;
			if (sunRevolutions == 6)
				checkWin();

			sunPos = 0;
		}
	}

	/**
	 * Berechnet und fuegt dem jeweiligen Inventar die in dieser Runde erhaltene Energie hinzu
	 */
	private void sunshine() {
		ArrayList<Hexagon> treesRed = inventoryRed.getPlantedTrees();
		ArrayList<Hexagon> treesBlue = inventoryBlue.getPlantedTrees();

		for (int i = 0; i < treesRed.size(); i++) {
			inventoryRed.addEnergy(getProducedEnergyOf(treesRed.get(i), sunPos));
		}
		for (int i = 0; i < treesBlue.size(); i++) {
			inventoryBlue.addEnergy(getProducedEnergyOf(treesBlue.get(i), sunPos));
		}
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
	 * Gibt true zurueck, falls gamefield an Stelle y und x ein unbepflanztes Feld hat
	 *
	 * @param y 1. Spielfeld-Array-Index
	 * @param x 2. Spielfeld-Array-Index
	 * @return y und x liegen nicht im Bereich des Spielfeld-Arrays oder das Array hat an der Stelle keinen Samen/Baum
	 */
	public boolean isEmpty(int y, int x) {
		return (!isNull(y, x)) ? (!gamefield[y][x].isTree()) : false;
	}

	/**
	 * Prueft, ob das ausgewaehlte Hexagon fuer den Vorbereitungszug geeignet ist
	 *
	 * @param hex zu testendes Hexagon
	 * @return Wahrheitswert, ob geeignet oder nicht
	 */
	public boolean isPreparable(Hexagon hex) {
		int hexY = hex.getColumn();
		int hexX = hex.getRow();

		if (isNull(hexY, hexX)) {
			//print("Selected hexagon is null!");
			return false;
		}

		GameUnit centerUnit = gamefield[k][k];

		if (!isInRange(k, centerUnit.getHexagon(), hex)) {
			//print("Selected hexagon is not in distance " + k + " of the center!");
			return false;
		}

		if (!isEmpty(hexY, hexX)) {
			//print("Selected hexagon is not empty!");
			return false;
		}

		return true;
	}

	/**
	 * Prueft, ob die Inhalte des Parameters aktiviert werden koennen
	 *
	 * @param numOfElements zu aktivierende Elemente
	 * @return Wahrheitswert, ob die Elemente aktiviert werden koennen
	 */
	public boolean isActivateable(ArrayList<Integer> numOfElements) {
		Inventory curInventory = getCurrentInventory().clone();

		int energyLeft = curInventory.getEnergy();
		int[] passiveInventory = curInventory.getPassiveInventory();
		int[] maxPassiveInventory = curInventory.getMaxPassiveInventory();

		if (numOfElements.size() > passiveInventory.length)
			return false;

		for (int i = 0; i < numOfElements.size(); i++) {
			if (passiveInventory[i] < numOfElements.get(i))
				return false;

			int diff = 1;
			if (passiveInventory[i] * 2 < maxPassiveInventory[i])
				diff = 2;

			energyLeft -= numOfElements.get(i) * (i + diff);

			if (energyLeft < 0)
				return false;
		}

		return true;
	}

	/**
	 * Berechnet die minimale Distanz zwischen zwei Hexagonen in O(1)
	 *
	 * @param hex1 erste Hexagon
	 * @param hex2 zweite Hexagon
	 * @return Distanz zwischen beiden Hexagonen
	 */
	private int minDistance(Hexagon hex1, Hexagon hex2) {
		int y1 = hex1.getColumn();
		int x1 = hex1.getRow();
		int y2 = hex2.getColumn();
		int x2 = hex2.getRow();

		int diffX = x2 - x1;
		int diffY = y2 - y1;

		int distance = 0;
		if (diffX * diffY > 0) {
			int steps;
			if (diffX > 0)
				steps = Math.min(diffX, diffY);
			else
				steps = Math.max(diffX, diffY);
			diffX -= steps;
			diffY -= steps;
			distance += Math.abs(steps);
		}
		distance += Math.abs(diffX) + Math.abs(diffY);

		return distance;
	}

	/**
	 * Prueft, ob ein Hexagon genau einen gewissen Abstand zum anderen Hexagons hat
	 *
	 * @param radius Abstand
	 * @param hex1 Erste Hexagon
	 * @param hex2 Zweite Hexagon
	 * @return Wahrheitswert, ob ein Hexagon genau einen gewissen Abstand zum anderen Hexagons hat
	 */
	private boolean isInRange(int radius, Hexagon hex1, Hexagon hex2) {
		int distance = minDistance(hex1, hex2);

		return distance == radius;
	}

	/**
	 * Uberprueft, ob der Inhalt des Parameters pflanzbar ist
	 *
	 * @param treeToSeedTuples Hexagon-Paare für Baeume und dazugehoerige Samen
	 * @return Wahrheitswert, ob Inhalte pflanzbar sind
	 */
	public boolean isPlantable(ArrayList<HexagonTuple> treeToSeedTuples) {
		Inventory curInventory = getCurrentInventory().clone();

		if (curInventory.getEnergy() < treeToSeedTuples.size())
			return false;

		if (curInventory.getActiveInventory()[0] < treeToSeedTuples.size())
			return false;

		boolean[][] tmpUsedField = new boolean[usedField.length][usedField[0].length];

		for (int i = 0; i < tmpUsedField.length; i++) {
			for (int j = 0; j < tmpUsedField[i].length; j++) {
				tmpUsedField[i][j] = usedField[i][j];
			}
		}

		int[] activeElements = curInventory.getActiveInventory();

		for (int i = 0; i < treeToSeedTuples.size(); i++) {
			Hexagon from = treeToSeedTuples.get(i).getFrom();
			Hexagon to = treeToSeedTuples.get(i).getTo();
			int fromY = from.getColumn();
			int fromX = from.getRow();
			int toY = to.getColumn();
			int toX = to.getRow();

			if (activeElements[0] <= 0)
				return false;

			if (isNull(fromY, fromX))
				return false;

			if (isNull(toY, toX))
				return false;

			GameUnit fromUnit = gamefield[fromY][fromX];
			GameUnit toUnit = gamefield[toY][toX];

			if (!fromUnit.isTree())
				return false;

			if (fromUnit.getPlayerOwner() != turnColor)
				return false;

			if (!isEmpty(toY, toX))
				return false;

			GameUnit tree = gamefield[fromY][fromX];

			if (!isInRange(tree.getSize(), tree.getHexagon(), toUnit.getHexagon()))
				return false;

			if (tmpUsedField[fromY][fromX])
				return false;

			if (tmpUsedField[toY][toX])
				return false;

			tmpUsedField[fromY][fromX] = true;
			tmpUsedField[toY][toX] = true;
		}

		return true;
	}

	/**
	 * Uberprueft, ob man die Inhalte des Paramaters wachsen lassen kann
	 *
	 * @param growList Hexagone, die wachsen sollen
	 * @return Wahrheitswerte, ob man die Inhalte des Paramaters wachsen lassen kann
	 */
	public boolean isGrowable(ArrayList<Hexagon> growList) {
		Inventory curInventory = getCurrentInventory().clone();

		boolean[][] tmpUsedField = new boolean[usedField.length][usedField[0].length];
		for (int i = 0; i < tmpUsedField.length; i++) {
			System.arraycopy(usedField[i], 0, tmpUsedField[i], 0, usedField.length);
		}

		int[] origActiveElements = getCurrentInventory().getActiveInventory();
		int[] activeElements = new int[origActiveElements.length];
		System.arraycopy(origActiveElements, 0, activeElements, 0, origActiveElements.length);

		int energyLeft = curInventory.getEnergy();

		for (int i = 0; i < growList.size(); i++) {
			Hexagon curHex = growList.get(i);
			int hexY = curHex.getColumn();
			int hexX = curHex.getRow();

			if (isNull(hexY, hexX))
				return false;

			if (isEmpty(hexY, hexX))
				return false;

			GameUnit tree = gamefield[hexY][hexX];

			if (tree.getPlayerOwner() != turnColor)
				return false;

			if (tmpUsedField[hexY][hexX])
				return false;

			int newTreeSize = tree.getSize() + 1;

			energyLeft -= newTreeSize;

			if (energyLeft <= 0)
				return false;

			tmpUsedField[hexY][hexX] = true;

			if (newTreeSize > k)
				continue;

			if (activeElements[newTreeSize] <= 0)
				return false;

			activeElements[newTreeSize]--;
		}

		return true;
	}

	/**
	 * Fuehrt einen Vorbereitungszug fuer das uebergebene Hexagon aus aus
	 *
	 * @param hex uebergebenens Hexagon
	 * @param size
	 * @return Status des gemachten Zuges
	 */
	private Status prepare(Hexagon hex) {
		int hexY = hex.getColumn();
		int hexX = hex.getRow();

		GameUnit tree = new GameUnit(hexY, hexX, 1, turnColor);

		if (!isPreparable(hex))
			return Status.Illegal;

		Inventory curInventory = getCurrentInventory();

		gamefield[hexY][hexX] = tree;
		curInventory.addPlantedTree(hex);
		curInventory.usePrepareTree();

		return Status.Ok;
	}

	/**
	 * Fuehrt einen Aktivierungszug fuer die uebergebene Anzahl an Elemente aus
	 *
	 * @param numOfElements uebergebene Anzahl an Elemente
	 * @return Status des gemachten Zuges
	 */
	private Status activate(ArrayList<Integer> numOfElements) {
		if (!isActivateable(numOfElements))
			return Status.Illegal;

		Inventory curInventory = getCurrentInventory();

		int[] activeInventory = curInventory.getActiveInventory();
		int[] passiveInventory = curInventory.getPassiveInventory();
		int[] maxPassiveInventory = curInventory.getMaxPassiveInventory();

		for (int i = 0; i < numOfElements.size(); i++) {

			int diff = 1;
			if (passiveInventory[i] * 2 < maxPassiveInventory[i])
				diff = 2;

			int energyDecrement = numOfElements.get(i) * (i + diff);
			curInventory.addEnergy(-energyDecrement);

			activeInventory[i] += numOfElements.get(i);
			passiveInventory[i] -= numOfElements.get(i);
		}

		return Status.Ok;
	}

	/**
	 * Fuehrt einen Pflanz-Zug fuer die uebergebenen Hexagon-Paare (Baum + Samen) aus
	 *
	 * @param treeToSeedTuples Liste von Hexagon-Paaren
	 */
	private Status plant(ArrayList<HexagonTuple> treeToSeedTuples) {
		if (!isPlantable(treeToSeedTuples))
			return Status.Illegal;

		Inventory curInventory = getCurrentInventory();

		int[] activeElements = curInventory.getActiveInventory();

		for (int i = 0; i < treeToSeedTuples.size(); i++) {
			Hexagon hexFrom = treeToSeedTuples.get(i).getFrom();
			Hexagon hexTo = treeToSeedTuples.get(i).getTo();
			int treeY = hexFrom.getColumn();
			int treeX = hexFrom.getRow();
			int seedY = hexTo.getColumn();
			int seedX = hexTo.getRow();

			usedField[treeY][treeX] = true;
			usedField[seedY][seedX] = true;

			GameUnit seed = new GameUnit(seedY, seedX, 0, turnColor);
			gamefield[seedY][seedX] = seed;

			curInventory.addPlantedTree(seed.getHexagon());
			activeElements[0]--;
		}

		int energyDecrement = treeToSeedTuples.size();
		curInventory.addEnergy(-energyDecrement);

		return Status.Ok;
	}

	/**
	 * Berechnet die Punkte beim Aufloesen eines Baumes
	 *
	 * @param distanceFromCenter Abstand vom mittleren Hexagon (GameUnit)
	 * @param completedTrees Anzahl der aufgeloesten Baeume mit selbem Abstand zum mittleren Hexagon
	 * @return Punkte
	 */
	private int completeTree(int distanceFromCenter, int completedTrees) {
		int d = distanceFromCenter;
		int i = completedTrees;
		int addPoints;

		if (d <= 1)
			addPoints = 20 - (int) Math.ceil((i - 1) / 2);
		else
			addPoints = (22 - 2 * d - (int) Math.ceil((i - 1) / (d + 1)));

		return addPoints;
	}

	/**
	 * Fuehrt einen Wachstums-Zug fuer die uebergebenen Hexagone aus
	 *
	 * @param growList Liste von Hexagonen, die Baume sind, welche wachsen sollen
	 * @return Status des gemachten Zuges
	 */
	private Status grow(ArrayList<Hexagon> growList) {
		if (!isGrowable(growList))
			return Status.Illegal;

		Inventory curInventory = getCurrentInventory();

		int[] activeElements = curInventory.getActiveInventory();
		int[] passiveElements = curInventory.getPassiveInventory();
		int[] maxPassiveElements = curInventory.getMaxPassiveInventory();

		for (int i = 0; i < growList.size(); i++) {
			int hexY = growList.get(i).getColumn();
			int hexX = growList.get(i).getRow();

			usedField[hexY][hexX] = true;

			GameUnit tree = gamefield[hexY][hexX];
			int treeSize = tree.getSize();

			if (passiveElements[treeSize] + 1 <= maxPassiveElements[treeSize])
				passiveElements[treeSize]++;

			curInventory.addEnergy(-(treeSize + 1));

			if (treeSize + 1 > k) {
				GameUnit centerUnit = gamefield[k][k];
				int distanceToCenter = minDistance(centerUnit.getHexagon(), tree.getHexagon());
				int numCompletions = ++numTreesCompleted[distanceToCenter];
				int newPoints = completeTree(distanceToCenter, numCompletions);
				curInventory.addPoints(newPoints);
				curInventory.removePlantedTree(tree.getHexagon());
				gamefield[hexY][hexX] = new GameUnit(hexY, hexX, -1, null);
				continue;
			}

			activeElements[treeSize + 1]--;

			tree.grow();
		}

		return Status.Ok;
	}

	/**
	 * Wechselt den Spieler, der an der Reihe ist
	 *
	 * @param playerColor Spieler, der momentan an der Reihe ist
	 */
	private void switchPlayerTurn(PlayerColor playerColor) {
		turnColor = (playerColor == PlayerColor.Red) ? PlayerColor.Blue : PlayerColor.Red;
	}

	/**
	 * Bestimmt und setzt die naechste Phase anhand der aktuellen Phase
	 */
	private void nextPhase() {
		MoveType curPhase = getPhaseOf(turnColor);

		switch (curPhase) {
		case Prepare:
			if (getCurrentInventory().getPrepareTrees() <= 0) {
				if (turnColor == PlayerColor.Blue)
					sunshine();

				setPhaseOf(turnColor, MoveType.Activate);
			}

			switchPlayerTurn(turnColor);
			break;
		case Activate:
			setPhaseOf(turnColor, MoveType.Plant);
			break;
		case Plant:
			setPhaseOf(turnColor, MoveType.Grow);
			break;
		case Grow:
			if (turnColor == PlayerColor.Blue) {
				rotateSun();
				sunshine();
				round++;
			}

			if (isGameOver())
				return;

			resetUsedField();

			setPhaseOf(turnColor, MoveType.Activate);
			switchPlayerTurn(turnColor);
			break;
		default:
			break;
		}
	}

	/**
	 * Ueberprueft, ob die uebergebene Phase mit der momentanen Phase uebereinstimmt
	 *
	 * @param expectedPhase uebergebene, zu erwartende Phase
	 * @return Wahrheitswert, ob die uebergebene Phase mit der momentanen Phase uebereinstimmt
	 */
	private boolean isInPhase(MoveType expectedPhase) {
		MoveType curPhase = getPhaseOf(turnColor);
		boolean isRightPhase = (curPhase == expectedPhase);

		if (!isRightPhase)
			status = Status.Illegal;

		return isRightPhase;
	}

	/**
	 * Ueberprueft, ob 6 Sonnenumlaeufe vorbei sind und bestimmt den Gewinner
	 */
	private void checkWin() {
		if (sunRevolutions != 6)
			return;

		int points1 = inventoryRed.getPoints();
		int points2 = inventoryBlue.getPoints();

		if (points1 > points2) {
			status = Status.RedWin;
		} else if (points1 < points2) {
			status = Status.BlueWin;
		} else {
			int energy1 = inventoryRed.getEnergy();
			int energy2 = inventoryBlue.getEnergy();
			if (energy1 > energy2)
				status = Status.RedWin;
			else
				status = Status.BlueWin;
		}

		gameOver = true;
	}

	/**
	 * Prueft, ob Phase des Parameters mit der erwarteten Phase uebereinstimmt, fuehrt den Zug aus, sobald es regelkonform ist, und setzt die naechste Phase
	 *
	 * @param move uebergebene Zug (Move)
	 */
	@Override
	public void make(Move move) throws IllegalStateException {
		if (gameOver) {
			throw new IllegalStateException("Spiel bereits beendet!");
		}

		if (move == null) {
			status = Status.Illegal;
			return;
		}

		switch (move.getType()) {
		case Surrender:
			if (turnColor == PlayerColor.Red)
				status = Status.BlueWin;
			else
				status = Status.RedWin;
			return;
		case Prepare:
			if (!isInPhase(MoveType.Prepare))
				return;

			status = prepare(move.getPrepare());

			if (status == Status.Illegal)
				return;

			break;
		case Activate:
			if (!isInPhase(MoveType.Activate))
				return;

			resetUsedField();

			status = activate(move.getActivate());

			if (status == Status.Illegal)
				return;

			break;
		case Plant:
			if (!isInPhase(MoveType.Plant))
				return;

			status = plant(move.getPlant());

			if (status == Status.Illegal)
				return;

			break;
		case Grow:
			if (!isInPhase(MoveType.Grow))
				return;

			status = grow(move.getGrow());

			if (status == Status.Illegal)
				return;

			break;
		case Empty:
			break;
		default:
			break;
		}

		nextPhase();
	}

	/**
	 * Gibt Inventar des gewuenschten Spielers zurueck
	 *
	 * @param color Farbe des gewuenschten Spielers
	 * @return Inventar des gewuenschten Spielers
	 */
	private Inventory getInventoryOf(PlayerColor color) {
		return (color == PlayerColor.Red) ? inventoryRed : inventoryBlue;
	}

	/**
	 * Setzt Phase, in der sich der Spieler befindet
	 *
	 * @param phase Phase
	 */
	private void setPhaseOf(PlayerColor color, MoveType phase) {
		if (color == PlayerColor.Red)
			phaseRed = phase;
		else
			phaseBlue = phase;
	}

	/**
	 * Gibt Phase, in der sich der Spieler befindet, zurueck
	 *
	 * @param color Farbe des Spielers zu der die Phase abgefragt wird
	 * @return Phase Phase des Spielers mit übergebener Farbe
	 */
	public MoveType getPhaseOf(PlayerColor color) {
		return (color == PlayerColor.Red) ? phaseRed : phaseBlue;
	}

	/**
	 * Gibt das Inventar des Spielers, der gerade an der Reihe ist, zurueck
	 *
	 * @return Inventar des Spielers, der gerade an der Reihe ist
	 */
	private Inventory getCurrentInventory() {
		return getInventoryOf(turnColor);
	}

	/**
	 * Gibt die Farbe des Spielers, der aktuell an der Reihe ist, zurueck
	 *
	 * @return Farbe des Spielers, der aktuell an der Reihe ist
	 */
	public PlayerColor getTurnColor() {
		return turnColor;
	}

	/**
	 * Gibt den Status zurueck
	 *
	 * @return Status
	 */
	public Status getStatus() {
		return status;
	}

	/**
	 * Gibt zurueck, ob das Spiel vorueber ist oder nicht
	 *
	 * @return Wahrheitswert, ob das Spiel beendet ist
	 */
	public boolean isGameOver() {
		return gameOver;
	}
}

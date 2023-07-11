package psynthesispp.player;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collections;

import psynthesispp.GameUnit;
import psynthesispp.GameView;
import psynthesispp.Inventory;
import psynthesispp.preset.Hexagon;
import psynthesispp.preset.HexagonTuple;
import psynthesispp.preset.Move;
import psynthesispp.preset.MoveType;
import psynthesispp.preset.PlayerColor;
import psynthesispp.preset.PlayerType;

/**
 * RandomBot Klasse
 * Erzeugt zufaellige Moves und gibt diese zurueck, sobald diese ausfuehrbar sind.
 *
 * @author Moritz
 *
 */
public class RandomBot extends Spieler {

	/**
	 * RandomBot Konstruktor
	 * erstellt Random Spieler und initialisiert spielbrett
	 *
	 * @param boardSize Groesse des Spielfeldes
	 * @param color Farbe des Spielers
	 */
	public RandomBot(int boardSize, PlayerColor color) {
		super(boardSize, color, PlayerType.RandomAI);
	}

	/**
	 * Fordert Zug des RandomBot an und gibt diesen zurück
	 */
	@Override
	public Move request() throws Exception, RemoteException {
		MoveType moveType = spielbrett.getPhaseOf(color);
		nextMove = generateMove(moveType);
		return nextMove;
	}

	/**
	 * Generiert einen zufälligen, möglichen Zug
	 *
	 * @param moveType Die Phase des zu erzeugenden Zuges
	 * @return Move Random generierter Zug
	 */
	private Move generateMove(MoveType moveType) {
		int n = 2 * k + 1;
		GameView gameView = (GameView)spielbrett.viewer();
		Inventory curInventory = gameView.getInventoryOf(color);
		int energyLeft = curInventory.getEnergy();
		int[] passive = curInventory.getPassiveInventory();
		ArrayList<Hexagon> plantedTrees = new ArrayList<>();
		ArrayList<Hexagon> plantedTreesRed = gameView.getInventoryOf(PlayerColor.Red).getPlantedTrees();
		ArrayList<Hexagon> plantedTreesBlue = gameView.getInventoryOf(PlayerColor.Blue).getPlantedTrees();
		plantedTrees.addAll(plantedTreesRed);
		plantedTrees.addAll(plantedTreesBlue);

		GameUnit[][] gamefield = gameView.getField();

		ArrayList<Hexagon> plantedTreesCurColor = (color == PlayerColor.Red) ? plantedTreesRed : plantedTreesBlue;

		switch (moveType) {
		/*
		 * Prepare Phase
		 * auswahl eines Zufälligen Feldes auf spielfeld
		 * Prüfen ob Zug auf Feld möglich ist
		 */
		case Prepare:
			int y = (int) (Math.random()*n);
			int x = (int) (Math.random()*n);
			Hexagon randomHex = new Hexagon(y, x);

			while (!spielbrett.isPreparable(randomHex)) {
				y = (int) (Math.random()*n);
				x = (int) (Math.random()*n);
				randomHex = new Hexagon(y, x);
			}

			return new Move(randomHex);

			/*
			 * Activate Phase
			 * Zufällige auswahl an zu aktivierenden Elementen
			 * wiederholung, bis aktivierbarer Zug gefunden wurde
			 */
		case Activate:
			if (energyLeft <= 0)
				return new Move(MoveType.Empty);

			if (noElementsIn(passive))
				return new Move(MoveType.Empty);

			ArrayList<Integer> elements = new ArrayList<>();
			boolean first = true;

			while (first || !spielbrett.isActivateable(elements)) {
				first = false;
				elements.clear();
				for (int i = 0; i < passive.length; i++) {
					int toActivate = (int) (Math.random() * 1.5);
					elements.add(toActivate);
				}
			}

			Integer[] elementsArray = elements.toArray(new Integer[0]);

			return new Move(elementsArray);

			/*
			 * Plant Phase
			 * Alle bereits vorhandenen Baeume werden betrachtet
			 * ein Baum wird Zufällig gewählt und eine passende Plant position gesucht
			 * diese wird dann zurückgegeben
			 */
		case Plant:
			if (energyLeft <= 0)
				return new Move(MoveType.Empty);

			ArrayList<HexagonTuple> plantTuples = new ArrayList<>();

			Collections.shuffle(plantedTreesCurColor);

			for (Hexagon treeHex : plantedTreesCurColor) {
				for (int j = 0; j < n-1; j++) {
					for (int k = 0; k < n-1; k++) {
						if (!spielbrett.isEmpty(j, k))
							continue;

						Hexagon testHex = gamefield[j][k].getHexagon();

						plantTuples.add(new HexagonTuple(treeHex, testHex));

						if (spielbrett.isPlantable(plantTuples))
							continue;

						plantTuples.remove(plantTuples.size() - 1);
					}
				}
			}

			if (plantTuples.size() == 0 || Math.random() < 0.1)
				return new Move(MoveType.Empty);

			HexagonTuple[] tuplesArray = plantTuples.toArray(new HexagonTuple[0]);

			return new Move(tuplesArray);

			/*
			 * Grow Phase
			 * Jeder Baum wird in zufaelliger reihenfolge betrachtet und zum Zug hinzugefügt
			 * es werden so lange elemente aus dem Zug entfernt, bis er Growable ist
			 */
		case Grow:
			if (energyLeft <= 0)
				return new Move(MoveType.Empty);

			ArrayList<Hexagon> shuffledTrees = curInventory.getPlantedTrees();
			Collections.shuffle(shuffledTrees);
			ArrayList<Hexagon> hexagons = new ArrayList<>();

			for (int i = 0; i < shuffledTrees.size(); i++) {
				Hexagon treeHex = shuffledTrees.get(i);
				GameUnit curTree = gamefield[treeHex.getColumn()][treeHex.getRow()];
				int treeSize = curTree.getSize();

				if (energyLeft < treeSize + 1)
					continue;

				hexagons.add(treeHex);
				energyLeft -= treeSize + 1;
			}

			if (shuffledTrees.size() == 0)
				return new Move(MoveType.Empty);

			//entfernt Bäume, solange nicht Growable
			while (!spielbrett.isGrowable(hexagons)) {
				for (int i = 0; i < hexagons.size(); i++) {
					if (Math.random() > 0.6)
						continue;

					hexagons.remove(i);
				}
			}

			Hexagon[] hexagonArray = hexagons.toArray(new Hexagon[0]);

			return new Move(hexagonArray);
		default:
			break;
		}

		return new Move(MoveType.Empty);
	}
}

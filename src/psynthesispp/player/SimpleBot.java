package psynthesispp.player;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collections;

import psynthesispp.GameUnit;
import psynthesispp.GameView;
import psynthesispp.Inventory;
import psynthesispp.Spielbrett;
import psynthesispp.preset.Hexagon;
import psynthesispp.preset.HexagonTuple;
import psynthesispp.preset.Move;
import psynthesispp.preset.MoveType;
import psynthesispp.preset.PlayerColor;
import psynthesispp.preset.PlayerType;

/**
 * SimpleBot Klasse
 * erstellt einen Spieler, der seine Zuege mit der meisten zu erzeugenden Energie bevorzugt
 *
 * @author Moritz
 *
 */
public class SimpleBot extends Spieler {

	/**
	 * SimpleBot Konstruktor
	 * erstellt einen Spieler des SpielerTyps SimpleAI
	 *
	 * @param boardSize Groesse des Spielfeldes
	 * @param color Farbe des Spielers
	 */
	public SimpleBot(int boardSize, PlayerColor color) {
		super(boardSize, color, PlayerType.SimpleAI);
	}

	/**
	 * Fordert Zug des SimpleBot an und gibt diesen zurück
	 */
	@Override
	public Move request() throws Exception, RemoteException {
		MoveType moveType = spielbrett.getPhaseOf(color);
		nextMove = generateMove(moveType);
		return nextMove;
	}

	/**
	 * generiert einen Move, der maximale energie erzielt
	 *
	 * @param MoveType Die Phase des angeforderten Zuges
	 * @return Move Nach einer Strategie berechneter Zug
	 */
	private Move generateMove(MoveType moveType) {
		int n = 2 * k + 1;

		Spielbrett testSb = spielbrett.clone();
		GameView gameView = (GameView)spielbrett.viewer();

		GameUnit[][] gamefield = gameView.getField();
		Inventory curInventory = gameView.getInventoryOf(color);

		int energyLeft = curInventory.getEnergy();
		int[] active = curInventory.getActiveInventory();
		int[] passive = curInventory.getPassiveInventory();

		ArrayList<Hexagon> plantedTrees = new ArrayList<>();
		ArrayList<Hexagon> plantedTreesRed = new ArrayList<>();
		ArrayList<Hexagon> plantedTreesBlue = new ArrayList<>();


		/*
		 * erstellt ein Spielfeld auf dem alle vorhandenen Bäume die maximale Größe haben
		 */
		Spielbrett maxTreeTestSb = testSb.clone();
		GameView maxTreeTestView = (GameView)maxTreeTestSb.viewer();
		GameUnit[][] maxTreeSizeField = maxTreeTestView.getField();


		for (int i = 0; i < n; i++) {
			for (int j = 0; j < n; j++) {
				if (testSb.isNull(i, j))
					continue;

				if (testSb.isEmpty(i, j))
					continue;

				maxTreeSizeField[i][j].setSize(k);

				GameUnit curTree = maxTreeSizeField[i][j];

				plantedTrees.add(curTree.getHexagon());

				if (curTree.getPlayerOwner() == PlayerColor.Red)
					plantedTreesRed.add(curTree.getHexagon());
				else
					plantedTreesBlue.add(curTree.getHexagon());
			}
		}

		ArrayList<Hexagon> plantedTreesCurPlayer = (color == PlayerColor.Red) ? plantedTreesRed : plantedTreesBlue;
		Spielbrett maxTreeSizeBrettReset = new Spielbrett(maxTreeTestView);

		switch (moveType) {
		/*
		 * Prepare Phase
		 * Jeder Baum auf dem Feld wird mit Maximaler Göße betrachtet
		 * Jede Prepare Position wird durchgegangen und über alle Sonnen Positionen die Energy gesammelt
		 * anschließend wird einer der Züge gewählt, der die meiste Energie erzeugt hätte
		 */
		case Prepare:
			Move bestMove = null;
			int maxProducableEnergy = 0;

			for (int i = 0; i < n; i++) {
				for (int j = 0; j < n; j++) {
					Hexagon newTreeHex = new Hexagon(i, j);

					if (!testSb.isPreparable(newTreeHex))
						continue;

					plantedTreesCurPlayer.add(newTreeHex);

					GameView maxTreeGameViewTest = (GameView)maxTreeSizeBrettReset.viewer();

					GameUnit modNewTree = new GameUnit(i, j, k, color);

					maxTreeGameViewTest.addUnit(modNewTree);

					Spielbrett maxTreeSb = new Spielbrett(maxTreeGameViewTest);

					int producableEnergy = 0;

					//für jeden baum wird die generierte Energy berechnet
					for (Hexagon curTreeHex : plantedTreesCurPlayer) {
						for(int m = 0; m < 6; m++) {
							producableEnergy += maxTreeSb.getProducedEnergyOf(curTreeHex, m);
						}
					}

					if (producableEnergy < maxProducableEnergy)
						continue;

					maxProducableEnergy = producableEnergy;
					bestMove = new Move(newTreeHex);
				}
			}

			return bestMove;

			/*
			 * Activate Phase
			 * Prüft was für baeume der eigenen Farbe auf dem Feld stehen
			 * und Versucht die Baeume zu aktivieren, die in den Nächsten Phase gebraucht werden könnten
			 */
		case Activate:
			if (energyLeft <= 0)
				return new Move(MoveType.Empty);

			if (noElementsIn(passive))
				return new Move(MoveType.Empty);

			int[] placedElements = new int[k + 1];

			//die Göße jedes platzierten Baumes wird in einem Array Gezählt
			for (Hexagon treeHex : plantedTreesCurPlayer) {
				int curTreeY = treeHex.getColumn();
				int curTreeX = treeHex.getRow();

				if (testSb.isNull(curTreeY, curTreeX))
					continue;

				if (testSb.isEmpty(curTreeY, curTreeX))
					continue;

				GameUnit curTree = gamefield[curTreeY][curTreeX];

				int treeSize = curTree.getSize();

				placedElements[treeSize]++;
			}

			int tmpEnergyLeft = energyLeft;
			int[] elements = new int[k + 1];

			//jede baumgöße wird einzeln betrachtet
			for (int i = 0; i < passive.length; i++) {
				if (i > 0 && placedElements[i - 1] == 0)
					continue;

				int leftPassive = passive[i];
				/*
				 * solange noch passive Element des Baumes und noch Energy zum Aktivieren übrig sind
				 * wird auch aktiviert
				 */
				while (tmpEnergyLeft >= i + 2 && leftPassive > 0) {
					if (leftPassive * 2 >= curInventory.getMaxPassiveInventory()[i])
						tmpEnergyLeft -= i + 1;
					else
						tmpEnergyLeft -= i + 2;

					leftPassive--;
					elements[i]++;
				}
			}

			Integer[] elementsArray = new Integer[k + 1];

			//jedes element wird maximal nur einmal gleichzeitig aktiviert
			for (int i = 0; i < elementsArray.length; i++) {
				elementsArray[i] = (int)Math.ceil(Math.random() * elements[i]);
			}

			return new Move(elementsArray);

			/*
			 * Jede Plant position wird betrachtet und es wird die gewählt,
			 * die bei Maximaler Baumgöße dei Meiste Energy erzeugt über alle Sonnenphasen
			 */
		case Plant:
			if (energyLeft <= 0)
				return new Move(MoveType.Empty);

			if (active[0] == 0)
				return new Move(MoveType.Empty);

			//mischen, damit er nicht immer die exakt gleichen Züge macht
			Collections.shuffle(plantedTreesCurPlayer);

			ArrayList<Integer> plantEnergy = new ArrayList<>();
			ArrayList<HexagonTuple> bestPlantTuples = new ArrayList<>();

			//alle Moeglichen plant positionen werden gespeichert in ArrayList
			for (Hexagon treeHex : plantedTreesCurPlayer) {
				for (int i = 0; i < n; i++) {
					for (int j = 0; j < n; j++) {
						if (!testSb.isEmpty(i, j))
							continue;

						Hexagon seedHex = gamefield[i][j].getHexagon();

						HexagonTuple plantTuple = new HexagonTuple(treeHex, seedHex);

						ArrayList<HexagonTuple> testTuples = new ArrayList<>();

						testTuples.add(plantTuple);

						if (!testSb.isPlantable(testTuples))
							continue;

						GameView modSeedGameViewTest = (GameView)testSb.viewer();

						GameUnit modSeed = new GameUnit(i, j, k, color);

						modSeedGameViewTest.addUnit(modSeed);

						Spielbrett modSeedSbTest = new Spielbrett(modSeedGameViewTest);


						int producableEnergy = 0;
						/*
						 * für jeden PlantTuple wird der Zug ausgeführt und die Energy berechnet
						 */
						for (Hexagon curPlantedTreeHex : plantedTreesCurPlayer) {
							for(int m = 0; m < 6; m++) {
								producableEnergy += modSeedSbTest.getProducedEnergyOf(curPlantedTreeHex, m);
							}
						}


						int insertIndex = 0;

						for (int m = 0; m < plantEnergy.size(); m++) {
							if (producableEnergy >= plantEnergy.get(m)) {
								insertIndex = m;
								break;
							}
						}

						plantEnergy.add(insertIndex, producableEnergy);
						bestPlantTuples.add(insertIndex, plantTuple);
					}
				}
			}

			//wenn nicht plantable, dann wird ein element aus der ArrayList entfernt und erneut probiert
			for (int i = 0; i < bestPlantTuples.size(); i++) {
				if (testSb.isPlantable(bestPlantTuples) && Math.random() < 0.2)
					break;

				bestPlantTuples.remove(i);
				i--;
			}

			if (bestPlantTuples.size() == 0)
				return new Move(MoveType.Empty);

			HexagonTuple[] bestMoveTuples = bestPlantTuples.toArray(new HexagonTuple[0]);

			return new Move(bestMoveTuples);

			/*
			 * Grow Phase
			 * nimmt jeden Baum, laesst ihn wachsen und speichert die moegliche Energy
			 * dann werden die Baeume nach energie sortiert und in der reihenfolge wachsen gelassen,
			 * bis keine Energie mehr übrig ist
			 */
		case Grow:
			if (energyLeft <= 0)
				return new Move(MoveType.Empty);

			Collections.shuffle(plantedTreesCurPlayer);

			ArrayList<Integer> growEnergy = new ArrayList<>();
			ArrayList<Hexagon> bestGrowTrees = new ArrayList<>();

			//jeder Baum des Spielers wird einmal wachsen gelassen
			for (Hexagon treeHex : plantedTreesCurPlayer) {
				ArrayList<Hexagon> testHexagons = new ArrayList<>();
				testHexagons.add(treeHex);

				//wenn baum nicht growable, dann nächster baum
				if (!testSb.isGrowable(testHexagons))
					continue;

				int curTreeY = treeHex.getColumn();
				int curTreeX = treeHex.getRow();

				GameUnit treeCopy = gamefield[curTreeY][curTreeX].clone();

				gamefield[curTreeY][curTreeX].grow();

				int producableEnergy = 0;
				//energy über alle Sonnenphasen sammeln
				for (Hexagon curTreeHex : plantedTreesCurPlayer) {
					for (int m = 0; m < 6; m++) {
						producableEnergy += testSb.getProducedEnergyOf(curTreeHex, m);
					}
				}

				gamefield[curTreeY][curTreeX] = treeCopy;

				int insertIndex = 0;
				//prueft ob es irgendwo einen Zug gab der Mehr Energie erzeugt hat
				for (int i = 0; i < growEnergy.size(); i++) {
					if (producableEnergy >= growEnergy.get(i)) {
						insertIndex = i;
						break;
					}
				}
				//Grow tree wird ins array einsortiert
				growEnergy.add(insertIndex, producableEnergy);
				bestGrowTrees.add(insertIndex, treeHex);
			}

			//solange, bis keine Energie mehr vorhanden ist wird wachsen gelassen

			for (int i = 0; i < bestGrowTrees.size(); i++) {
				if (testSb.isGrowable(bestGrowTrees))

					break;

				bestGrowTrees.remove(i);
				i--;
			}

			if (bestGrowTrees.size() == 0)
				return new Move(MoveType.Empty);

			Hexagon[] hexagonArray = bestGrowTrees.toArray(new Hexagon[0]);

			return new Move(hexagonArray);
		default:
			return new Move(MoveType.Empty);
		}
	}
}

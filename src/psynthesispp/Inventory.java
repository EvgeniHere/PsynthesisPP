package psynthesispp;

import java.util.ArrayList;

import psynthesispp.preset.Hexagon;

/**
 * Inventar eines Spielers
 *
 * @author Jannik
 */
public class Inventory implements Cloneable {

	private int energy = 0;
	private int points = 0;
	private int leftPrepareTrees;
	private int[] activeElements;
	private int[] passiveElements;
	private int[] maxPassiveElements;
	private ArrayList<Hexagon> plantedTrees;

	/**
	 * Fuellt das Inventar nach Vorgabe
	 *
	 * @param k Spielbrettgroesse
	 */
	public Inventory(int k) {
		leftPrepareTrees = (int) Math.ceil((2 * k) / 3.0);

		activeElements = new int[k + 1];
		passiveElements = new int[k + 1];
		maxPassiveElements = new int[passiveElements.length];

		activeElements[0] = k - 1; //Samen
		for (int i = 1; i <= k; i++) { //Baum(1) bis Baum(k)
			activeElements[k - i + 1] = i - 1;
		}
		activeElements[1] += leftPrepareTrees;

		passiveElements[0] = k + 1;
		maxPassiveElements[0] = k + 1;
		for (int i = 1; i <= k; i++) {
			passiveElements[k - i + 1] = i + 1;
			maxPassiveElements[k - i + 1] = passiveElements[k - i + 1];
		}

		plantedTrees = new ArrayList<>();
	}

	/**
	 * Gibt Anzahl der aktiven Elemente der Inventar-Einheit aus
	 *
	 * @return Anzahl der aktiven Elemente
	 */
	public int[] getActiveInventory() {
		return activeElements;
	}

	/**
	 * Gibt Anzahl der passiven Elemente der Inventar-Einheit aus
	 *
	 * @return Anzahl der passiven Elemente
	 */
	public int[] getPassiveInventory() {
		return passiveElements;
	}

	/**
	 * Gibt maximale Anzahl der passiven Elemente der Inventar-Einheit aus
	 *
	 * @return maximale Anzahl der passiven Elemente
	 */
	public int[] getMaxPassiveInventory() {
		return maxPassiveElements;
	}

	/**
	 * Fuegt einen neuen Baum den gepflanzten Baeumen der Inventar-Einheit hinzu
	 *
	 * @param tree neu gepflanzter Baum
	 */
	public void addPlantedTree(Hexagon tree) {
		plantedTrees.add(tree);
	}

	/**
	 * Entfernt einen Baum aus den gepflanzten Baeumen der Inventar-Einheit
	 *
	 * @param tree gepflanzter, zu entfernender Baum
	 */
	public void removePlantedTree(Hexagon tree) {
		plantedTrees.remove(tree);
	}

	/**
	 * Gibt die gepflanzten Baeume der Inventar-Einheit zurueck
	 *
	 * @return gepflanzte Baeume
	 */
	public ArrayList<Hexagon> getPlantedTrees() {
		return plantedTrees;
	}

	/**
	 * Verringert die Baeume der Vorbereitungsphase um eins
	 */
	public void usePrepareTree() {
		activeElements[1]--;
		this.leftPrepareTrees--;
	}

	/**
	 * Gibt die Anzahl der uebrigen Baeume der Vorbereitungsphase zurueck
	 *
	 * @return Anzahl der uebrigen Baeume
	 */
	public int getPrepareTrees() {
		return this.leftPrepareTrees;
	}

	/**
	 * Fuegt Punkte zur Inventar-Einheit hinzu
	 *
	 * @param points Punkte
	 */
	public void addPoints(int points) {
		this.points += points;
	}

	/**
	 * Gibt Punkte der Inventar-Einheit zurueck
	 *
	 * @return Punkte
	 */
	public int getPoints() {
		return points;
	}

	/**
	 * Fuegt Energie zur Inventar-Einheit hinzu
	 *
	 * @param energy Energie
	 */
	public void addEnergy(int energy) {
		this.energy += energy;
	}

	/**
	 * Gibt Energie der Inventar-Einheit zurueck
	 *
	 * @return Energie
	 */
	public int getEnergy() {
		return energy;
	}

	/**
	 * Gibt DeepCopy des Inventars zurück
	 *
	 * @return Kopie des Inventars
	 */
	@Override
	public Inventory clone() {
		return new Inventory(energy, points, leftPrepareTrees, activeElements, passiveElements, maxPassiveElements, plantedTrees);
    }

	/**
	 * Kopiert alle in den Parametern übergebene Variablen und Objekte und weist sie sich selber zu
	 *
	 * @param energy Energie
	 * @param points Punkte
	 * @param leftPrepareTrees uebrige Vorbereitunsbaeume
	 * @param activeElements aktive Elemente
	 * @param passiveElements passive Elemente
	 * @param maxPassiveElements maximale passive Elemente
	 * @param plantedTrees gepflanzte Baeume
	 */
    public Inventory(int energy, int points, int leftPrepareTrees, int[] activeElements, int[] passiveElements, int[] maxPassiveElements, ArrayList<Hexagon> plantedTrees) {
		this.energy = energy;
		this.points = points;
		this.leftPrepareTrees = leftPrepareTrees;
		this.activeElements = new int[activeElements.length];
		for (int i = 0; i < activeElements.length; i++) {
			this.activeElements[i] = activeElements[i];
		}
		this.passiveElements = new int[passiveElements.length];
		for (int i = 0; i < passiveElements.length; i++) {
			this.passiveElements[i] = passiveElements[i];
		}
		this.maxPassiveElements = new int[maxPassiveElements.length];
		for (int i = 0; i < maxPassiveElements.length; i++) {
			this.maxPassiveElements[i] = maxPassiveElements[i];
		}
		this.plantedTrees = new ArrayList<>();
		for (int i = 0; i < plantedTrees.size(); i++) {
			this.plantedTrees.add(new Hexagon(plantedTrees.get(i).getColumn(), plantedTrees.get(i).getRow()));
		}
    }

    /**
     * Inventardarstellung als String
     */
	@Override
	public String toString() {
		String erg = "";
		erg  += "Energie: " + energy + "\n";
		erg  += "Punkte: " + points + "\n";
		erg += "Aktive Elemente: ";
		for (int i = 0; i < activeElements.length; i++) {
			erg += activeElements[i] + " ";
		}
		erg += "\n";
		erg += "Passive Elements: ";
		for (int i = 0; i < passiveElements.length; i++) {
			erg += passiveElements[i] + " ";
		}
		erg += "\n";
		erg += "Max Passive Elements: ";
		for (int i = 0; i < maxPassiveElements.length; i++) {
			erg += maxPassiveElements[i] + " ";
		}
		return erg;
	}
}

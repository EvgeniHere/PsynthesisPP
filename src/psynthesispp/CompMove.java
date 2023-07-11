package psynthesispp;

import java.util.ArrayList;

import psynthesispp.preset.Hexagon;
import psynthesispp.preset.HexagonTuple;
import psynthesispp.preset.Move;

/**
 * Wrapper für ein Move-Objekt
 *
 * @author Jannik
 */
public class CompMove {

	private Move move;

	/**
	 * Weist das uebergebene Move-Objekt als Move-Objekt von CompMove zu
	 *
	 * @param move uebergebenes Move-Objekt
	 */
	public CompMove(Move move) {
		this.move = move;
	}

	/**
	 * Gibt Move-Objekt zurueck
	 *
	 * @return Move-Objekt
	 */
	 public Move getMove() {
		 return move;
	 }

	 /**
	  * Aktualisiert das Move-Objekt von CompMove mit dem uebergebenen Move-Objekt
	  *
	  * @param move uebergebenes Move-Objekt
	  */
	 public void setMove(Move move) {
		 this.move = move;
	 }

	 /**
	  * Gibt Move-Objekt in String-Form zurueck
	  *
	  * @return Move-Objekt in String
	  */
	 @Override
	 public String toString() {
		 if (move == null)
			 return "null";

		 return move.toString();
	 }

	 /**
	  * Vergleicht zwei Move-Objekte
	  *
	  * @param o Platzhalter für CompMove
	  * @return Wahrheitswert, ob beide gleich sind
	  */
	 @Override
	 public boolean equals(Object o) {
		 Move tmpMove = ((CompMove)o).getMove();

		 if (move == null || tmpMove == null)
			 return move == null && tmpMove == null;

		 if (move.getType() != tmpMove.getType())
			 return false;

		 switch(move.getType()) {
		 case Empty:
			 return move.getType() == tmpMove.getType();
		 case Surrender:
			 return move.getType() == tmpMove.getType();
		 case Prepare:
			 return move.getPrepare().equals(tmpMove.getPrepare());
		 case Activate:
			 ArrayList<Integer> activate = move.getActivate();
			 ArrayList<Integer> tmpActivate = tmpMove.getActivate();

			 for (int i = 0; i < activate.size(); i++) {
				 if (!tmpActivate.contains(activate.get(i)))
					 return false;
			 }

			 return true;
		 case Plant:
			 ArrayList<HexagonTuple> plant = move.getPlant();
			 ArrayList<HexagonTuple> tmpPlant = tmpMove.getPlant();

			 if (plant.size() != tmpPlant.size())
				 return false;

			 for (int i = 0; i < plant.size(); i++) {
				 if (!tmpPlant.contains(plant.get(i)))
					 return false;
			 }

			 return true;
		 case Grow:
			 ArrayList<Hexagon> grow = move.getGrow();
			 ArrayList<Hexagon> tmpGrow = tmpMove.getGrow();

			 if (grow.size() != tmpGrow.size())
				 return false;

			 for (int i = 0; i < grow.size(); i++) {
				 if (!tmpGrow.contains(grow.get(i)))
					 return false;
			 }

			 return true;
		 default:
			 return true;
		 }
	 }
}

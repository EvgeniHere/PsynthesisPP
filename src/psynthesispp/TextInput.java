package psynthesispp;

import java.util.Scanner;

import psynthesispp.preset.Move;
import psynthesispp.preset.MoveFormatException;
import psynthesispp.preset.MoveType;
import psynthesispp.preset.Requestable;

/**
 * Liest Spielereingaben und verarbeitet diese
 *
 * @author Jannik
 */
public class TextInput implements Requestable {

	private Scanner scanner;

	/**
	 * Erstellt neuen Text-Scanner für die Konsole
	 */
	public TextInput() {
		scanner = new Scanner(System.in);
	}

	/**
	 * Fordert Texteingabe vom Spieler und berechnet daraus einen Move
	 *
	 * @param type Momentane Phase des Spielers
	 */
	@Override
	public Move request(MoveType type) throws Exception {
		String input = scanner.next();
		try {
			return Move.parse(input, type);
		} catch (MoveFormatException e) {
			System.err.println(e);
		}
		return null;
	}
}

package psynthesispp.player;

import java.rmi.RemoteException;

import psynthesispp.GameFrame;
import psynthesispp.preset.Move;
import psynthesispp.preset.MoveType;
import psynthesispp.preset.PlayerColor;
import psynthesispp.preset.PlayerType;

/**
 * Gibt die Anfrage auf Zug an die grafische Oberfläche weiter
 *
 * @author Moritz
 *
 */
public class HumanPlayer extends Spieler {

	GameFrame gameFrame;

	/**
	 * HumanPlayer Konstruktor
	 * erstellt Spieler des Typs Human
	 *
	 * @param boardSize Groesse des Spielfelds
	 * @param color Farbe des Spielers
	 */
	public HumanPlayer(int boardSize, PlayerColor color, GameFrame gameFrame) {
		super(boardSize, color, PlayerType.Human);
		this.gameFrame = gameFrame;
	}

	/**
	 * Fragt Zug des Spielers an
	 *
	 * @return Move Auf dem Spielbrett auszufuehrender Zug
	 */
	@Override
	public Move request() throws Exception, RemoteException {
		MoveType moveType = spielbrett.getPhaseOf(color);
		nextMove = requestHumanInput(moveType);
		return nextMove;
	}

	/**
	 * Fragt Zug ueber die Grafische oberflaeche des Spielers an
	 *
	 * @param moveType Die Phase des zu bestimmenden Zuges
	 * @return Move Gewuenschter Zug des Spielers
	 */
	private Move requestHumanInput(MoveType moveType) throws Exception {
		return gameFrame.request(moveType);
	}
}

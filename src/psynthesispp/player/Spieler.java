package psynthesispp.player;

import java.rmi.RemoteException;

import psynthesispp.Spielbrett;
import psynthesispp.TextInput;
import psynthesispp.preset.Move;
import psynthesispp.preset.MoveType;
import psynthesispp.preset.Player;
import psynthesispp.preset.PlayerColor;
import psynthesispp.preset.PlayerType;
import psynthesispp.preset.Status;
/**
 * Spieler Objekt kann erzeugt werden um eine Party zu starten
 * und Aktionen des Spielers durchzufuehren
 */
public abstract class Spieler implements Player {

	int k;
	Move nextMove;
	PlayerColor color;
	TextInput textInput;
	PlayerType playerType;
	Spielbrett spielbrett;
	PlayerColor opponentColor;
	Status playerStatus = Status.Ok;

	/**
	 * Spieler Konstruktor
	 * spieler und spielfeld werden initialisiert
	 *
	 * @param boardSize Groesse des Spielfelds
	 * @param color Farbe des Spielers
	 * @param playerType Spielertyp
	 */
	public Spieler(int boardSize, PlayerColor color, PlayerType playerType) {
		this.playerType = playerType;
		this.textInput = new TextInput();

		try {
			init(boardSize, color);
		} catch (RemoteException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Spieler wird erstellt und in Kombination mit
	 * Spielfeldgroesse kann eine Party erstellt werden
	 *
	 * @param boardSize Groesse des Spielfelds
	 * @param color Farbe des Spielers
	 */
	@Override
	public void init(int boardSize, PlayerColor color) throws Exception, RemoteException {
		this.color = color;
		this.k = boardSize;
		this.spielbrett = new Spielbrett(boardSize);
		this.opponentColor = (color == PlayerColor.Red) ? PlayerColor.Blue : PlayerColor.Red;
	}

	/**
	 * Liefert die Farbe des Spielers
	 *
	 * @return PlayerColor Die Farbe dieses Spielers
	 */
	public PlayerColor getColor() {
		return this.color;
	}

	/**
	 *Bestätigt den Nächsten Zug
	 *sollte er dem Status des Main spielbretts wiedersprechen, wird eine Exception geworfen
	 *
	 *@param boardStatus Status des Spielbretts
	 */
	@Override
	public void confirm(Status boardStatus) throws Exception, RemoteException {
		spielbrett.make(nextMove);

		playerStatus = spielbrett.getStatus();

		if (boardStatus == playerStatus && boardStatus == Status.Ok) {
			;
		} else if (boardStatus == Status.RedWin) {
			;
		} else if (boardStatus == Status.BlueWin) {
			;
		} else if (boardStatus != playerStatus) {
			throw new IllegalStateException("Status stimmt nicht mit Spielbrett ueberein");
		}
	}

	/**
	 * Updated den Zug des Gegeners auf dem Spielbrett des Spielers
	 * sollte er dem Status des Main spielbretts wiedersprechen, wird eine Exception geworfen
	 *
	 * @param opponentMove Gegnerischer Zug
	 * @param boardStatus Status des Spielbretts
	 */
	@Override
	public void update(Move opponentMove, Status boardStatus) throws Exception, RemoteException {
		spielbrett.make(opponentMove);

		playerStatus = spielbrett.getStatus();

		if (boardStatus == playerStatus && boardStatus == Status.Ok) {
			;
		} else if (boardStatus == Status.RedWin) {
			;
		} else if (boardStatus == Status.BlueWin) {
			;
		} else if (boardStatus != playerStatus) {
			throw new IllegalStateException("Status stimmt nicht mit Spielbrett ueberein");
		}
	}

	/**
	 * Prüft ob in einem Integer Array nur nullen sind
	 *
	 * @param passive Elemente eines Inventars
	 * @return boolean True, wenn Array nur 0 enthällt
	 */
	public boolean noElementsIn(int[] passive) {
		for (int i = 0; i < passive.length; i++) {
			if (passive[i] == 0)
				continue;

			return false;
		}

		return true;
	}

	/**
	 * Liefert den PlayerType des Spielers
	 *
	 * @return playerType Spielertyp dieses Spielers
	 */
	public PlayerType getType() {
		return this.playerType;
	}

	/**
	 * Fragt den nächsten Zug des Spielers an
	 *
	 * @return Move Auf dem Spielbrett auszufuehrender Zug
	 */
	@Override
	public Move request() throws Exception, RemoteException {
		MoveType moveType = spielbrett.getPhaseOf(color);

		if (moveType == MoveType.Prepare)
			return new RandomBot(k, color).request();

		return new Move(MoveType.Empty);
	}
}

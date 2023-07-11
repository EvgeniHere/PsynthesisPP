package psynthesispp.player;

import java.rmi.RemoteException;
import java.util.ArrayList;

import psynthesispp.CompMove;
import psynthesispp.GameView;
import psynthesispp.Spielbrett;
import psynthesispp.preset.Move;
import psynthesispp.preset.MoveType;
import psynthesispp.preset.PlayerColor;
import psynthesispp.preset.PlayerType;
import psynthesispp.preset.Status;

/**
 * AdvancedBot, der für jeden Zug je nach schwierigkeit des Bots züge probiert
 * und für diese mehrere Spiele spielt und so entscheidet, wenn er mehr gewinnt oder verliert,
 * ob der zug gut oder schlecht ist
 *
 * @author Moritz
 *
 */
public class AdvancedBot extends Spieler {

	private Spieler simPlayer;
	private int difficulty;

	/**
	 * AdvancedBot Konstruktor
	 * Spieler des typs AdvancedAI
	 * difficulty: easy, medium, hard, waitingSimulator, impossible
	 * simType: simple, random
	 *
	 * @param boardSize Groesse des Spielfeldes
	 * @param color Farbe des Spielers
	 * @param simType Spielertyp zur Simulation
	 * @param difficulty Schwierigkeitslevel
	 */
	public AdvancedBot(int boardSize, PlayerColor color, PlayerType simType, int difficulty) {
		super(boardSize, color, PlayerType.AdvancedAI);
		this.difficulty = difficulty;
		this.simPlayer = (simType == PlayerType.RandomAI) ? new RandomBot(boardSize, color) : new SimpleBot(boardSize, color);
	}

	/**
	 * Fordert Zug des AdvancedBot an und gibt diesen zurück
	 */
	@Override
	public Move request() throws Exception, RemoteException {
		MoveType moveType = spielbrett.getPhaseOf(color);
		nextMove = generateMove(moveType);
		return nextMove;
	}

	/**
	 * Simuliert für moegliche Zuege des ausgewaehlten Bots mehrere Spielverlaeufe und
	 * entscheidet sich anhand der Punktedifferenz für den besten Zug
	 *
	 * @param MoveType Die Phase des angeforderten Zuges
	 * @return Move Bester Zug, den dieser Spieler gefunden hat
	 */
	private Move generateMove(MoveType moveType) {
		if (moveType == MoveType.Prepare) {
			try {
				return super.request();
			} catch (RemoteException e) {
				e.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		Spielbrett originalSb = spielbrett.clone();

		int numStartMoves = 7 * difficulty + 7;
		int numGames = 10 * difficulty + 10;

		int bestGamePointDiff = -1000000;
		Move bestStartMove = null;

		ArrayList<CompMove> pastStartMoves = new ArrayList<>();

		/*
		 * Anzahl der zur Auswahl stehenden Züge, die ausgeführt werden
		 */
		for (int i = 0; i < numStartMoves; i++) {
			int curGamePointDiff = 0;

			simPlayer.spielbrett = originalSb.clone();

			Move curStartMove = (i == 0) ? new Move(MoveType.Empty) : requestSimMove();

			CompMove curStartCompMove = new CompMove(curStartMove);

			if (pastStartMoves.contains(curStartCompMove))
				continue;

			pastStartMoves.add(curStartCompMove);

			/*
			 * für jeden der obigen Züge werdem "numGames" mal spiele simuliert, und deren Endergebnisse verglichen
			 * dann wird der Zug mit der höchsten gewinnquote gewählt
			 */
			for (int j = 0; j < numGames; j++) {
				simPlayer.spielbrett = originalSb.clone();

				Move curMove = curStartMove;
				Status simStatus;

				while (true) {
					simPlayer.spielbrett.make(curMove);

					simStatus = simPlayer.spielbrett.getStatus();

					if (simStatus == Status.RedWin || simStatus == Status.BlueWin)
						break;

					curMove = (Math.random() < 0.05) ? new Move(MoveType.Empty) : requestSimMove();
				}

				GameView endGameView = (GameView)simPlayer.spielbrett.viewer();

				int points1 = endGameView.getInventoryOf(color).getPoints();
				int points2 = endGameView.getInventoryOf(opponentColor).getPoints();

				// addieren der Punktedifferenz jedes Spieldurchlaufs
				curGamePointDiff += points1 - points2;
			}

			// wenn gesammtpunktedifferenz besser, dann neuer bester Zug
			if (curGamePointDiff >= bestGamePointDiff) {
				bestGamePointDiff = curGamePointDiff;
				bestStartMove = curStartMove;
			}
		}

		if (bestStartMove == null)
			return new Move(MoveType.Empty);

		simPlayer.spielbrett = originalSb.clone();
		simPlayer.color = spielbrett.getTurnColor();

		return bestStartMove;
	}

	/**
	 * Fragt einen Move des simPlayer an
	 *
	 * @return Move Zug des Spielertyps für Simulationen
	 */
	public Move requestSimMove() {
		Move ergMove = new Move(MoveType.Empty);

		simPlayer.color = simPlayer.spielbrett.getTurnColor();

		Status testStatus = Status.Illegal;

		while (testStatus == Status.Illegal) {
			Spielbrett testSb = simPlayer.spielbrett.clone();

			try {
				ergMove = simPlayer.request();
			} catch (RemoteException e) {
				e.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			}

			testSb.make(ergMove);
			testStatus = testSb.getStatus();
		}

		if (ergMove == null)
			return new Move(MoveType.Empty);

		return ergMove;
	}
}

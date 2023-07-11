package psynthesispp;

import java.rmi.RemoteException;

import psynthesispp.player.AdvancedBot;
import psynthesispp.player.HumanPlayer;
import psynthesispp.player.RandomBot;
import psynthesispp.player.SimpleBot;
import psynthesispp.player.Spieler;
import psynthesispp.preset.ArgumentParser;
import psynthesispp.preset.ArgumentParserException;
import psynthesispp.preset.Move;
import psynthesispp.preset.PlayerColor;
import psynthesispp.preset.PlayerType;
import psynthesispp.preset.Status;

/**
 * Hauptklasse, beeinhaltet die Main-Methode
 *
 * @author Ivo
 */
public class Main {

	static int numGames = 1;
	static int curGameNum = 0;
	static GameFrame gameFrame;
	static int overallRedWins = 0;
	static int overallBlueWins = 0;

	/**
	 * Erzeugt und startet ein Spiel
	 *
	 * @param k Spielbrettgroesse
	 * @param typeRed Art des roten Spielers
	 * @param typeBlue Art des blauen Spielers
	 * @param simTypeRed Spielertyp der Simulation vom roten AdvancedBot
	 * @param simTypeBlue Spielertyp der Simulation vom blauen AdvancedBot
	 * @param delayMillis Verzögerung
	 * @param difficultyRed Schwierigkeitsgrad des roten Spielers
	 * @param difficultyBlue Schwierigkeitsgrad des blauen Spieler
	 * @return letzte Spielstand
	 */
	public static GameView startGame(int k, PlayerType typeRed, PlayerType typeBlue, PlayerType simTypeRed, PlayerType simTypeBlue, int delayMillis, int difficultyRed, int difficultyBlue) {
		Spieler player1 = null;
		Spieler player2 = null;

		switch (typeRed) {
		case Human:
			player1 = new HumanPlayer(k, PlayerColor.Red, gameFrame);
			break;
		case RandomAI:
			player1 = new RandomBot(k, PlayerColor.Red);
			break;
		case SimpleAI:
			player1 = new SimpleBot(k, PlayerColor.Red);
			break;
		case AdvancedAI:
			player1 = new AdvancedBot(k, PlayerColor.Red, simTypeRed, difficultyRed);
			break;
		default:
			System.out.println("No such Playertype found");
			System.exit(0);
		}

		switch (typeBlue) {
		case Human:
			player2 = new HumanPlayer(k, PlayerColor.Blue, gameFrame);
			break;
		case RandomAI:
			player2 = new RandomBot(k, PlayerColor.Blue);
			break;
		case SimpleAI:
			player2 = new SimpleBot(k, PlayerColor.Blue);
			break;
		case AdvancedAI:
			player2 = new AdvancedBot(k, PlayerColor.Blue, simTypeBlue, difficultyBlue);
			break;
		default:
			System.out.println("No such Playertype found");
			System.exit(0);
		}

		Spieler curPlayer = player1;
		Spieler otherPlayer = player2;

		Spielbrett sb = new Spielbrett(k);

		GameView viewer = (GameView)sb.viewer();
		Status status = Status.Ok;
		Move curMove = null;

		gameFrame.updateViewer(viewer);

		while(true) {
			viewer = (GameView)sb.viewer();
			gameFrame.updateViewer(viewer);

			if (viewer.getStatus() == Status.RedWin || viewer.getStatus() == Status.BlueWin)
				break;

			PlayerColor curColor = viewer.getTurnColor();
			curPlayer = (curColor == PlayerColor.Red) ? player1 : player2;
			otherPlayer = (curColor == PlayerColor.Red) ? player2 : player1;

			if (status == Status.Ok && curPlayer.getType() != PlayerType.Human)
				delay(delayMillis);

			try {
				curMove = curPlayer.request();
			} catch (RemoteException e) {
				e.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			}

			viewer = (GameView)sb.viewer();
			gameFrame.updateViewer(viewer);

			sb.make(curMove);

			status = sb.getStatus();

			try {
				curPlayer.confirm(status);
			} catch (RemoteException e) {
				e.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			}

			try {
				otherPlayer.update(curMove, status);
			} catch (RemoteException e) {
				e.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		return viewer;
	}

	/**
	 * Uebergibt die Aufrufparameter dem ArgumentParser, erhält deren Werte, ruft damit startGame auf, oeffnet ggf. das Ergebnisfenster und aktualisiert dieses
	 *
	 * @param args Aufrufparameter
	 */
	public static void main(String[] args) {
		ArgumentParser parser = null;

		try {
			parser = new ArgumentParser(args);
		} catch (ArgumentParserException e) {
			e.printStackTrace();
			System.exit(0);
		}

		int k = 0;
		PlayerType typeRed = null;
		PlayerType typeBlue = null;
		PlayerType simTypeRed = null;
		PlayerType simTypeBlue = null;
		int delayMillis = 0;
		int gamesCounter = 1;
		int diffRed = 0;
		int diffBlue = 0;

		try {
			k = parser.getSize();
			typeRed = parser.getRed();
			typeBlue = parser.getBlue();

			if (typeRed != PlayerType.Human || typeBlue != PlayerType.Human)
				delayMillis = parser.getDelay();

			gamesCounter = parser.getGamesCounter();

			diffRed = parser.getDifficultyRed();
			simTypeRed = parser.getSimRed();

			diffBlue = parser.getDifficultyBlue();
			simTypeBlue = parser.getSimBlue();
		} catch (ArgumentParserException e) {
			e.printStackTrace();
			System.exit(0);
		}

		if (gamesCounter <= 0) {
			System.out.println("Zu wenige Spiele ausgewaehlt!");
			return;
		}

		numGames = gamesCounter;

		int redWins = 0;
		int blueWins = 0;

		int[] redPoints = new int[gamesCounter];
		int[] bluePoints = new int[gamesCounter];

		int[] redEnergy = new int[gamesCounter];
		int[] blueEnergy = new int[gamesCounter];

		gameFrame = new GameFrame();

		ResultFrame resultFrame = null;

		if (gamesCounter > 1) {
			String result = "";

			result += "Rot gewinnt " + redWins + " mal!\n";
			result += "Blau gewinnt " + blueWins + " mal!\n\n";

			result += "Runde:\t";
			result += "\n\n";

			result += "Punkte:\n";
			result += "Rot:\t";
			result += "\n";
			result += "Blau:\t";
			result += "\n\n";

			result += "Energie:\n";
			result += "Rot:\t";
			result += "\n";
			result += "Blau:\t";

			resultFrame = new ResultFrame(result);
		}

		for (int i = 0; i < gamesCounter; i++) {
			curGameNum = i + 1;
			GameView endGameView = startGame(k, typeRed, typeBlue, simTypeRed, simTypeBlue, delayMillis, diffRed, diffBlue);
			Status endGameStatus = endGameView.getStatus();

			if (endGameStatus == Status.RedWin) {
				redWins++;
				overallRedWins++;
			} else if (endGameStatus == Status.BlueWin) {
				blueWins++;
				overallBlueWins++;
			} else {
				continue;
			}

			gameFrame.updateViewer(endGameView);

			Inventory redInv = endGameView.getInventoryOf(PlayerColor.Red);
			Inventory blueInv = endGameView.getInventoryOf(PlayerColor.Blue);

			redPoints[i] = redInv.getPoints();
			bluePoints[i] = blueInv.getPoints();

			redEnergy[i] = redInv.getEnergy();
			blueEnergy[i] = blueInv.getEnergy();

			if (gamesCounter <= 1)
				continue;

			String result = "";

			result += "Rot gewinnt " + redWins + " mal!\n";
			result += "Blau gewinnt " + blueWins + " mal!\n\n";

			result += "Runde:\t";
			for (int j = 0; j < i + 1; j++) {
				result += (j + 1) + "\t";
			}
			result += "\n\n";

			result += "Punkte:\n";
			result += "Rot:\t";
			for (int j = 0; j < i + 1; j++) {
				result += redPoints[j] + "\t";
			}
			result += "\n";
			result += "Blau:\t";
			for (int j = 0; j < i + 1; j++) {
				result += bluePoints[j] + "\t";
			}
			result += "\n\n";

			result += "Energie:\n";
			result += "Rot:\t";
			for (int j = 0; j < i + 1; j++) {
				result += redEnergy[j] + "\t";
			}
			result += "\n";
			result += "Blau:\t";
			for (int j = 0; j < i + 1; j++) {
				result += blueEnergy[j] + "\t";
			}

			resultFrame.setResults(result);
		}
	}

	/**
	 * Verzögert den Hauptprozess um die angegebenen Millisekunden
	 *
	 * @param ms Millisekunden
	 */
	public static void delay(int ms) {
		try {
		    Thread.sleep(ms);
		} catch(InterruptedException ex) {
		    Thread.currentThread().interrupt();
		}
	}
}

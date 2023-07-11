package psynthesispp;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import psynthesispp.preset.Move;
import psynthesispp.preset.MoveFormatException;
import psynthesispp.preset.MoveType;
import psynthesispp.preset.PlayerColor;
import psynthesispp.preset.Requestable;

/**
 * Erzeugt grafische Benutzeroberflaeche und reagiert auf Groessenaenderung des Fensters
 *
 * @author Clemens
 *
 */
public class GameFrame extends JFrame {

	private static final long serialVersionUID = 1L;
	private GamePanel gamePanel;

	private JLabel phaseLabel;
	private JTextArea text1;
	private JTextArea text2;
	private JTextField textInput;

	private static int frameWidth = 600;
	private static int frameHeight = 700;

	private boolean surrender;

	/**
	 * Erzeugt ein Fenster und ordnet Panels an
	 */
	public GameFrame() {
		setTitle("Psynthesis");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		Font font1 = new Font("SansSerif", Font.BOLD, 20);

		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new BorderLayout());
		phaseLabel = new JLabel("(0/1) - Round 0: Phase: ");
		phaseLabel.setFont(font1);

		text1 = new JTextArea();
		text2 = new JTextArea();

		text1.setEditable(false);
		text2.setEditable(false);

		JPanel holdPanel = new JPanel();
		holdPanel.setLayout(new BorderLayout());

		textInput = new JTextField(0);
		textInput.setFont(font1);
		textInput.addActionListener(new ActionListener(){

        @Override
		public void actionPerformed(ActionEvent e){
        	gamePanel.enterPressed = true;
        }});

		gamePanel = new GamePanel(frameWidth, frameHeight);

		holdPanel.add(text1, BorderLayout.WEST);
		holdPanel.add(text2, BorderLayout.EAST);
		holdPanel.add(gamePanel, BorderLayout.SOUTH);
		holdPanel.add(textInput, BorderLayout.CENTER);
		holdPanel.add(phaseLabel, BorderLayout.NORTH);

		mainPanel.add(holdPanel, BorderLayout.NORTH);
		add(mainPanel);

		addComponentListener(new ComponentAdapter() {
			@Override
			public void componentResized(ComponentEvent evt) {
				frameWidth = getWidth();
				frameHeight = getHeight();
			    gamePanel.setPreferredSize(new Dimension(frameWidth, frameHeight));

				if (gamePanel.gamefield != null)
					gamePanel.update();
			}
		});

		Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run () {
            	frameWidth = getWidth();
				frameHeight = getHeight();
			    gamePanel.setPreferredSize(new Dimension(frameWidth, frameHeight));

				if (gamePanel.gamefield != null)
					gamePanel.update();
            }
        }, 1000, 1000);

		setPreferredSize(new Dimension(frameWidth, frameHeight));

		pack();
		setVisible(true);
	}

	/**
	 * Prueft, ob aufgegeben wurde und leitet request-Anfrage an GamePanel weiter.
	 * Falls nicht aufgegeben wurde, wird der Zug zurueckgegeben
	 *
	 * @param type Momentane Phase des Spielers
	 * @throws Exception Input kann nicht geparsed werden
	 * @return Rueckgabe des vom Spieler gewaehlten Zuges bei Nicht-Aufgabe
	 */
	public Move request(MoveType type) throws Exception {
		Move selMove = gamePanel.request(type);

		if (!surrender)
			return selMove;

		surrender = false;
		return new Move(MoveType.Surrender);
	}

	/**
	 * Ruft Aktualisierung der Darstellung des GamePanels auf
	 *
	 * @param viewer Read-Only Darstellung des Spielbretts
	 */
	public void updateViewer(GameView viewer) {
		if (gamePanel == null)
			return;

		gamePanel.updateViewer(viewer);
	}

	/**
	 * Erstellt die Darstellung des Spielbretts und achtet auf Inputs
	 *
	 * @author Clemens
	 */
	public class GamePanel extends JPanel implements Requestable, MouseMotionListener, MouseListener, KeyListener {

		private static final long serialVersionUID = 1L;
		private ArrayList<Point> selectedHexagons;
		private boolean enterPressed = false;
		private Point hoveredHexagonPosition;
		private GameUnit[][] gamefield;
		private Polygon[][] hexagons;
		private int[][] sunLocations;
		private Point center;
		private int radius;
		private int sunPos;
		private int k;

		/**
		 * Setzt die Groesse des GamePanels und fuegt die InputListener hinzu
		 *
		 * @param width Breite des GamePanels
		 * @param height Hoehe des GamePanels
		 */
		public GamePanel(int width, int height) {
		    this.setPreferredSize(new Dimension(width, height));
		    addMouseListener(this);
		    addMouseMotionListener(this);
		    addKeyListener(this);
		    selectedHexagons = new ArrayList<>();
		}

		/**
		 * Passt die relativen Werte der Fenstergroesse an und zeichnet das Spielbrett neu
		 */
		public void update() {
			radius = getRadius();
			center = new Point(getWidth() / 2, getHeight() / 2 - text1.getHeight());
			setupSunLocations();
			setupHexagonfield();
			repaint();
		}

		/**
		 * Aktualisiert die Texte und bezieht neue Werte aus dem Viewer
		 *
		 * @param viewer Read-Only Darstellung des Spielbretts
		 */
		public void updateViewer(GameView viewer) {
			phaseLabel.setText("(" + Main.curGameNum + "/" + Main.numGames + ") - Round " + viewer.getRound() + ": " + viewer.getTurnColor() + ", " + viewer.getPhaseOf(viewer.getTurnColor()));
			text1.setText("Rot:\n" + "Wins: " + Main.overallRedWins + "\n" + viewer.getInventoryOf(PlayerColor.Red).toString());
			text2.setText("Blau:\n" + "Wins: " + Main.overallBlueWins + "\n" + viewer.getInventoryOf(PlayerColor.Blue).toString());
			gamefield = viewer.getField();
			sunPos = viewer.getSunPos();
			k = viewer.getSize();
			//requestFocus();
			update();
		}

		/**
		 * Wartet auf eine Eingabe des Spielers
		 */
		public void waitForInput() {
			while(!enterPressed && !surrender) {
				System.out.print("");
			}

			enterPressed = false;
		}

		/**
		 * Wartet auf Spielereingabe, wandelt Eingabe in Move um und gibt diesen zurueck
		 *
		 * @param type Momentane Phase des Spielers
		 * @throws Exception Input kann nicht geparsed werden
		 * @return Vom Spieler gewuenschter Zug
		 */
		@Override
		public Move request(MoveType type) throws Exception {
			requestFocus();

			String input = "";
			switch(type) {
			case Prepare:
				waitForInput();

				if (selectedHexagons.size() <= 0)
					return null;

				if (selectedHexagons.size() > 1) {
					selectedHexagons.clear();
					return null;
				}

				int y = selectedHexagons.get(0).y;
				int x = selectedHexagons.get(0).x;

				input += "(" + x + "," + y + ")";
				break;
			case Activate:
				waitForInput();

				input = textInput.getText();

				if (input.length() == 0)
					return new Move(MoveType.Empty);
				break;
			case Plant:
				waitForInput();

				if (selectedHexagons.size() <= 0)
					return new Move(MoveType.Empty);

				if (selectedHexagons.size() % 2 == 1)
					return null;

				for (int i = 0; i + 1 < selectedHexagons.size(); i += 2) {
					int y1 = selectedHexagons.get(i).y;
					int x1 = selectedHexagons.get(i).x;
					int y2 = selectedHexagons.get(i+1).y;
					int x2 = selectedHexagons.get(i+1).x;
					input += "[(" + x1 + "," + y1 + "),(" + x2 + "," + y2 + ")]+";
				}

				input = input.substring(0, input.length() - 1);
				break;
			case Grow:
				waitForInput();

				if (selectedHexagons.size() <= 0)
					return new Move(MoveType.Empty);

				for (int i = 0; i < selectedHexagons.size(); i++) {
					y = selectedHexagons.get(i).y;
					x = selectedHexagons.get(i).x;
					input += "(" + x + "," + y + ")+";
				}

				input = input.substring(0, input.length() - 1);
				break;
			default:
				break;
			}

			selectedHexagons.clear();
			textInput.setText("");

			try {
				return Move.parse(input, type);
			} catch (MoveFormatException e) {
				throw new IllegalStateException("Input kann nicht geparsed werden!");
			}
		}

		/**
		 * Berechnet anhand der Fenstergroesse den Radius eines Hexagons
		 *
		 * @return Radius eines Hexagons
		 */
		public int getRadius() {
			int minFrameSize = Math.min(GameFrame.frameWidth, GameFrame.frameHeight);
			int hexagonSize = minFrameSize / (gamefield.length + k + 2);
			int radius = hexagonSize / 2;
			return radius;
		}

		/**
		 * Erstellt ein Polygon anhand der Position und dem Radius
		 *
		 * @param top Koordinaten der Position Oben
		 * @param radius Radius eines Hexagons
		 * @return Polygon
		 */
		public Polygon createHexagon(Point top, int radius) {
			int x = top.x;
			int y = top.y;

			int[] cx = new int[] {x,
							x + radius,
							x + radius,
							x,
							x - radius,
							x - radius};

			int[] cy = new int[] {y,
							y + radius / 2,
							y + (3 * radius) / 2,
							y + 2 * radius,
							y + (3 * radius) / 2,
							y + radius / 2};

			return new Polygon(cx, cy, 6);
		}

		/**
		 * Berechnet die Sonnen-Positionen
		 */
		public void setupSunLocations() {
			int x1 = (int)((k + 1.5) * radius * 2);
			int x2 = (k + 1) * radius;
			int y1 = k * radius * 2 + radius;

			sunLocations = new int[][] {
				{center.x - x2, center.y + y1},
				{center.x - x1, center.y},
				{center.x - x2, center.y - y1},
				{center.x + x2, center.y - y1},
				{center.x + x1, center.y},
				{center.x + x2, center.y + y1}
			};
		}

		/**
		 * Erstellt das Hexagon-Feld
		 */
		public void setupHexagonfield() {
			int n = gamefield.length;
			hexagons = new Polygon[gamefield.length][gamefield[0].length];

			for (int i = 0; i < gamefield.length; i++) {
				int convI = n - i - 1;
				int curDistance = (k - (convI)) * radius;
				for (int j = 0; j < gamefield[i].length; j++) {
					if (gamefield[convI][j] == null)
						continue;

					int hexX = (j - gamefield[i].length / 2) * 2 * radius + curDistance + center.x;
					int hexY = (int) ((i - gamefield.length / 2) * 1.5 * radius) + center.y - radius;

					hexagons[convI][j] = createHexagon(new Point(hexX, hexY), radius);
				}
			}
		}

		/**
		 * Zeichnet Hexagon mit Inhalt
		 *
		 * @param g Grafische Referenz
		 * @param hexagon Polygon in Hexagon-Form
		 * @param gameUnit Spielbretteinheit
		 */
		public void drawHexagon(Graphics g, Polygon hexagon, GameUnit gameUnit) {
			if (hexagon == null)
				return;

			if (gameUnit == null) {
				g.setColor(Color.black);
				g.drawPolygon(hexagon);
				return;
			}

			if (gameUnit.isTree()) {
				Color curColor = getTreeColor(gameUnit.getSize());
				g.setColor(curColor);
				g.fillPolygon(hexagon);

				int x = hexagon.xpoints[0];
				int y = hexagon.ypoints[0];

				PlayerColor color = gameUnit.getPlayerOwner();
				g.setColor((color == PlayerColor.Red) ? Color.red : Color.blue);
				g.fillOval(x - radius / 4, y + (3 * radius) / 4, radius / 2, radius / 2);
				g.setColor(Color.black);
				g.drawOval(x - radius / 4, y + (3 * radius) / 4, radius / 2, radius / 2);
			} else {
				g.setColor(Color.white);
				g.fillPolygon(hexagon);
			}

			g.setColor(Color.black);
			g.drawPolygon(hexagon);
		}

		/**
		 * Zeichnet das Hexagon-Feld
		 *
		 * @param g Grafische Referenz
		 */
		public void drawHexagonfield(Graphics g) {
			for (int i = 0; i < hexagons.length; i++) {
				for (int j = 0; j < hexagons[i].length; j++) {
					if (hexagons[i][j] == null)
						continue;

					drawHexagon(g, hexagons[i][j], gamefield[i][j]);
				}
			}
		}

		/**
		 * Zeichnet die Sonne
		 *
		 * @param g Grafische Referenz
		 */
		public void drawSun(Graphics g) {
			Point sunPoint = new Point(sunLocations[sunPos][0], sunLocations[sunPos][1]);

			int sunWidth = radius * 2;

			g.setColor(Color.yellow);
			g.fillOval(sunPoint.x - sunWidth / 2, sunPoint.y - sunWidth / 2, sunWidth, sunWidth);
			g.setColor(Color.black);
			g.drawOval(sunPoint.x - sunWidth / 2, sunPoint.y - sunWidth / 2, sunWidth, sunWidth);
		}

		/**
		 * Berechnet die Baumfarbe anhand der Baumgroesse
		 *
		 * @param treeSize Baumgroesse
		 * @return Farbe des Baumes
		 */
		public Color getTreeColor(int treeSize) {
			int red = (int)map(treeSize, 0, k, 0, 255);
			int green = (int)map(k - treeSize, 0, k, 0, 255);
			return new Color(red, green, 0);
		}

		/**
		 * Mappt eine Zahl von einen Zahlenbereich auf einen anderen
		 *
		 * @param value Wert, der gemappt / umberechnet werden soll
		 * @param from1 minimaler Wert von value
		 * @param to1 maximaler Wert von value
		 * @param from2	minimaler Wert des Ziels
		 * @param to2 maximaler Wert des Ziels
		 * @return umberechneten Wert
		 */
		public double map(double value, double from1, double to1, double from2, double to2) {
			return (value - from1) / (to1 - from1) * (to2 - from2) + from2;
		}

		/**
		 * Zeichnet Rahmen mit variabler Rahmenbreite um Hexagon
		 *
		 * @param g Grafische Referenz
		 * @param hexPos Indizes des Hexagons
		 * @param color Farbe des Rahmens
		 * @param strokeFactor Breite des Rahmens
		 */
		public void drawHexagonRahmen(Graphics g, Point hexPos, Color color, int strokeFactor) {
			if (hexPos == null)
				return;

			Polygon hexagon = hexagons[hexPos.x][hexPos.y];

			if (hexagon == null)
				return;

			Graphics2D g2 = (Graphics2D) g;
			g2.setColor(color);
			g2.setStroke(new BasicStroke((radius * strokeFactor) / 20));
			g2.drawPolygon(hexagon);
			g2.setColor(Color.black);
			g2.setStroke(new BasicStroke(1));
		}

		/**
		 * Zeichenfunktion mit sequenziellen Funktionsaufrufen
		 *
		 * @param g Grafische Referenz
		 */
		@Override
		protected void paintComponent(Graphics g) {
			if (gamefield == null)
				return;

			super.paintComponent(g);

			drawHexagonfield(g);

			drawHexagonRahmen(g, hoveredHexagonPosition, Color.lightGray, 3);

			for (int i = 0; i < selectedHexagons.size(); i++) {
				drawHexagonRahmen(g, selectedHexagons.get(i), Color.gray, 4);
			}

			drawSun(g);
		}

		/**
		 * Bei Mausclick
		 */
		@Override
		public void mouseClicked(MouseEvent e) {

		}

		/**
		 * Ermittelt bei Mausdruck das ausgewaehlte Hexagon und fuegt es den bisher ausgewaehlten Hexagons hinzu bzw. entfernt es aus diesen
		 */
		@Override
		public void mousePressed(MouseEvent e) {
			int mx = e.getX();
			int my = e.getY();
			Point mousePoint = new Point(mx, my);

			boolean inField = false;

			for (int i = 0; i < hexagons.length; i++) {
				for (int j = 0; j < hexagons[i].length; j++) {
					Polygon curHex = hexagons[i][j];
					if (curHex == null)
						continue;

					if (!curHex.contains(mousePoint))
						continue;

					inField = true;

					Point curHexPoint = new Point(i, j);

					if (selectedHexagons.contains(curHexPoint)) {
						selectedHexagons.remove(curHexPoint);
						continue;
					}

					selectedHexagons.add(curHexPoint);
				}
			}

			if (!inField)
				selectedHexagons.clear();

			repaint();
		}

		/**
		 * Wenn die Maus losgelassen wird
		 */
		@Override
		public void mouseReleased(MouseEvent e) {

		}

		/**
		 * Wenn der Mauszeiger den Fensterbereich betritt
		 */
		@Override
		public void mouseEntered(MouseEvent e) {

		}

		/**
		 * Wenn der Mauszeiger den Fensterbereich verlaesst
		 */
		@Override
		public void mouseExited(MouseEvent e) {

		}

		/**
		 * Wenn die Maus gedrückt und zeitgleich bewegt wird
		 */
		@Override
		public void mouseDragged(MouseEvent e) {

		}

		/**
		 * Ermittelt das Hexagon, über dem der Mauszeiger ist, und markiert es als spezielles Hexagon
		 */
		@Override
		public void mouseMoved(MouseEvent e) {
			int mx = e.getX();
			int my = e.getY();
			Point mousePoint = new Point(mx, my);

			if (hexagons == null)
				return;

			boolean inField = false;

			for (int i = 0; i < hexagons.length; i++) {
				for (int j = 0; j < hexagons[i].length; j++) {
					Polygon curHex = hexagons[i][j];
					if (curHex == null)
						continue;

					if (!curHex.contains(mousePoint))
						continue;

					hoveredHexagonPosition = new Point(i, j);
					inField = true;
				}
			}

			if (!inField)
				hoveredHexagonPosition = null;

			repaint();
		}

		/**
		 * Wenn ein Tastatur-Input eingegeben wurde
		 */
		@Override
		public void keyTyped(KeyEvent e) {

		}

		/**
		 * Ueberprueft, ob gedrueckte Taste ESC oder ENTER ist und aktualisiert Variablen
		 */
		@Override
		public void keyPressed(KeyEvent e) {
			if (e.getKeyCode() == KeyEvent.VK_ENTER)
				enterPressed = true;

			if (e.getKeyCode() == KeyEvent.VK_ESCAPE)
				surrender = true;
		}

		/**
		 * Wenn eine Taste losgelassen wird
		 */
		@Override
		public void keyReleased(KeyEvent e) {

		}
	}
}

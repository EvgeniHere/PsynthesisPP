package psynthesispp;

import java.awt.Dimension;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

/**
 * Fenster fuer Ergebnisse des Turniers
 *
 * @author Ivo
 */
public class ResultFrame extends JFrame {

	private static final long serialVersionUID = 1L;

	private static int frameWidth = 550;
	private static int frameHeight = 250;

	private JTextArea textBox;

	/**
	 * Fuellt das Fenster mit einer Textbox, die Spieldaten vergangener Spiele enthaelt
	 *
	 * @param results Spieldaten der vergangenen Spiele
	 */
	public ResultFrame(String results) {
		setTitle("Psynthesis");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		textBox = new JTextArea();

		textBox.setSize(frameWidth, frameHeight);

		textBox.setText(results);

		JScrollPane scroll = new JScrollPane(textBox);

		add(scroll);

		setPreferredSize(new Dimension(frameWidth, frameHeight));

		pack();
		setVisible(true);
	}

	/**
	 * Aktualisiert die Daten der Textbox
	 *
	 * @param results Spieldaten vergangener Spiele
	 */
	public void setResults(String results) {
		textBox.setText(results);
	}
}

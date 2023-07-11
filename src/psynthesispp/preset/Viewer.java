package psynthesispp.preset;

import psynthesispp.GameUnit;
import psynthesispp.Inventory;

public interface Viewer {
    public int getSize();
	public int getRound();
	public int getSunPos();
    public Status getStatus();
	public boolean isGameOver();
	public PlayerColor getTurnColor();
	public int getSunRevolutions();
    public GameUnit[][] getField();
	public boolean[][] getUsedField();
	public int[] getNumTreesCompleted();
    public MoveType getPhaseOf(PlayerColor playerColor);
    public Inventory getInventoryOf(PlayerColor playerColor);

	//public void printInventory(PlayerColor playerColor);
}

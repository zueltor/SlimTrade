package main.java.com.slimtrade.gui.stash.helper;

import java.awt.Color;
import java.awt.Dimension;

import javax.swing.BorderFactory;

import main.java.com.slimtrade.core.managers.OLD_ColorManager;
import main.java.com.slimtrade.enums.StashTabType;
import main.java.com.slimtrade.gui.basic.BasicDialog;

public class ItemHighlighter extends BasicDialog{

	private static final long serialVersionUID = 1L;
	//Static
	private static int gridX;
	private static int gridY;
	private static double gridWidth = 0;
	private static double gridHeight = 0;
	//Internal
	private int stashX;
	private int stashY;
	
	private StashTabType type;

	public ItemHighlighter(StashTabType type, int stashX, int stashY, Color color){
		this.type = type;
		this.stashX = stashX;
		this.stashY = stashY;
		this.setVisible(false);
		this.setBackground(OLD_ColorManager.CLEAR);
		this.getRootPane().setBorder(BorderFactory.createLineBorder(color, 4, false));
		this.setBounds(0-(int)gridWidth*2, 0, (int)gridWidth, (int)gridHeight);
		this.setSize(new Dimension((int)gridWidth, (int)gridHeight));
		this.setVisible(false);
	}
	
	public static void saveGridInfo(int gridX, int gridY, int gridWidth, int gridHeight){
		ItemHighlighter.gridX = gridX;
		ItemHighlighter.gridY = gridY;
		ItemHighlighter.gridWidth = gridWidth;
		ItemHighlighter.gridHeight = gridHeight;
	}
	
	public void updatePos(int cellCount){
		double cellWidth = gridWidth/(double)cellCount;
		double cellHeight = gridHeight/(double)cellCount;
		
		if(type == StashTabType.QUAD){
			cellWidth = cellWidth/2;
			cellHeight = cellHeight/2;
		}
		
		this.setBounds((int)(gridX+((stashX-1)*cellWidth)), (int)(gridY+((stashY-1)*cellHeight)), (int)cellWidth, (int)cellHeight);
	}
	
	public void destroy(){
		this.dispose();
	}
	
}

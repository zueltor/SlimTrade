package com.slimtrade.gui.messaging;

import java.awt.Color;

import javax.swing.BorderFactory;

import com.slimtrade.core.managers.ColorManager;

public class NameClickPanel extends ClickPanel {

	private static final long serialVersionUID = 1L;

	public NameClickPanel() {
		backgroundDefault = Color.LIGHT_GRAY;
		this.setBackground(Color.LIGHT_GRAY);
		backgroundHover = ColorManager.LOW_CONTRAST_1;
		textDefault = Color.BLACK;
		textHover = Color.BLACK;
		borderDefault = BorderFactory.createEmptyBorder(1, 1, 1, 1);
		borderHover = BorderFactory.createMatteBorder(1, 1, 1, 1, Color.BLACK);
	}

}

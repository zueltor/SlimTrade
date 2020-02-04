package com.slimtrade.gui.options.general;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import com.slimtrade.App;
import com.slimtrade.core.managers.ColorManager;
import com.slimtrade.core.observing.REDO_MacroEventManager;
import com.slimtrade.core.observing.improved.ColorUpdateListener;
import com.slimtrade.enums.ColorTheme;
import com.slimtrade.gui.FrameManager;
import com.slimtrade.gui.buttons.BasicButton;
import com.slimtrade.gui.options.ISaveable;
import com.slimtrade.gui.panels.BufferPanel;
import com.slimtrade.gui.panels.ContainerPanel;

public class BasicsPanel extends ContainerPanel implements ISaveable, ColorUpdateListener {

	private static final long serialVersionUID = 1L;

	private JTextField characterInput = new JTextField();
	private JCheckBox guildCheckbox = new JCheckBox();
	private JCheckBox kickCheckbox = new JCheckBox();
	private JCheckBox quickPasteCheckbox = new JCheckBox();
	private JComboBox<ColorTheme> colorThemeCombo = new JComboBox<ColorTheme>();
	private JButton editStashButton = new BasicButton();
	private JButton editOverlayButton = new BasicButton();

	//Labels
	private JLabel characterLabel = new JLabel("Character Name");
	private JLabel guildLabel = new JLabel("Show Guild Name");
	private JLabel kickLabel = new JLabel("Close on Kick");
	private JLabel quickPasteLabel = new JLabel("Quick Paste Trades");
	private JLabel colorThemeLabel = new JLabel("Color Theme");
	
	public BasicsPanel() {

//		this.setBackground(Color.LIGHT_GRAY);

//        App.saveFile.characterName = new SaveElement("charName", characterInput);

		guildCheckbox.setFocusable(false);
		kickCheckbox.setFocusable(false);
		editStashButton.setFocusable(false);
		editOverlayButton.setFocusable(false);

		JPanel showGuildContainer = new JPanel(new BorderLayout());
		guildCheckbox.setOpaque(false);
		showGuildContainer.setOpaque(false);
		showGuildContainer.add(guildCheckbox, BorderLayout.EAST);

		JPanel kickContainer = new JPanel(new BorderLayout());
		kickContainer.setOpaque(false);
		kickCheckbox.setOpaque(false);
		kickContainer.add(kickCheckbox, BorderLayout.EAST);
		
		JPanel quickPasteContainer = new JPanel(new BorderLayout());
		quickPasteContainer.setOpaque(false);
		quickPasteCheckbox.setOpaque(false);
		quickPasteContainer.add(quickPasteCheckbox, BorderLayout.EAST);
		
		JPanel colorThemeContainer = new JPanel(new BorderLayout());
		colorThemeContainer.setOpaque(false);
		colorThemeCombo.setOpaque(false);
		colorThemeContainer.add(colorThemeCombo, BorderLayout.EAST);


		editStashButton.setText("Edit Stash");
		editOverlayButton.setText("Edit Overlay");
		for(ColorTheme theme : ColorTheme.values()){
			colorThemeCombo.addItem(theme);
		}

		container.setLayout(new GridBagLayout());
		GridBagConstraints gc = new GridBagConstraints();

		gc.fill = GridBagConstraints.BOTH;
		gc.gridx = 0;
		gc.gridy = 0;
		gc.insets.bottom = 5;
		gc.weightx = 1;

		// Sizing
		gc.gridx = 1;
		container.add(new BufferPanel(20, 0), gc);
		gc.gridx = 2;
		container.add(new BufferPanel(140, 0), gc);
		gc.gridx = 0;
		gc.gridy++;

		// Character
		container.add(characterLabel, gc);
		gc.gridx = 2;
		container.add(characterInput, gc);
		gc.gridwidth = 1;
		gc.gridx = 0;
		gc.gridy++;

		// show Guild
		container.add(guildLabel, gc);
		gc.gridx = 2;

		container.add(showGuildContainer, gc);
		gc.gridx = 0;
		gc.gridy++;

		// Kick on Close
		container.add(kickLabel, gc);
		gc.gridx = 2;

		container.add(kickContainer, gc);
		gc.gridx = 0;
		gc.gridy++;
		
		// Quick Paste
		container.add(quickPasteLabel, gc);
		gc.gridx = 2;
		
		container.add(quickPasteContainer, gc);
		gc.gridx = 0;
		gc.gridy++;
		
		// Color Combo
		container.add(colorThemeLabel, gc);
		gc.gridx = 2;
		
		container.add(colorThemeContainer, gc);
		gc.gridx = 0;
		gc.gridy++;

		// Edit Buttons
		gc.gridwidth = 3;
		container.add(editStashButton, gc);
		gc.gridy++;
		gc.insets.bottom = 0;
		container.add(editOverlayButton, gc);
		gc.gridy++;
		
		editStashButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				FrameManager.hideAllFrames();
				FrameManager.stashOverlayWindow.setShow(true);
			}
		});

		editOverlayButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				FrameManager.hideAllFrames();
				FrameManager.overlayManager.showDialog();
			}
		});

		App.eventManager.addListener(this);
		this.updateColor();
		load();
		
		//TEMP COLOR THEME CHANGER
		colorThemeCombo.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				App.eventManager.updateAllColors((ColorTheme)colorThemeCombo.getSelectedItem());
			}
		});
	}

	@Override
	public void save() {
		String characterName = characterInput.getText().replaceAll("\\s+", "");
		if (characterName.equals("")) {
			characterName = null;
		}
		REDO_MacroEventManager.setCharacterName(characterName);
		ColorTheme colorTheme = (ColorTheme)colorThemeCombo.getSelectedItem();
		App.saveManager.saveFile.characterName = characterName;
		App.saveManager.saveFile.showGuildName = guildCheckbox.isSelected();
		App.saveManager.saveFile.closeOnKick = kickCheckbox.isSelected();
		App.saveManager.saveFile.quickPasteTrades = guildCheckbox.isSelected();
		App.saveManager.saveFile.colorTheme = colorTheme;

	}

	@Override
	public void load() {
		String characterName = App.saveManager.saveFile.characterName;
		REDO_MacroEventManager.setCharacterName(characterName);
		characterInput.setText(characterName);
		guildCheckbox.setSelected(App.saveManager.saveFile.showGuildName);
		kickCheckbox.setSelected(App.saveManager.saveFile.closeOnKick);
		quickPasteCheckbox.setSelected(false);
		colorThemeCombo.setSelectedItem(ColorTheme.LIGHT_THEME);
	}

	@Override
	public void updateColor() {
		this.setBackground(ColorManager.BACKGROUND);
		characterLabel.setForeground(ColorManager.TEXT);
		guildLabel.setForeground(ColorManager.TEXT);
		kickLabel.setForeground(ColorManager.TEXT);
		quickPasteLabel.setForeground(ColorManager.TEXT);
		colorThemeLabel.setForeground(ColorManager.TEXT);
	}

}

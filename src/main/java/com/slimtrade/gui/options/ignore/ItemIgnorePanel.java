package com.slimtrade.gui.options.ignore;

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.*;

import com.slimtrade.App;
import com.slimtrade.gui.FrameManager;
import com.slimtrade.gui.basic.CustomCombo;
import com.slimtrade.gui.basic.CustomLabel;
import com.slimtrade.gui.basic.CustomSpinner;
import com.slimtrade.gui.basic.CustomTextField;
import com.slimtrade.gui.components.AddRemovePanel;
import com.slimtrade.gui.buttons.BasicButton;
import com.slimtrade.gui.enums.MatchType;
import com.slimtrade.gui.options.ISaveable;
import com.slimtrade.gui.panels.BufferPanel;
import com.slimtrade.gui.panels.ContainerPanel;

public class ItemIgnorePanel extends ContainerPanel implements ISaveable {

    private static final long serialVersionUID = 1L;

    private AddRemovePanel addRemovePanel = new AddRemovePanel();
    private JTextField itemText = new CustomTextField(30);
    private JComboBox<MatchType> typeCombo = new CustomCombo<>();
    private SpinnerModel spinnerModel = new SpinnerNumberModel(60, 0, 300, 10);
    private JSpinner timerSpinner = new CustomSpinner(spinnerModel);
    private JButton ignoreButton = new BasicButton("Ignore Item");

    // TODO : Impose max?
    private final int MAX_IGNORE_COUNT = 40;

    public ItemIgnorePanel() {
        this.setVisible(false);

        FrameManager.ignoreItemAddRemovePanel = addRemovePanel;

        JPanel entryPanel = new JPanel(new GridBagLayout());
        entryPanel.setOpaque(false);

        JLabel itemLabel = new CustomLabel("Item Name");
        JLabel typeLabel = new CustomLabel("Match Type");
        JLabel timerLabel = new CustomLabel("Minutes");

        for (MatchType type : MatchType.values()) {
            typeCombo.addItem(type);
        }

        container.setLayout(new GridBagLayout());
        GridBagConstraints gc = new GridBagConstraints();
        gc.gridx = 0;
        gc.gridy = 0;

        gc.insets.left = 10;
        gc.insets.right = 10;

        // Entry Panel
        gc.gridx = 1;
        entryPanel.add(new BufferPanel(200, 0), gc);
        gc.gridy++;

        entryPanel.add(itemLabel, gc);
        gc.gridx++;
        entryPanel.add(typeLabel, gc);
        gc.gridx++;
        entryPanel.add(timerLabel, gc);
        gc.gridx = 0;
        gc.gridy++;

        entryPanel.add(ignoreButton, gc);
        gc.gridx++;
        entryPanel.add(itemText, gc);
        gc.gridx++;
        entryPanel.add(typeCombo, gc);
        gc.gridx++;
        entryPanel.add(timerSpinner, gc);
        gc.gridx = 0;
        gc.gridy++;

        // Reset gc
        gc.fill = GridBagConstraints.NONE;
        gc.gridx = 0;
        gc.gridy = 0;
        gc.gridwidth = 1;
        gc.insets.left = 0;
        gc.insets.right = 0;

        // Container
        container.add(entryPanel, gc);
        gc.gridy++;
        container.add(new BufferPanel(0, 15), gc);
        gc.gridy++;
        container.add(addRemovePanel, gc);
        gc.gridy++;

        load();
        FrameManager.itemIgnorePanel = this;

        ignoreButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String text = itemText.getText().trim();
                if (text.matches("")) {
                    return;
                }
                int i = 0;
                for (Component c : addRemovePanel.getComponents()) {
                    if (c instanceof IgnoreRow) {
                        IgnoreRow row = (IgnoreRow) c;
                        if(!row.isToBeDeleted() && text.toLowerCase().matches(row.getIgnoreData().getItemName().toLowerCase())) {
                            return;
                        }
                        if(row.isVisible()){
                            i++;
                        }
                    }
                }
                if(i >= MAX_IGNORE_COUNT) {
                    return;
                }
                i = (int)timerSpinner.getValue();
                itemText.setText("");
                addRemovePanel.addRemoveablePanel(new IgnoreRow(new IgnoreData(text, (MatchType) typeCombo.getSelectedItem(), i), addRemovePanel));
            }
        });
    }

    public void revertChanges() {
        addRemovePanel.revertChanges();
    }

    @Override
    public void save() {
        addRemovePanel.saveChanges();
        App.saveManager.saveFile.ignoreData.clear();
        for (Component c : addRemovePanel.getComponents()) {
            if (c instanceof IgnoreRow) {
                IgnoreRow row = (IgnoreRow) c;
                IgnoreData rowData = row.getIgnoreData();
                if(rowData.getRemainingTime() > 0 || rowData.indefinite) {
                    App.saveManager.saveFile.ignoreData.add(rowData);
                }
            }
        }
        App.chatParser.setWhisperIgnoreTerms(App.saveManager.saveFile.ignoreData);
    }

    @Override
    public void load() {
        ArrayList<IgnoreData> fullData = new ArrayList<IgnoreData>();
        for (IgnoreData data : App.saveManager.saveFile.ignoreData) {
            if(data.getRemainingTime() > -0 || data.indefinite) {
                fullData.add(data);
                addRemovePanel.addRemoveablePanel(new IgnoreRow(data, addRemovePanel));
            }
        }
        addRemovePanel.saveChanges();
        App.chatParser.setWhisperIgnoreTerms(fullData);
    }

}

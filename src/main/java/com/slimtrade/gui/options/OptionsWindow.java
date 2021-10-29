
package com.slimtrade.gui.options;

import com.slimtrade.App;
import com.slimtrade.core.References;
import com.slimtrade.core.managers.ColorManager;
import com.slimtrade.core.managers.SaveManager;
import com.slimtrade.core.observing.AdvancedMouseAdapter;
import com.slimtrade.core.observing.IColorable;
import com.slimtrade.core.utility.TradeUtility;
import com.slimtrade.enums.MessageType;
import com.slimtrade.enums.QuickPasteSetting;
import com.slimtrade.gui.FrameManager;
import com.slimtrade.gui.basic.AbstractResizableWindow;
import com.slimtrade.gui.buttons.BasicButton;
import com.slimtrade.gui.buttons.ConfirmButton;
import com.slimtrade.gui.buttons.DenyButton;
import com.slimtrade.gui.custom.CustomLabel;
import com.slimtrade.gui.custom.CustomScrollPane;
import com.slimtrade.gui.options.cheatsheet.CheatSheetPanel;
import com.slimtrade.gui.options.general.GeneralPanel;
import com.slimtrade.gui.options.general.HistorySettingsPanel;
import com.slimtrade.gui.options.hotkeys.HotkeyPanel;
import com.slimtrade.gui.options.ignore.ItemIgnorePanel;
import com.slimtrade.gui.options.macro.MacroPanel;
import com.slimtrade.gui.options.stashsearch.StashSearchPanel;
import com.slimtrade.gui.panels.BufferPanel;
import com.slimtrade.gui.stash.StashTabPanel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;

public class OptionsWindow extends AbstractResizableWindow implements IColorable {

    private static final long serialVersionUID = 1L;
    private final JPanel display = new JPanel(new GridBagLayout());
    private final JScrollPane scrollDisplay;

    private final JPanel menuPanel = new JPanel(new GridBagLayout());
    private final JPanel menuPanelLower = new JPanel(new GridBagLayout());
    private BasicButton updateButton;
    private BasicButton donateButton;

    private GeneralPanel generalPanel;
    public MacroPanel macroPanelIncoming;
    public MacroPanel macroPanelOutgoing;

    private Component strut = Box.createVerticalStrut(10);

    public OptionsWindow() {
        super("Options", true, true);
        this.setAlwaysOnTop(false);
        this.setFocusable(true);
        this.setFocusableWindowState(true);

        container.setLayout(new BorderLayout());
        JPanel menuBorder = new JPanel(new BorderLayout());

        menuBorder.add(menuPanel, BorderLayout.NORTH);
        menuBorder.add(menuPanelLower, BorderLayout.SOUTH);

        scrollDisplay = new CustomScrollPane(display);
        scrollDisplay.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        display.setLayout(new GridBagLayout());
        GridBagConstraints gc = new GridBagConstraints();
        gc.gridx = 0;
        gc.gridy = 0;

        int buffer = 6;
        JPanel bottomPanel = new JPanel();
        bottomPanel.setOpaque(false);
        menuPanel.setOpaque(false);
        menuPanelLower.setOpaque(false);
        menuBorder.setOpaque(false);

        bottomPanel.setLayout(new FlowLayout(FlowLayout.RIGHT, 30, buffer));
        DenyButton revertButton = new DenyButton("Revert Changes");
        ConfirmButton saveButton = new ConfirmButton("Save");
        bottomPanel.add(revertButton);
        bottomPanel.add(saveButton);

        SelectorButton generalButton = new SelectorButton("General");
        generalPanel = new GeneralPanel();
        link(generalButton, generalPanel);

        SelectorButton hotKeyButton = new SelectorButton("Hotkeys");
        HotkeyPanel hotkeyPanel = new HotkeyPanel();
        link(hotKeyButton, hotkeyPanel);

        SelectorButton macroIncomingButton = new SelectorButton("Incoming Macros");
        macroPanelIncoming = new MacroPanel(MessageType.INCOMING_TRADE);
        link(macroIncomingButton, macroPanelIncoming);

        SelectorButton macroOutgoingButton = new SelectorButton("Outgoing Macros");
        macroPanelOutgoing = new MacroPanel(MessageType.OUTGOING_TRADE);
        link(macroOutgoingButton, macroPanelOutgoing);

        SelectorButton ignoreButton = new SelectorButton("Ignore Items");
        ItemIgnorePanel ignorePanel = new ItemIgnorePanel();
        link(ignoreButton, ignorePanel);

        SelectorButton stashButton = new SelectorButton("Stash Tabs");
        StashTabPanel stashPanel = new StashTabPanel();
        link(stashButton, stashPanel);

        SelectorButton stashSearcherButton = new SelectorButton("Stash Sorting");
        StashSearchPanel stashSearchPanel = new StashSearchPanel();
        link(stashSearcherButton, stashSearchPanel);

        SelectorButton cheatSheetButton = new SelectorButton("Cheat Sheets");
        CheatSheetPanel cheatSheetPanel = new CheatSheetPanel();
        link(cheatSheetButton, cheatSheetPanel);

        JButton contactButton = new SelectorButton("Information");
        InformationPanel contactPanel = new InformationPanel();
        link(contactButton, contactPanel);

        updateButton = new ConfirmButton("Install Update");
        updateButton.setVisible(false);
        strut.setVisible(false);
        donateButton = new ConfirmButton("Donate with PayPal");
        gc = new GridBagConstraints();

        gc.weightx = 1;
        gc.weighty = 1;
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.insets.bottom = 10;
        gc.insets.left = 5;
        gc.insets.right = 5;
        gc.gridx = 0;
        gc.gridy = 0;
        menuPanel.add(generalButton, gc);
        gc.gridy++;
        menuPanel.add(hotKeyButton, gc);
        gc.gridy++;
        menuPanel.add(macroIncomingButton, gc);
        gc.gridy++;
        menuPanel.add(macroOutgoingButton, gc);
        gc.gridy++;
        menuPanel.add(ignoreButton, gc);
        gc.gridy++;
        menuPanel.add(stashButton, gc);
        gc.gridy++;
        menuPanel.add(stashSearcherButton, gc);
        gc.gridy++;
        menuPanel.add(cheatSheetButton, gc);
        gc.gridy++;
        menuPanel.add(contactButton, gc);
        gc.gridy++;

        // Update Button
        gc.gridx = 0;
        gc.gridy = 0;
        gc.insets.bottom = 0;
        gc.fill = GridBagConstraints.NONE;
        menuPanelLower.add(new CustomLabel(References.APP_NAME + " " + References.getAppVersion()), gc);
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.gridy++;
        menuPanelLower.add(updateButton, gc);
        gc.gridy++;
        menuPanelLower.add(strut, gc);
        gc.gridy++;
        menuPanelLower.add(donateButton, gc);

        container.add(new BufferPanel(0, buffer), BorderLayout.NORTH);
        container.add(new BufferPanel(buffer, 0), BorderLayout.EAST);
        container.add(bottomPanel, BorderLayout.SOUTH);
        container.add(menuBorder, BorderLayout.WEST);
        container.add(scrollDisplay, BorderLayout.CENTER);

        gc = new GridBagConstraints();
        gc.gridx = 0;
        gc.gridy = 0;
        display.add(generalPanel, gc);
        gc.gridy++;
        display.add(hotkeyPanel, gc);
        gc.gridy++;
        display.add(macroPanelIncoming, gc);
        gc.gridy++;
        display.add(macroPanelOutgoing, gc);
        gc.gridy++;
        display.add(ignorePanel, gc);
        gc.gridy++;
        display.add(stashPanel, gc);
        gc.gridy++;
        display.add(stashSearchPanel, gc);
        gc.gridy++;
        display.add(cheatSheetPanel, gc);
        gc.gridy++;
        display.add(contactPanel, gc);
        for (Component c : display.getComponents()) {
            c.setVisible(false);
        }
        generalPanel.setVisible(true);
        generalButton.selected = true;
        this.setDefaultSize(new Dimension(1000, 780));

        this.refresh();
        //TODO : Resize doesn't respect maximum size
        this.setMinimumSize(new Dimension(500, 550));

        donateButton.addActionListener(e -> TradeUtility.openLink(References.PAYPAL));
        updateButton.addActionListener(e -> {
            App.updateManager.runUpdateProcess();
        });

        revertButton.addActionListener(e -> {
            SaveManager.recursiveLoad(FrameManager.optionsWindow);
            FrameManager.optionsWindow.revalidate();
            FrameManager.optionsWindow.repaint();
        });

        saveButton.addActionListener(e -> {

            // Reload history window if needed
            // This is done on a new thread as it needs to reload the chat parser
            boolean reloadHistory = false;
            HistorySettingsPanel historySettingsPanel = FrameManager.optionsWindow.generalPanel.historyPanel;
            if (App.saveManager.settingsSaveFile.timeStyle != historySettingsPanel.getTimeStyle()
                    || App.saveManager.settingsSaveFile.dateStyle != historySettingsPanel.getDateStyle()
                    || App.saveManager.settingsSaveFile.orderType != historySettingsPanel.getOrderType()
                    || App.saveManager.settingsSaveFile.historyLimit != historySettingsPanel.getMessageLimit()
                    || !App.saveManager.settingsSaveFile.clientPath.equals(FrameManager.optionsWindow.generalPanel.getClientPath())) {
                FrameManager.historyWindow.setTimeStyle(historySettingsPanel.getTimeStyle());
                FrameManager.historyWindow.setDateStyle(historySettingsPanel.getDateStyle());
                FrameManager.historyWindow.setOrderType(historySettingsPanel.getOrderType());
                reloadHistory = true;
//                FrameManager.historyWindow.(historyOptionsPanel.getDateStyle());
            }

            // Save all windows
            SaveManager.recursiveSave(FrameManager.optionsWindow);
            App.saveManager.saveSettingsToDisk();

            // Refresh messages
            FrameManager.messageManager.refreshPanelLocations();

            // Update Clipboard Listener
            App.clipboardManager.setListeningState(App.saveManager.settingsSaveFile.quickPasteSetting == QuickPasteSetting.AUTOMATIC);

            // Set menubar visibility
            FrameManager.menubarToggle.setShow(App.saveManager.settingsSaveFile.enableMenubar);

            // Update Stash Helper Container
            FrameManager.stashHelperContainer.updateLocation();

            if (reloadHistory) {
                new Thread(() -> {
                    // Restart file monitor
                    App.chatParser.init();

                }).start();
            }
        });
    }

    private void link(JButton b, JPanel p) {
        b.addMouseListener(new AdvancedMouseAdapter() {
            public void click(MouseEvent e) {
                for (Component c : display.getComponents()) {
                    c.setVisible(false);
                }
                p.setVisible(true);
            }
        });
    }

    private void hideAllWindows() {
        for (Component c : display.getComponents()) {
            c.setVisible(false);
        }
    }

    public void refresh() {
        display.revalidate();
        scrollDisplay.revalidate();
        this.pack();
        this.repaint();
    }

    public void showUpdateButton() {
        updateButton.setVisible(true);
        strut.setVisible(true);
    }

    public void reloadGeneralPanel() {
        SaveManager.recursiveLoad(generalPanel);
    }

    @Override
    public void updateColor() {
        super.updateColor();
        container.setBackground(ColorManager.BACKGROUND);
        display.setBackground(ColorManager.BACKGROUND);
        display.setBorder(ColorManager.BORDER_TEXT);
        display.setBorder(BorderFactory.createLineBorder(Color.RED));
        display.setBorder(null);
        scrollDisplay.setBorder(ColorManager.BORDER_TEXT);
    }

    @Override
    public void pinAction(MouseEvent e) {
        super.pinAction(e);
        FrameManager.saveWindowPins();
    }

}

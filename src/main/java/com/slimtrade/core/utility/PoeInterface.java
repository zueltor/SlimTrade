package com.slimtrade.core.utility;

import com.slimtrade.App;
import com.slimtrade.core.References;
import com.slimtrade.enums.LangRegex;
import com.slimtrade.enums.MessageType;
import com.slimtrade.gui.FrameManager;
import com.sun.jna.Native;
import com.sun.jna.PointerType;
import com.sun.jna.platform.win32.User32;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class PoeInterface {

    private static StringSelection pasteString;
    private static Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
    private static Robot robot;

    private static String wtbText_English = "like to buy your";
    private static String wtbText_Russian = "Здравствуйте, хочу купить у вас";
    private static String wtbText_Korean = "구매하고 싶습니다";
    private static String wtbText_Thai = "สวัสดี, เราต้องการจะชื้อของคุณ";
    private static String[] wtbTextArray;
    private static ExecutorService executor = Executors.newSingleThreadExecutor();

    private static boolean quickPasteSuccess = false;

    public static void init() {
        try {
            robot = new Robot();
        } catch (AWTException e) {
            e.printStackTrace();
        }
        wtbTextArray = new String[]{wtbText_English, wtbText_Russian, wtbText_Korean, wtbText_Thai};
    }

    public static void attemptQuickPaste() {
        String text;
        try {
            text = (String) Toolkit.getDefaultToolkit().getSystemClipboard().getData(DataFlavor.stringFlavor);
        } catch (UnsupportedFlavorException | IOException | IllegalStateException e) {
            return;
        }
        if (text == null) {
            return;
        }
        attemptQuickPaste(text);
    }

    public static void attemptQuickPaste(String text) {
        if (text == null) {
            return;
        }
        boolean valid = false;
        if (text.startsWith("@")) {
            for (LangRegex l : LangRegex.values()) {
                if (text.contains(l.CONTAINS_TEXT) && App.chatParser.validateQuickPaste(text, l)) {
                    valid = true;
                    break;
                }
            }
        }
        if (valid) {
            pasteWithFocus(text);
        }
    }

//    static volatile boolean blocker = false;

    private static void pasteWithFocus(String s) {
        new Thread(() -> {
            pasteString = new StringSelection(s);
            try {
                clipboard.setContents(pasteString, null);
            } catch (IllegalStateException ignored) {
            }
            clickForceFocusWindow();
            focus();
            int i = 0;
            while (!isPoeFocused(false)) {
                try {
                    Thread.sleep(1);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                i++;
                if (i > 20) {
                    break;
                }
            }
            if (isPoeFocused(false)) {
                robot.keyPress(KeyEvent.VK_ALT);
                robot.keyRelease(KeyEvent.VK_ALT);
                robot.keyPress(KeyEvent.VK_ENTER);
                robot.keyRelease(KeyEvent.VK_ENTER);
                robot.keyPress(KeyEvent.VK_CONTROL);
                robot.keyPress(KeyEvent.VK_V);
                robot.keyRelease(KeyEvent.VK_V);
                robot.keyRelease(KeyEvent.VK_CONTROL);
                robot.keyPress(KeyEvent.VK_ENTER);
                robot.keyRelease(KeyEvent.VK_ENTER);
            }
            SwingUtilities.invokeLater(() -> {
                FrameManager.showVisibleFrames();
                FrameManager.forceAllToTop();
            });
            quickPasteSuccess = true;
            App.globalMouse.setGameFocusedFlag(true);
        }).start();
    }

    public static void paste(String s, boolean... send) {
        pasteString = new StringSelection(s);
        try {
            clipboard.setContents(pasteString, null);
        } catch (IllegalStateException e) {
            System.out.println("[SlimTrade] Failed to read clipboard, aborting.");
            return;
        }
        focus();
        robot.keyPress(KeyEvent.VK_ALT);
        robot.keyRelease(KeyEvent.VK_ALT);
        robot.keyPress(KeyEvent.VK_ENTER);
        robot.keyRelease(KeyEvent.VK_ENTER);
        robot.keyPress(KeyEvent.VK_CONTROL);
        robot.keyPress(KeyEvent.VK_V);
        robot.keyRelease(KeyEvent.VK_V);
        robot.keyRelease(KeyEvent.VK_CONTROL);
        if (send.length == 0 || send[0]) {
            robot.keyPress(KeyEvent.VK_ENTER);
            robot.keyRelease(KeyEvent.VK_ENTER);
        }
        FrameManager.forceAllToTop();
    }

    public static void runCommand(String command) {
        runCommand(command, null);
    }

    public static void runCommand(String command, MessageType type) {
        executor.execute(() -> {
            focus();
            try {
                Thread.sleep(5);
            } catch (InterruptedException e1) {
                e1.printStackTrace();
            }
            PointerType hwnd;
            byte[] windowText = new byte[512];
            int i = 0;
            String curWindowTitle;
            do {
                hwnd = User32.INSTANCE.GetForegroundWindow();
                if (hwnd != null) {
                    User32Custom.INSTANCE.GetWindowTextA(hwnd, windowText, 512);
                    curWindowTitle = Native.toString(windowText);
                    if (curWindowTitle.equals(References.POE_WINDOW_TITLE)) {
                        break;
                    } else if (i > 400) {
                        return;
                    }
                } else {
                    try {
                        Thread.sleep(1);
                    } catch (InterruptedException e1) {
                        e1.printStackTrace();
                    }
                }
                i++;
            } while (true);
            FrameManager.forceAllToTop();
            pasteString = new StringSelection(command);
            int attempt = 1;
            int MAX_ATTEMPTS = 3;
            while (attempt <= MAX_ATTEMPTS) {
                try {
                    clipboard.setContents(pasteString, null);
                    break;
                } catch (IllegalStateException e) {
                    App.debugger.log("Retrying clipboard...");
                    if (attempt == MAX_ATTEMPTS) {
                        App.debugger.log("Failed to get clipboard contents.");
                        return;
                    }
                }
                attempt++;
            }
            robot.keyPress(KeyEvent.VK_ALT);
            robot.keyRelease(KeyEvent.VK_ALT);
            robot.keyPress(KeyEvent.VK_ENTER);
            robot.keyRelease(KeyEvent.VK_ENTER);
            robot.keyPress(KeyEvent.VK_CONTROL);
            robot.keyPress(KeyEvent.VK_V);
            robot.keyRelease(KeyEvent.VK_V);
            robot.keyRelease(KeyEvent.VK_CONTROL);
            robot.keyPress(KeyEvent.VK_ENTER);
            robot.keyRelease(KeyEvent.VK_ENTER);
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
    }

    public static void runCommand(String[] commands, String player, String item, String price, String fullMessage, MessageType messageType) {
        if (commands.length == 0) {
            return;
        }
        executor.execute(() -> {
            focus();
            try {
                Thread.sleep(5);
            } catch (InterruptedException e1) {
                e1.printStackTrace();
            }
            PointerType hwnd;
            byte[] windowText = new byte[512];
            int i = 0;
            String curWindowTitle;
            do {
                hwnd = User32.INSTANCE.GetForegroundWindow();
                if (hwnd != null) {
                    User32Custom.INSTANCE.GetWindowTextA(hwnd, windowText, 512);
                    curWindowTitle = Native.toString(windowText);
                    if (curWindowTitle.equals(References.POE_WINDOW_TITLE)) {
                        break;
                    } else if (i > 400) {
                        return;
                    }
                } else {
                    try {
                        Thread.sleep(1);
                    } catch (InterruptedException e1) {
                        e1.printStackTrace();
                    }
                }
                i++;
            } while (true);
            FrameManager.forceAllToTop();
            for (String cmd : commands) {
                cmd = cmd.replaceAll("\\{self\\}", App.saveManager.settingsSaveFile.characterName);
                cmd = cmd.replaceAll("\\{player\\}", player);
                cmd = cmd.replaceAll("\\{item\\}", item);
                cmd = cmd.replaceAll("\\{price\\}", price);
                cmd = cmd.replaceAll("\\{message\\}", fullMessage);
                cmd = cmd.replaceAll("\\{zone\\}", App.chatParser.getZone());
                pasteString = new StringSelection(cmd);
                int attempt = 1;
                int MAX_ATTEMPTS = 3;
                while (attempt <= MAX_ATTEMPTS) {
                    try {
                        clipboard.setContents(pasteString, null);
                        break;
                    } catch (IllegalStateException e) {
                        App.debugger.log("Retrying clipboard...");
                        if (attempt == MAX_ATTEMPTS) {
                            App.debugger.log("Aborting clipboard...");
                            return;
                        }
                    }
                    attempt++;
                }
                if (cmd.contains("/invite")) {
                    FrameManager.messageManager.showStashHelper(fullMessage, messageType);
                }
                robot.keyPress(KeyEvent.VK_ALT);
                robot.keyRelease(KeyEvent.VK_ALT);
                robot.keyPress(KeyEvent.VK_ENTER);
                robot.keyRelease(KeyEvent.VK_ENTER);
                robot.keyPress(KeyEvent.VK_CONTROL);
                robot.keyPress(KeyEvent.VK_V);
                robot.keyRelease(KeyEvent.VK_V);
                robot.keyRelease(KeyEvent.VK_CONTROL);
                robot.keyPress(KeyEvent.VK_ENTER);
                robot.keyRelease(KeyEvent.VK_ENTER);
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    App.debugger.log("Error while running command...");
                    App.debugger.log(e.getStackTrace());
                }
            }
        });
    }

    public static void findInStash(String s) {
        executor.execute(() -> {
            focus();
            if (!isPoeFocused(false)) {
                return;
            }
            pasteString = new StringSelection(s);
            clipboard.setContents(pasteString, null);
            robot.keyPress(KeyEvent.VK_BACK_SPACE);
            robot.keyRelease(KeyEvent.VK_BACK_SPACE);
            robot.keyPress(KeyEvent.VK_CONTROL);
            robot.keyPress(KeyEvent.VK_F);
            robot.keyRelease(KeyEvent.VK_F);
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            robot.keyPress(KeyEvent.VK_V);
            robot.keyRelease(KeyEvent.VK_V);
            robot.keyRelease(KeyEvent.VK_CONTROL);
            robot.keyPress(KeyEvent.VK_ENTER);
            robot.keyRelease(KeyEvent.VK_ENTER);
            SwingUtilities.invokeLater(FrameManager::forceAllToTop);
        });

    }

    public static void clickForceFocusWindow() {
        try {
            SwingUtilities.invokeAndWait(() -> {
                robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
                FrameManager.forceFocusDialog.moveToMouse();
                FrameManager.forceFocusDialog.setVisible(true);
                robot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
                robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
                FrameManager.forceFocusDialog.setVisible(false);
            });
        } catch (InterruptedException | InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    public static void focus() {
        User32.INSTANCE.EnumWindows((hWnd, arg1) -> {
            char[] className = new char[512];
            User32.INSTANCE.GetClassName(hWnd, className, 512);
            String wText = Native.toString(className);
            if (wText.isEmpty()) {
                return true;
            }
            if (wText.equals("POEWindowClass")) {
                User32.INSTANCE.ShowWindow(hWnd, User32.SW_SHOW);
                User32.INSTANCE.SetForegroundWindow(hWnd);
                User32.INSTANCE.SetFocus(hWnd);
                return false;
            }
            return true;
        }, null);
    }

    public static boolean isPoeFocused(boolean checkApp) {
        byte[] windowText = new byte[512];
        PointerType hwnd = User32.INSTANCE.GetForegroundWindow();
        User32Custom.INSTANCE.GetWindowTextA(hwnd, windowText, 512);
        String curWindowTitle = Native.toString(windowText);
        if (curWindowTitle.startsWith(References.POE_WINDOW_TITLE)) {
            return true;
        }
        return checkApp && curWindowTitle.startsWith(References.APP_NAME);
    }

    private static void fixKeys() {
        System.out.println(App.globalKeyboard.isCtrlPressed());
        System.out.println(App.globalKeyboard.isAtlPressed());
        System.out.println(App.globalKeyboard.isShiftPressed());
    }

}

package com.slimtrade.core.utility;

import com.slimtrade.App;
import com.slimtrade.core.References;
import com.slimtrade.core.parsing.*;
import com.slimtrade.enums.LangRegex;
import com.slimtrade.enums.MessageType;
import com.slimtrade.gui.FrameManager;
import com.slimtrade.gui.options.ignore.IgnoreData;
import org.apache.commons.io.input.Tailer;

import javax.swing.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ChatParser {

    private static final Pattern SEARCH_PATTERN = Pattern.compile(References.REGEX_SCANNER_PREFIX + "(?<scannerMessage>.+))");
    private static final Pattern JOINED_PATTERN = Pattern.compile(".+ : (.+) has joined the area(.)");
    private String[] searchIgnoreTerms;
    private String[] searchTerms;
    private String searchName;

    // File Tailing
    private Tailer tailer;
    public ChatTailerListener chatListener;
    private ArrayList<IgnoreData> whisperIgnoreData;
    private boolean chatScannerRunning;

    boolean initialized;

    // Callbacks
    public List<ITradeOfferCallback> tradeOfferPreloadCallbackList = new ArrayList<ITradeOfferCallback>();
    public List<ITradeOfferCallback> tradeOfferCallbackList = new ArrayList<ITradeOfferCallback>();
    public List<IChatScannerCallback> chatScannerCallbackList = new ArrayList<IChatScannerCallback>();
    public List<IPlayerJoinedAreaCallback> playerJoinedAreaCallbackList = new ArrayList<IPlayerJoinedAreaCallback>();
    public List<ITradeHistoryCallback> tradeHistoryCallbackList = new ArrayList<ITradeHistoryCallback>();

    public void init() {
        initialized = false;
        if (tailer != null) {
            tailer.stop();
        }
        initHistory();
        initChatTailer();
        initialized = true;
    }

    private void initHistory() {

        try {
            InputStreamReader stream = new InputStreamReader(new FileInputStream(App.saveManager.settingsSaveFile.clientPath), StandardCharsets.UTF_8);
            BufferedReader reader = new BufferedReader(stream);
            while (reader.ready()) {
                parseLine(reader.readLine());
            }
            reader.close();
            for (ITradeHistoryCallback callback : tradeHistoryCallbackList) {
                callback.onHistoryLoaded();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void initChatTailer() {
        chatListener = new ChatTailerListener(this);
        File clientFile = new File(App.saveManager.settingsSaveFile.clientPath);
        if (clientFile.exists()) {
            tailer = Tailer.create(clientFile, chatListener, 100, true);
        } else {
            App.debugger.log("[ERROR] Chat parser failed to launch.");
            return;
        }
        chatListener.init(tailer);
    }

    public void parseLine(String text) {
        TradeOffer trade = App.chatParser.getTradeOffer(text);
        // Preload, no UI update
        if (!initialized && trade != null) {
            for (ITradeHistoryCallback callback : tradeHistoryCallbackList) {
                callback.onNewTrade(trade);
            }
            return;
        }
        if (!initialized) return;
        // Trade Offer
        if (trade != null) {
            SwingUtilities.invokeLater(() -> {
                for (ITradeOfferCallback callback : tradeOfferCallbackList) {
                    callback.onNewTrade(trade);
                }
            });
        }
        // Chat Scanner
        else {
            TradeOffer searchOffer = App.chatParser.getSearchOffer(text);
            if (searchOffer != null) {

                SwingUtilities.invokeLater(() -> {
                    for (IChatScannerCallback callback : chatScannerCallbackList) {
                        callback.onNewChatScan(searchOffer);
                    }
                });
            }
        }
        // Player Joined Area
        for (LangRegex l : LangRegex.values()) {
            if (l.JOINED_AREA_PATTERN == null) continue;
            Matcher matcher = l.JOINED_AREA_PATTERN.matcher(text);
            if (matcher.matches()) {
                SwingUtilities.invokeLater(() -> {
                    String username = matcher.group("username");
                    for (IPlayerJoinedAreaCallback callback : playerJoinedAreaCallbackList) {
                        callback.onPlayerJoinedArea(username);
                    }
                });
                break;
            }
        }
    }

    public static LangRegex getLang(String text) {
        // Languages only support one contain text so 'wtb' is checked separately to support legacy sites
        if (text.contains("wtb")) {
            return LangRegex.ENGLISH;
        }
        for (LangRegex l : LangRegex.values()) {
            if (text.contains(l.CONTAINS_TEXT)) {
                return l;
            }
        }
        return null;
    }

    public void setSearchName(String searchName) {
        this.searchName = searchName;
    }

    public void setSearchTerms(String[] terms) {
        this.searchTerms = terms;
    }

    public void setSearchIgnoreTerms(String[] terms) {
        this.searchIgnoreTerms = terms;
    }

    public boolean validateQuickPaste(String text, LangRegex lang) {
        Matcher matcher;
        for (Pattern p : lang.QUICK_PASTE_PATTERNS) {
            matcher = p.matcher(text);
            if (matcher.matches()) {
                return true;
            }
        }
        return false;
    }

    public TradeOffer getTradeOffer(String text) {
        if (!text.contains("@")) return null;
        LangRegex lang = getLang(text);
        if (getLang(text) == null) return null;
        Matcher matcher = null;
        boolean found = false;
        for (Pattern p : lang.CLIENT_PATTERNS) {
            matcher = p.matcher(text);
            if (matcher.matches()) {
                found = true;
                break;
            }
        }
        if (!found) {
            return null;
        }
        TradeOffer trade = new TradeOffer();
        trade.date = matcher.group("date").replaceAll("/", "-");
        trade.time = matcher.group("time");
        trade.time = cleanResult(matcher, "time");
        trade.messageType = getMessageType(matcher.group("messageType"));
        trade.guildName = matcher.group("guildName");
        trade.playerName = matcher.group("playerName");
        trade.itemName = matcher.group("itemName");
        trade.itemQuantity = cleanDouble(cleanResult(matcher, "itemQuantity"));
        trade.priceTypeString = cleanResult(matcher, "priceType");
        trade.priceQuantity = cleanDouble(cleanResult(matcher, "priceQuantity"));
        trade.stashtabName = cleanResult(matcher, "stashtabName");
        trade.stashtabX = cleanInt(cleanResult(matcher, "stashX"));
        trade.stashtabY = cleanInt(cleanResult(matcher, "stashY"));
        trade.bonusText = cleanResult(matcher, "bonusText");
        trade.sentMessage = matcher.group("message");
        if (trade.messageType == MessageType.UNKNOWN) {
            return null;
        }
        return trade;
    }

    public TradeOffer getSearchOffer(String text) {
        if (searchTerms == null) return null;
        Matcher matcher = SEARCH_PATTERN.matcher(text);
        if (matcher.matches()) {
            TradeOffer trade = new TradeOffer();
            trade.date = matcher.group("date");
            trade.time = matcher.group("time");
            trade.messageType = MessageType.CHAT_SCANNER;
            trade.guildName = matcher.group("guildName");
            trade.playerName = matcher.group("playerName");
            trade.searchName = this.searchName;
            trade.searchMessage = matcher.group("scannerMessage");
            String chatMessage = trade.searchMessage.toLowerCase();
            if (this.searchIgnoreTerms != null) {
                for (String s : this.searchIgnoreTerms) {
                    if (chatMessage.contains(s)) {
                        return null;
                    }
                }
            }
            boolean found = false;
            for (String s : this.searchTerms) {
                if (!s.equals("")) {
                    if (chatMessage.contains(s)) {
                        found = true;
                        break;
                    }
                }
            }
            if (found) {
                return trade;
            }
        }
        return null;
    }

    private <T> T cleanResult(Matcher matcher, String text) {
        try {
            return (T) matcher.group(text);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private int cleanInt(String text) {
        if (text == null) {
            return 0;
        }
        return Integer.parseInt(text);
    }

    private double cleanDouble(String text) {
        if (text == null) {
            return 0;
        }
        text = text.replaceAll(",", ".");
        return Double.parseDouble(text);
    }

    private MessageType getMessageType(String s) {
        MessageType type = MessageType.UNKNOWN;
        switch (s.toLowerCase()) {
            case "to":
            case "向":      // Chinese
            case "à":       // French
            case "an":      // German
            case "para":    // Portuguese & Spanish
            case "кому":    // Russian
            case "ถึง":      // Thai
                type = MessageType.OUTGOING_TRADE;
                break;
            case "from":
            case "來自":     // Chinese
            case "de":      // French, Portuguese & Spanish
            case "von":     // German
            case "от кого": // Russian
            case "จาก":     // Thai
                type = MessageType.INCOMING_TRADE;
                break;
        }
        return type;
    }

    public void setWhisperIgnoreTerms(ArrayList<IgnoreData> ignoreData) {
        this.whisperIgnoreData = ignoreData;
    }

    public void setChatScannerRunning(boolean state) {
        this.chatScannerRunning = state;
    }
}

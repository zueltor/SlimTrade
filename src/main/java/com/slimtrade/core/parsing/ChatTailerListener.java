package com.slimtrade.core.parsing;

import com.slimtrade.core.utility.ChatParser;
import org.apache.commons.io.input.TailerListenerAdapter;

public class ChatTailerListener extends TailerListenerAdapter {

    private ChatParser chatParser;
    private boolean disabled;

    public ChatTailerListener(ChatParser chatParser){
       this.chatParser = chatParser;
    }

    public void handle(String line) {
        if(disabled) return;
        chatParser.parseLine(line);
    }

    @Override
    public void fileRotated() {
        super.fileRotated();
        disabled = true;
        chatParser.init();
    }

}

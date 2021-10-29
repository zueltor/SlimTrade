package com.slimtrade.core.parsing;

import com.slimtrade.core.utility.TradeOffer;

public interface IChatScannerCallback {

    void onNewChatScan(TradeOffer tradeOffer);

}

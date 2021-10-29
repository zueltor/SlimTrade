package com.slimtrade.core.parsing;

import com.slimtrade.core.utility.TradeOffer;

public interface ITradeHistoryCallback extends ITradeOfferCallback{

    public void onHistoryInit();
    public void onHistoryLoaded();

}

package com.slimtrade.core.parsing;

import com.slimtrade.core.utility.TradeOffer;

public interface ITradeOfferCallback {

    void onNewTrade(TradeOffer tradeOffer);

}

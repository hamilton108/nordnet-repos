package nordnet.html;

import nordnet.downloader.PageInfo;
import nordnet.downloader.TickerInfo;
import oahu.financial.StockOptionPrice;
import oahu.financial.StockPrice;

import java.util.Collection;
import java.util.Optional;

public interface StockOptionParser {
    Optional<StockPrice> stockPrice(TickerInfo tickerInfo, PageInfo pageInfo);
    Collection<StockOptionPrice> options(PageInfo pageInfo, StockPrice stockPrice);
}

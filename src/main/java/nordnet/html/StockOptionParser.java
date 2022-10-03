package nordnet.html;

import critter.stock.StockPrice;
import critter.stockoption.StockOptionPrice;
import nordnet.downloader.PageInfo;
import nordnet.downloader.TickerInfo;

import java.util.Collection;

public interface StockOptionParser {
    StockPrice stockPrice(int oid, PageInfo pageInfo);
    Collection<StockOptionPrice> options(PageInfo pageInfo, StockPrice stockPrice);
}

package nordnet.html;

import critter.stock.Stock;
import critter.stock.StockPrice;
import critter.util.StockOptionUtil;

public abstract class StockOptionParserBase {
    protected final StockOptionUtil stockOptionUtil;

    public StockOptionParserBase(StockOptionUtil stockOptionUtil) {
        this.stockOptionUtil = stockOptionUtil;
    }
    protected StockPrice createStockPrice(double opn,
                                          double hi,
                                          double lo,
                                          double cls,
                                          Stock stock) {
        StockPrice result = new StockPrice();
        result.setOpn(opn);
        result.setHi(hi);
        result.setLo(lo);
        result.setCls(cls);
        result.setStock(stock);
        result.setVolume(1000);
        result.setLocalDx(stockOptionUtil.getCurrentDate());
        return result;
    }
}

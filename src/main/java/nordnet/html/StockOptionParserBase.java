package nordnet.html;

import critterrepos.beans.StockPriceBean;
import critterrepos.utils.StockOptionUtils;
import oahu.financial.Stock;
import oahu.financial.StockPrice;

public abstract class StockOptionParserBase {
    protected final StockOptionUtils stockOptionUtils;

    public StockOptionParserBase(StockOptionUtils stockOptionUtils) {
        this.stockOptionUtils = stockOptionUtils;
    }
    protected StockPrice createStockPrice(double opn,
                                          double hi,
                                          double lo,
                                          double cls,
                                          Stock stock) {
        StockPriceBean result = new StockPriceBean();
        result.setOpn(opn);
        result.setHi(hi);
        result.setLo(lo);
        result.setCls(cls);
        result.setStock(stock);
        result.setVolume(1000);
        result.setLocalDx(stockOptionUtils.getCurrentDate());
        return result;
    }
}

package nordnet.html;

import critter.repos.StockMarketRepository;
import critter.stock.Stock;
import critter.stock.StockPrice;
import critter.stockoption.StockOption;
import critter.stockoption.StockOptionPrice;
import critter.util.StockOptionUtil;
import nordnet.financial.OpeningPrices;
import org.jsoup.nodes.Element;
import vega.financial.calculator.OptionCalculator;

import java.time.LocalDate;
import java.util.Optional;

public abstract class StockOptionParserBase {
    protected final OptionCalculator optionCalculator;
    //protected final StockOptionUtil stockOptionUtil;
    protected final StockMarketRepository<Integer,String> stockMarketRepos;
    protected final OpeningPrices openingPrices;
    protected final LocalDate currentDate;

    public StockOptionParserBase(OptionCalculator optionCalculator,
                                 OpeningPrices nordnetRedis,
                                 StockMarketRepository stockMarketRepos,
                                 LocalDate currentDate) {
        this.optionCalculator = optionCalculator;
        this.stockMarketRepos = stockMarketRepos;
        this.openingPrices = nordnetRedis;
        this.currentDate = currentDate;
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
        result.setLocalDx(currentDate);
        return result;
    }
    protected double elementToDouble(Element el) {
        var s = el.text();
        return Double.parseDouble(s.replace(",", "."));
    }



    protected  StockOption fetchOrCreateStockOption(String ticker,
                                                 double x,
                                                 StockOption.OptionType optionType,
                                                 StockPrice stockPrice) {

        Optional<StockOption> result = stockMarketRepos.findStockOption(ticker);

        if (result.isPresent()) {
            var so = result.get();
            so.setLifeCycle(StockOption.LifeCycle.FROM_DATABASE);
            //so.setStockOptionUtil(stockOptionUtil);
            so.setCurrentDate(currentDate);
            return so;
        }
        else {
            var so = new StockOption();
            so.setTicker(ticker);
            so.setLifeCycle(StockOption.LifeCycle.FROM_HTML);
            so.setOpType(optionType);
            so.setX(x);
            //so.setStockOptionUtil(stockOptionUtil);
            so.setCurrentDate(currentDate);
            so.setStock(stockPrice.getStock());
            return so;
        }
    }
    protected StockOptionPrice createStockOptionPrice(StockPrice stockPrice,
                                                      String optionTicker,
                                                      StockOption.OptionType optionType,
                                                      double bid,
                                                      double ask,
                                                      double x) {
        StockOption callOption = fetchOrCreateStockOption(optionTicker, x, optionType, stockPrice);
        var price = new StockOptionPrice();
        price.setStockOption(callOption);
        price.setStockPrice(stockPrice);
        price.setBuy(bid);
        price.setSell(ask);
        price.setCalculator(optionCalculator);
        return price;
    }
}

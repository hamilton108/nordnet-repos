package nordnet.html;

import critterrepos.beans.options.StockOptionBean;
import critterrepos.beans.options.StockOptionPriceBean;
import critterrepos.utils.StockOptionUtils;
import nordnet.downloader.PageInfo;
import nordnet.downloader.TickerInfo;
import nordnet.redis.NordnetRedis;
import oahu.dto.Tuple;
import oahu.exceptions.FinancialException;
import oahu.financial.*;
import oahu.financial.repository.StockMarketRepository;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;

import static nordnet.html.StockOptionEnum.X;

public class StockOptionParser2 extends  StockOptionParserBase implements StockOptionParser {

    private final OptionCalculator optionCalculator;
    private final NordnetRedis nordnetRedis;
    private final StockMarketRepository stockMarketRepos;

    public StockOptionParser2(OptionCalculator optionCalculator,
                              NordnetRedis nordnetRedis,
                              StockMarketRepository stockMarketRepos,
                              StockOptionUtils stockOptionUtils) {
        super(stockOptionUtils);
        this.optionCalculator = optionCalculator;
        this.nordnetRedis = nordnetRedis;
        this.stockMarketRepos = stockMarketRepos;
    }

    @Override
    public Optional<StockPrice> stockPrice(TickerInfo tickerInfo, PageInfo pageInfo) {
        Stock stock = stockMarketRepos.findStock(tickerInfo.getTicker());
        var doc = Jsoup.parse(pageInfo.getPage().getWebResponse().getContentAsString());
        return createStockPrice(doc, stock);
    }

    private Optional<StockPrice> createStockPrice(Document doc, Stock stock) {
        double opn = nordnetRedis.fetchPrice(stock.getTicker());
        var rows = doc.select("[role=row]");
        var row = rows.get(1);
        var arias= row.select("[aria-hidden=true]");
        try {
            var cls = elementToDouble(arias.get(2));
            var hi = elementToDouble(arias.get(5));
            var lo = elementToDouble(arias.get(6));
            var stockPrice = createStockPrice(opn, hi, lo, cls, stock);
            return Optional.of(stockPrice);
        }
        catch (Exception ex) {
            return Optional.empty();
        }
    }


    @Override
    public Collection<StockOptionPrice> options(PageInfo pageInfo, StockPrice stockPrice) {
        Collection<StockOptionPrice> result = new ArrayList<>();
        var doc = Jsoup.parse(pageInfo.getPage().getWebResponse().getContentAsString());
        var rows = doc.select("[role=row]");
        int sz = rows.size();
        int optionIndex = -1;
        for (int i = 1; i < sz; ++i) {
            var row = rows.get(i);
            var arias= row.select("[aria-hidden=true]");
            if (arias.size() == 13) {
                optionIndex = i;
                break;
            }
            //System.out.println(arias.size());
        }
        if (optionIndex == -1) {
            throw new FinancialException("No rows with size 13, cannot evaluate option prices");
        }
        for (int i = optionIndex; i < sz; ++i) {
            var row = rows.get(i);
            var arias= row.select("[aria-hidden=true]");
            var x = elementToDouble(arias.get(5));
            var optionNames = row.getElementsByTag("a");
            var call = createOption(arias, optionNames, x, stockPrice, StockOption.OptionType.CALL);
            call.ifPresent(result::add);
            var put = createOption(arias, optionNames, x, stockPrice, StockOption.OptionType.PUT);
            put.ifPresent(result::add);
        }
        return result;
    }

    private Optional<StockOptionPrice> createOption(Elements arias,
                                                    Elements optionNames,
                                                    double x,
                                                    StockPrice stockPrice,
                                                    StockOption.OptionType ot) {
        try {
            int tickerIndex = ot == StockOption.OptionType.CALL ?  2 : 3;
            int bidIndex = ot == StockOption.OptionType.CALL ?  0 : 10;
            int askIndex = ot == StockOption.OptionType.CALL ?  2 : 8;
            var bid = elementToDouble(arias.get(bidIndex));
            var ask = elementToDouble(arias.get(askIndex));
            var ticker = optionNames.get(tickerIndex).text().strip();

            StockOption stockOption = fetchOrCreateStockOption(ticker, x, ot, stockPrice);

            StockOptionPriceBean price = new StockOptionPriceBean();
            price.setDerivative(stockOption);
            price.setStockPrice(stockPrice);
            price.setBuy(bid);
            price.setSell(ask);
            price.setCalculator(optionCalculator);

            return Optional.of(price);
        }
        catch (NumberFormatException | NullPointerException ex) {
            return Optional.empty();
        }
    }

    private StockOption fetchOrCreateStockOption(String ticker,
                                                 double x,
                                                 StockOption.OptionType optionType,
                                                 StockPrice stockPrice) {

        Optional<StockOption> result = stockMarketRepos.findDerivative(ticker);

        if (result.isPresent()) {
            return result.get();
        }
        else {
            StockOptionBean so = new StockOptionBean();
            so.setTicker(ticker);
            so.setLifeCycle(StockOption.LifeCycle.FROM_HTML);
            so.setOpType(optionType);
            so.setX(x);
            so.setStockOptionUtils(stockOptionUtils);
            so.setStock(stockPrice.getStock());
            return so;
        }
    }

    private double elementToDouble(Element el) {
        var s = el.text();
        return Double.parseDouble(s.replace(",", "."));
    }

    /*
    private Tuple<Optional<StockOptionPrice>> createCallAndPut(Element el, StockPrice stockPrice) {
        var arias= el.select("[aria-hidden=true]");
        var x = elementToDouble(arias.get(5));
        var callBid = elementToDouble(arias.get(0));
        var callAsk = elementToDouble(arias.get(2));
        var putBid = elementToDouble(arias.get(10));
        var putAsk = elementToDouble(arias.get(8));
        var optionNames = el.getElementsByTag("a");
        var callTicker = optionNames.get(2).text().strip();
        var putTicker = optionNames.get(3).text().strip();

        StockOptionPriceBean callPrice = new StockOptionPriceBean();
        StockOption stockOption = fetchOrCreateStockOption(callTicker, x, StockOption.OptionType.CALL, stockPrice);
        ((StockOptionBean) stockOption).setStock(stockPrice.getStock());
        callPrice.setDerivative(stockOption);
        callPrice.setStockPrice(stockPrice);
        callPrice.setBuy(callBid);
        callPrice.setSell(callAsk);
        callPrice.setCalculator(optionCalculator);

        return new Tuple<>(Optional.empty(), Optional.empty());
    }
     */

}

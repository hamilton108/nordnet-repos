package nordnet.html;

import critterrepos.utils.StockOptionUtils;
import nordnet.downloader.PageInfo;
import nordnet.downloader.TickerInfo;
import nordnet.redis.NordnetRedis;
import oahu.financial.OptionCalculator;
import oahu.financial.Stock;
import oahu.financial.StockOptionPrice;
import oahu.financial.StockPrice;
import oahu.financial.repository.StockMarketRepository;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.util.Collection;
import java.util.Optional;

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

    private double elementToDouble(Element el) {
        var s = el.text();
        return Double.parseDouble(s.replace(",", "."));
    }

    @Override
    public Collection<StockOptionPrice> options(PageInfo pageInfo, StockPrice stockPrice) {
        return null;
    }

}

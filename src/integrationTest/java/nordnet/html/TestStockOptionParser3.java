package nordnet.html;

import critter.util.StockOptionUtil;
import nordnet.downloader.DownloaderStub;
import nordnet.downloader.PageInfo;
import nordnet.downloader.TickerInfo;
import nordnet.redis.NordnetRedis;
import nordnet.repos.StockMarketReposStub;
import oahu.financial.html.EtradeDownloader;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;
import vega.financial.calculator.BlackScholes;
import vega.financial.calculator.OptionCalculator;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
public class TestStockOptionParser3 {
    private final LocalDate currentDate = LocalDate.of(2022,5,6);
    private static final String storePath = "/home/rcs/opt/java/nordnet-repos/src/integrationTest/resources/html/derivatives";
    private StockOptionParser3 stockOptionParser;
    private EtradeDownloader<List<PageInfo>, TickerInfo, Serializable> downloader;
    private final TickerInfo tickerInfo = new TickerInfo("YAR");
    private final StockOptionUtil stockOptionUtils = new StockOptionUtil(currentDate);

    @Before
    public void init() {
        StockMarketReposStub stockMarketRepos = new StockMarketReposStub();
        downloader = new DownloaderStub(storePath, 3);
        OptionCalculator optionCalculator = new BlackScholes();
        NordnetRedis nordnetRedis = new NordnetRedis("172.20.1.2", 5);
        stockOptionParser = new StockOptionParser3(
                optionCalculator,
                nordnetRedis,
                stockMarketRepos,
                stockOptionUtils);
    }

    @Test
    public void test_stockprice() {
        var sp = stockOptionParser.stockPrice(tickerInfo, getPage());
        //assertThat(stockPrice).isNotEmpty();
        //var sp = stockPrice.get();
        assertThat(sp.getStock()).isNotNull();
        assertThat(sp.getStock().getTicker()).isEqualTo("YAR");
        assertThat(sp.getOpn()).isEqualTo(482.0);
        assertThat(sp.getHi()).isEqualTo(488.7);
        assertThat(sp.getLo()).isEqualTo(481.0);
        assertThat(sp.getCls()).isEqualTo(487.4);
    }

    @Test
    public void test_option_prices() {
        var page = getPage();
        var stockPrice = stockOptionParser.stockPrice(tickerInfo, page);

        var options = stockOptionParser.options(page, stockPrice);
        assertThat(options.size()).isEqualTo(66);
    }

    @Test
    public void test_normalize_ticker() {
        String invalidTicker = stockOptionParser.elementToTicker("Norway YAR2F534.58X");
        assertThat(invalidTicker).isNull();
        String validTicker = stockOptionParser.elementToTicker("Norway YAR2F470");
        assertThat(validTicker).isNotNull();
        assertThat(validTicker).isEqualTo("YAR2F470");
    }

    private PageInfo getPage() {
        List<PageInfo> pages = downloader.downloadDerivatives(tickerInfo);
        return pages.get(0);
    }
}

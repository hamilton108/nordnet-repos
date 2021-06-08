package nordnet.html;

import critterrepos.beans.options.StockOptionBean;
import critterrepos.utils.StockOptionUtils;
import nordnet.downloader.DownloaderStub;
import nordnet.downloader.PageInfo;
import nordnet.downloader.TickerInfo;
import nordnet.redis.NordnetRedis;
import nordnet.repos.StockMarketReposStub;
import oahu.financial.OptionCalculator;
import oahu.financial.StockOption;
import oahu.financial.StockOptionPrice;
import oahu.financial.html.EtradeDownloader;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;
import vega.financial.calculator.BlackScholes;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static oahu.financial.StockOption.OptionType;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.offset;

@RunWith(SpringRunner.class)
public class TestStockOptionParser2 {

    private final LocalDate currentDate = LocalDate.of(2020,9,22);
    private static final String storePath = "/home/rcs/opt/java/nordnet-repos/src/integrationTest/resources/html/derivatives";
    private StockOptionParser2 stockOptionParser;
    private EtradeDownloader<PageInfo, TickerInfo, Serializable> downloader;
    private final TickerInfo tickerInfo = new TickerInfo("EQNR");
    private final StockOptionUtils stockOptionUtils = new StockOptionUtils(currentDate);

    @Before
    public void init() {
        StockMarketReposStub stockMarketRepos = new StockMarketReposStub();
        downloader = new DownloaderStub(storePath, 2);
        OptionCalculator optionCalculator = new BlackScholes();
        NordnetRedis nordnetRedis = new NordnetRedis("172.20.1.2", 5);
        stockOptionParser = new StockOptionParser2(
                optionCalculator,
                nordnetRedis,
                stockMarketRepos,
                stockOptionUtils);
    }

    @Test
    public void test_stockprice() {
        var stockPrice = stockOptionParser.stockPrice(tickerInfo, getPage());
        assertThat(stockPrice).isNotEmpty();
        var sp = stockPrice.get();
        assertThat(sp.getStock()).isNotNull();
        assertThat(sp.getStock().getTicker()).isEqualTo("EQNR");
        assertThat(sp.getOpn()).isEqualTo(190.0);
        assertThat(sp.getHi()).isEqualTo(191.60);
        assertThat(sp.getLo()).isEqualTo(190.22);
        assertThat(sp.getCls()).isEqualTo(190.48);
    }

    /*
    @Test
    public void test_option_prices() {
        var page = getPage();
        var stockPrice = stockOptionParser1.stockPrice(tickerInfo, page);
        assertThat(stockPrice).isNotEmpty();
        var options = stockOptionParser1.options(page, stockPrice.get());
        assertThat(options.size()).isEqualTo(10);

        var calls = stockOptionParser1.calls(options);

        assertThat(calls.size()).isEqualTo(5);

        String callTicker = "EQNR0K125";
        Optional<StockOptionPrice> call = calls.stream().filter(x -> x.getTicker().equals(callTicker)).findAny();
        assertThat(call).isNotEmpty();

        call.ifPresent( c -> {
            StockOptionBean d = (StockOptionBean)c.getDerivative();
            //d.setCurrentDate(currentDate); //LocalDate.of(2020,9,22));
            assertThat(d.getStock()).isNotNull();
            assertThat(d).isNotNull();
            assertThat(d.getOpType()).isEqualTo(OptionType.CALL);
            assertThat(d.getX()).isEqualTo(125);
            assertThat(d.getExpiry()).isEqualTo(LocalDate.of(2020,11,20));
            assertThat(d.getLifeCycle()).isEqualTo(StockOption.LifeCycle.FROM_HTML);
            assertThat(c.getStockPrice()).isNotNull();
            assertThat(c.getStockPrice().getOpn()).isEqualTo(131.00);
            assertThat(c.getStockPrice().getHi()).isEqualTo(133.35);
            assertThat(c.getStockPrice().getLo()).isEqualTo(130.80);
            assertThat(c.getStockPrice().getCls()).isEqualTo(132.30);
            assertThat(c.getBuy()).as("Bid").isEqualTo(9.5);
            assertThat(c.getSell()).as("Ask").isEqualTo(11.25);

            assertThat(c.optionPriceFor(138.0)).as("Option price for").isCloseTo(14.45, offset(0.05));
        });

        var puts = stockOptionParser1.puts(options);
        String putTicker = "EQNR0W125";
        Optional<StockOptionPrice> put = puts.stream().filter(x -> x.getTicker().equals(putTicker)).findAny();
        assertThat(put).isNotEmpty();

        put.ifPresent( c -> {
            StockOption d = c.getDerivative();
            assertThat(d).isNotNull();
            assertThat(d.getOpType()).isEqualTo(OptionType.PUT);
            assertThat(d.getX()).isEqualTo(125);
            assertThat(d.getExpiry()).isEqualTo(LocalDate.of(2020,11,20));
            assertThat(d.getLifeCycle()).isEqualTo(StockOption.LifeCycle.FROM_HTML);
            assertThat(c.getBuy()).as("Bid").isEqualTo(2.95);
            assertThat(c.getSell()).as("Ask").isEqualTo(3.95);
        });
    }
    */

    private PageInfo getPage() {
        List<PageInfo> pages = downloader.downloadDerivatives(tickerInfo);
        return pages.get(0);
    }
}



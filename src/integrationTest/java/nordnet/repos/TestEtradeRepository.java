package nordnet.repos;

import nordnet.downloader.DownloaderStub;
import nordnet.downloader.TickerInfo;
import oahu.financial.Stock;
import oahu.financial.StockPrice;
import oahu.financial.html.EtradeDownloader;
import oahu.financial.repository.StockMarketRepository;
import oahu.testing.TestUtil;
import static  org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.data.Offset.offset;

import org.jsoup.nodes.Document;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Optional;

@RunWith(SpringRunner.class)
public class TestEtradeRepository {
    private static String storePath = "/home/rcs/opt/java/nordnet-repos/src/integrationTest/resources/html/derivatives";

    private static EtradeRepositoryImpl repos;
    private static StockMarketRepository stockMarketRepos;
    private TickerInfo tickerInfo = new TickerInfo("EQNR");

    @BeforeClass
    public static void setup() {
        repos = new EtradeRepositoryImpl();

        stockMarketRepos = new StockMarketReposStub();
        repos.setStockMarketRepository(stockMarketRepos);

        EtradeDownloader downloader = new DownloaderStub(storePath);
        repos.setDownloader(downloader);

        //OptionCalculator calculator = new BlackScholesStub();
        //repos.setOptionCalculator(calculator);
    }

    @Test
    public void testDocIsNotNull() {
        Document doc = getDocument();
        assertThat(doc).isNotNull();
    }

    @Test
    public void testStockPrice() {
        Stock stock = stockMarketRepos.findStock(tickerInfo.getTicker());

        Document doc = getDocument();

        Optional<StockPrice> stockPrice = createStockPrice(doc, stock);
        assertThat(stockPrice.isPresent()).isEqualTo(true);
        validateStockPrice(stockPrice.get());
    }

    private Optional<StockPrice> createStockPrice(Document doc, Stock stock) {
        Class[] paramsTypes = {Document.class, Stock.class};
        Object[] params = {doc,stock};
        return TestUtil.callMethodFor(EtradeRepositoryImpl.class, repos, "createStockPrice", paramsTypes, params);
    }
    private void validateStockPrice(StockPrice stockPrice) {
        double expectedOpn = 41.83;
        assertThat(stockPrice.getOpn()).isCloseTo(expectedOpn, offset(0.01));

        double expectedHi = 41.85;
        assertThat(stockPrice.getHi()).isCloseTo(expectedHi, offset(0.01));

        double expectedLo = 40.87;
        assertThat(stockPrice.getLo()).isCloseTo(expectedLo, offset(0.01));

        double expectedCls = 41.05;
        assertThat(stockPrice.getCls()).isCloseTo(expectedCls, offset(0.01));

        long expectedVol = 4777330;
        assertThat(stockPrice.getVolume()).isEqualTo(expectedVol);
    }
    private Document getDocument() {
        Class[] paramsTypes = {TickerInfo.class};
        Object[] params = {tickerInfo};
        return TestUtil.callMethodFor(EtradeRepositoryImpl.class, repos, "getDocument", paramsTypes, params);
    }
}

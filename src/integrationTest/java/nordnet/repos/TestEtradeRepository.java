package nordnet.repos;

import nordnet.downloader.DownloaderStub;
import nordnet.downloader.TickerInfo;
import oahu.financial.Stock;
import oahu.financial.StockPrice;
import oahu.financial.html.EtradeDownloader;
import oahu.financial.html.WebClientManager;
import oahu.financial.repository.StockMarketRepository;
import oahu.testing.TestUtil;
import static  org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.data.Offset.offset;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Optional;

@RunWith(SpringRunner.class)
public class TestEtradeRepository {
    private static String storePath = "/home/rcs/opt/java/nordnet-repos/src/integrationTest/resources/html/derivatives";
    //private static String storePath = "c:/opt/lx/nordnet-repos/src/integrationTest/resources/html/derivatives";

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

    private Document _doc;
    private Document getCachedDocument() {
        if (_doc == null) {
            System.out.println("No cache...");
            _doc = getDocument();
        }
        return _doc;
    }

    @Test
    public void testC01408Table1() {
        Document doc = getCachedDocument();
        Elements tables = doc.getElementsByClass("c01408");
        assertThat(tables.size()).isEqualTo(3);

        Element table1 = tables.first();
        Elements rows = table1.getElementsByTag("tr");
        assertThat(rows.size()).isEqualTo(4);

        Element row1 = rows.first();
        Elements tds = row1.getElementsByTag("td");
        assertThat(tds.size()).isEqualTo(22);

        Element row8 = tds.get(8);
        Element cell8 = row8.getElementsByClass("c01438").first();
        assertThat(cell8.text()).isEqualTo("160.15");

        Element row9 = tds.get(9);
        Element cell9 = row9.getElementsByClass("c01438").first();
        assertThat(cell9.text()).isEqualTo("160.05");
    }

    public void testStockPrice() {
        Stock stock = stockMarketRepos.findStock(tickerInfo.getTicker());

        Document doc = getDocument();
        Optional<StockPrice> stockPrice = createStockPrice(doc, stock);
        assertThat(stockPrice.isPresent()).isEqualTo(true);
        stockPrice.ifPresent(this::validateStockPrice);
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

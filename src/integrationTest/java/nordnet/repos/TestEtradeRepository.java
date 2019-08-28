package nordnet.repos;

import nordnet.downloader.DownloaderStub;
import nordnet.downloader.TickerInfo;
import static nordnet.html.DerivativesEnum.*;

import oahu.financial.StockPrice;
import oahu.financial.html.EtradeDownloader;
import oahu.testing.TestUtil;

import static nordnet.html.DerivativesStringEnum.TABLE_CLASS;
import static nordnet.html.DerivativesStringEnum.TD_CLASS;
import static  org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.File;
import java.util.List;
import java.util.Map;

@RunWith(SpringRunner.class)
public class TestEtradeRepository {
    //private static String storePath = "/home/rcs/opt/java/nordnet-repos/src/integrationTest/resources/html/derivatives";
    private static String storePath = "c:/opt/lx/nordnet-repos/src/integrationTest/resources/html/derivatives";

    private StockMarketReposStub stockMarketRepos = new StockMarketReposStub();
    private EtradeDownloader downloader = new DownloaderStub(storePath);
    private EtradeRepositoryImpl repos;

    @Before
    public void init() {
        repos = new EtradeRepositoryImpl();
        repos.setStorePath(storePath);

        stockMarketRepos = new StockMarketReposStub();
        repos.setStockMarketRepository(stockMarketRepos);

        EtradeDownloader downloader = new DownloaderStub(storePath);
        repos.setDownloader(downloader);
    }

    private Document getDocument(TickerInfo tickerInfo, EtradeRepositoryImpl curRepos) {
        Class[] paramsTypes = {TickerInfo.class};
        Object[] params = {tickerInfo};
        return TestUtil.callMethodFor(EtradeRepositoryImpl.class, curRepos, "getDocument", paramsTypes, params);
    }

    @Test
    @Ignore
    public void testC01408Table1() {
        Document doc = getDocument(new TickerInfo("EQNR"), repos);
        Elements tables = doc.getElementsByClass(TABLE_CLASS.getText());
        assertThat(tables.size()).isEqualTo(3);

        Element table1 = tables.first();
        Elements rows = table1.getElementsByTag("tr");
        assertThat(rows.size()).isEqualTo(1);

        Element row1 = rows.first();
        Elements tds = row1.getElementsByTag("td");
        assertThat(tds.size()).isEqualTo(22);

        Element rowClose = tds.get(STOCK_PRICE_CLOSE.getIndex());
        Element cellClose = rowClose.getElementsByClass(TD_CLASS.getText()).first();
        assertThat(cellClose.text()).isEqualTo("149.9");

        Element rowHi = tds.get(STOCK_PRICE_Hi.getIndex());
        Element cellHi = rowHi.getElementsByClass(TD_CLASS.getText()).first();
        assertThat(cellHi.text()).isEqualTo("150.85");

        Element rowLo = tds.get(STOCK_PRICE_Lo.getIndex());
        Element cellLo = rowLo.getElementsByClass(TD_CLASS.getText()).first();
        assertThat(cellLo.text()).isEqualTo("149.1");
    }

    @Test
    @Ignore
    public void testC01408Table2() {
        Document doc = getDocument(new TickerInfo("EQNR"), repos);
        Elements tables = doc.getElementsByClass(TABLE_CLASS.getText());

        Element table2 = tables.get(2);
        Elements rows = table2.getElementsByTag("tr");
        assertThat(rows.size()).isEqualTo(19);

        Element row1 = rows.get(5); // rows.first();
        Elements tds = row1.getElementsByTag("td");
        assertThat(tds.size()).isEqualTo(16);

        Element tickerBuy = tds.get(TICKER_BUY.getIndex());
        assertThat(tickerBuy.text()).isEqualTo("EQNR9H30Y162.50");

        Element x = tds.get(X.getIndex());
        assertThat(x.text()).isEqualTo("162.5 162,50");
    }

    @Test
    public void testOpeningPrices_no_file() {
        repos.setOpeningPricesFileName("openingPrices.txt");
        File openingPricesFileName = getOpeningPricesFile();
        boolean deleteResult = openingPricesFileName.delete();
        testOpeningPrices();
    }
    @Test

    public void testOpeningPrices_initialized() {
        repos.setOpeningPricesFileName("openingPrices_initialized.txt");
        testOpeningPrices();
    }

    private void testOpeningPrices() {
        repos.initOpeningPrices();
        File openingPricesFileName = getOpeningPricesFile();
        assertThat(openingPricesFileName.exists()).isEqualTo(true);
        Map<String,Double> openingPrices = repos.getOpeningPrices();
        assertThat(openingPrices.size()).isEqualTo(3);
    }

    private File getOpeningPricesFile() {
        return new File(repos.getOpeningPricesFileName());
    }

    /*
    private static EtradeRepositoryImpl repos;
    private static StockMarketRepository stockMarketRepos;
    //private TickerInfo tickerInfo = new TickerInfo("EQNR");
    private TickerInfo tickerInfo = new TickerInfo("NHY");

    @BeforeClass
    public static void setup() {
        repos = new EtradeRepositoryImpl();
        repos.setStorePath(storePath);

        stockMarketRepos = new StockMarketReposStub();
        repos.setStockMarketRepository(stockMarketRepos);

        EtradeDownloader downloader = new DownloaderStub(storePath);
        repos.setDownloader(downloader);

        //OptionCalculator calculator = new BlackScholesStub();
        //repos.setOptionCalculator(calculator);
    }

    @Test
    @Ignore
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
    @Ignore
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

        Element rowClose = tds.get(STOCK_PRICE_CLOSE.getIndex());
        Element cellClose = rowClose.getElementsByClass("c01438").first();
        assertThat(cellClose.text()).isEqualTo("160.15");

        Element rowHi = tds.get(STOCK_PRICE_Hi.getIndex());
        Element cellHi = rowHi.getElementsByClass("c01438").first();
        assertThat(cellHi.text()).isEqualTo("160.95");

        Element rowLo = tds.get(STOCK_PRICE_Lo.getIndex());
        Element cellLo = rowLo.getElementsByClass("c01438").first();
        assertThat(cellLo.text()).isEqualTo("157.2");
    }

    @Test
    @Ignore
    public void testC01408Table2() {
        Document doc = getCachedDocument();
        Elements tables = doc.getElementsByClass("c01408");

        Element table2 = tables.get(2);
        Elements rows = table2.getElementsByTag("tr");
        assertThat(rows.size()).isEqualTo(21);

        Element row1 = rows.get(5); // rows.first();
        Elements tds = row1.getElementsByTag("td");
        assertThat(tds.size()).isEqualTo(16);

        Element tickerBuy = tds.get(TICKER_BUY.getIndex());
        assertThat(tickerBuy.text()).isEqualTo("EQNR0F280");

        Element x = tds.get(X.getIndex());
        assertThat(x.text()).isEqualTo("280 280,00");
    }

    @Test
    public void testOpeningPrices_no_file() {
        File openingPricesFileName = getOpeningPricesFile();
        boolean deleteResult = openingPricesFileName.delete();
        testOpeningPrices();
    }

    @Test
    public void testOpeningPrices_already_initialized() {
        testOpeningPrices();
    }

    public void testOpeningPrices() {
        repos.initOpeningPrices();
        File openingPricesFileName = getOpeningPricesFile();
        assertThat(openingPricesFileName.exists()).isEqualTo(true);
        List<StockPrice> openingPrices = repos.getOpeningPrices();
        assertThat(openingPrices.size()).isEqualTo(3);
    }

    private File getOpeningPricesFile() {
        return new File(repos.getOpeningPricesFileName());
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
     */
}

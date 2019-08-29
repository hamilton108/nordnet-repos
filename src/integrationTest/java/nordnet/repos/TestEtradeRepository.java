package nordnet.repos;

import nordnet.downloader.DownloaderStub;
import nordnet.downloader.TickerInfo;
import nordnet.html.Util;
import oahu.financial.Derivative;
import oahu.financial.DerivativePrice;
import oahu.financial.StockPrice;
import oahu.financial.html.EtradeDownloader;
import oahu.testing.TestUtil;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.File;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;

import static nordnet.html.DerivativesEnum.*;
import static nordnet.html.DerivativesStringEnum.TABLE_CLASS;
import static nordnet.html.DerivativesStringEnum.TD_CLASS;
import static org.assertj.core.api.Assertions.assertThat;

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
    public void testC01408Table1() {
        Document doc = getDocument(new TickerInfo("EQNR"), repos);
        Elements tables = doc.getElementsByClass(TABLE_CLASS.getText());
        assertThat(tables.size()).isEqualTo(3);

        Element table1 = tables.get(TABLE_STOCK_PRICE.getIndex());
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

        Element table2 = tables.get(TABLE_DERIVATIVES.getIndex());
        Elements rows = table2.getElementsByTag("tr");
        assertThat(rows.size()).isEqualTo(19);

        Element row1 = rows.get(5); // rows.first();
        Elements tds = row1.getElementsByTag("td");
        assertThat(tds.size()).isEqualTo(16);

        Element tickerBuy = tds.get(CALL_TICKER.getIndex());
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

        Map<String,Double> openingPrices = repos.getOpeningPrices();

        double nhy = openingPrices.get("NHY");
        assertThat(nhy).isEqualTo(28.00);

        double eqnr = openingPrices.get("EQNR");
        assertThat(eqnr).isEqualTo(150.00);

        double yar = openingPrices.get("YAR");
        assertThat(yar).isEqualTo(373.40);
    }

    @Test
    public void testCreateStockPrice_oid() {
        repos.setOpeningPricesFileName("openingPrices_initialized.txt");
        repos.initOpeningPrices();
        Optional<StockPrice> price = repos.stockPrice(2);
        assertThat(price).isNotEmpty();
        price.ifPresent(s -> {
            assertThat(s.getOpn()).isEqualTo(150.00);
            assertThat(s.getHi()).isEqualTo(150.85);
            assertThat(s.getLo()).isEqualTo(149.10);
            assertThat(s.getCls()).isEqualTo(149.90);
        });
    }

    @Test
    public void testCallPutDefs() {
        Collection<Derivative> defs = repos.callPutDefs(2);
        assertThat(defs.size()).isEqualTo(19);

        String ticker = "EQNR9H30Y175";
        Optional<Derivative> def = defs.stream().filter(x -> x.getTicker().equals(ticker)).findAny();
        assertThat(def).isNotEmpty();
        def.ifPresent(s -> {
            assertThat(s.getX()).isEqualTo(175.00);
        });
    }


    @Test
    public void testParse_X_price() {
        double x = Util.parseExercisePrice("175 175,00");
        assertThat(x).isEqualTo(175.00);

        double x2 = Util.parseExercisePrice("175   175,00");
        assertThat(x2).isEqualTo(175.00);
    }

    @Test
    @Ignore
    public void testCalls() {
        Collection<DerivativePrice> calls = repos.calls(2);
        assertThat(calls.size()).isEqualTo(19);

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
}

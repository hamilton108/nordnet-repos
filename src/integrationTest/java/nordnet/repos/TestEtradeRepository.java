package nordnet.repos;

import nordnet.downloader.DownloaderStub;
import nordnet.downloader.TickerInfo;
import nordnet.html.Util;
import oahu.financial.Derivative;
import oahu.financial.DerivativePrice;
import oahu.financial.StockPrice;
import oahu.financial.html.EtradeDownloader;
import oahu.functional.Procedure2;
import oahu.functional.Procedure3;
import oahu.testing.TestUtil;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
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
import java.util.function.Consumer;

import static nordnet.html.DerivativesEnum.*;
import static nordnet.html.DerivativesStringEnum.TABLE_CLASS;
import static nordnet.html.DerivativesStringEnum.TD_CLASS;
import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
public class TestEtradeRepository {
    private static String storePath = "/home/rcs/opt/java/nordnet-repos/src/integrationTest/resources/html/derivatives";
    //private static String storePath = "c:/opt/lx/nordnet-repos/src/integrationTest/resources/html/derivatives";

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

    private Procedure3<Element,String,String> myAssert = (el, val, msg) -> {
        assertThat(el.text()).as(String.format("%s: %s",msg,val)).isEqualTo(val);
    };

    @Test
    @Ignore
    public void testTable1() {
        Document doc = getDocument(new TickerInfo("EQNR"), repos);
        Elements tables = doc.getElementsByTag("tbody");
        assertThat(tables.size()).isEqualTo(3);

        Element table1 = tables.get(TABLE_STOCK_PRICE.getIndex());
        Elements rows = table1.getElementsByTag("tr");
        assertThat(rows.size()).isEqualTo(4);

        Element row1 = rows.first();
        Elements tds = row1.getElementsByTag("td");
        assertThat(tds.size()).isEqualTo(22);


        Element rowClose = tds.get(STOCK_PRICE_CLOSE.getIndex());
        Element tdClose = Util.getTd(rowClose);
        myAssert.apply(tdClose,"153.9","Close");

        Element rowHi = tds.get(STOCK_PRICE_Hi.getIndex());
        Element tdHi = Util.getTd(rowHi);
        myAssert.apply(tdHi,"154.6","Hi");

        Element rowLo = tds.get(STOCK_PRICE_Lo.getIndex());
        Element tdLo = Util.getTd(rowLo);
        myAssert.apply(tdLo,"151.95","Lo");

    }

    @Test
    @Ignore
    public void testTable3() {
        Document doc = getDocument(new TickerInfo("EQNR"), repos);
        //Elements tables = doc.getElementsByClass(TABLE_CLASS.getText());
        Elements tables = doc.getElementsByTag("tbody");

        Element table = tables.get(TABLE_DERIVATIVES.getIndex());
        Elements rows = table.getElementsByTag("tr");
        assertThat(rows.size()).as("Row size").isEqualTo(30);

        Element row1 = rows.get(5); // rows.first();
        Elements tds = row1.getElementsByTag("td");
        assertThat(tds.size()).as("Tds size").isEqualTo(16);

        Element callTicker = tds.get(CALL_TICKER.getIndex());
        assertThat(callTicker.text()).as("Call ticker").isEqualTo("EQNR9L280");

        Element x = tds.get(X.getIndex());
        assertThat(x.text()).as("X").isEqualTo("280 280,00");
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

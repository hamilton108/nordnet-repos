package nordnet.repos;

import nordnet.downloader.DownloaderStub;
import nordnet.downloader.TickerInfo;
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
import java.util.Map;

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

        Map<String,Double> openingPrices = repos.getOpeningPrices();

        double nhy = openingPrices.get("NHY");
        assertThat(nhy).isEqualTo(28.37);

        double eqnr = openingPrices.get("EQNR");
        assertThat(eqnr).isEqualTo(150.90);

        double yar = openingPrices.get("YAR");
        assertThat(yar).isEqualTo(373.40);
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

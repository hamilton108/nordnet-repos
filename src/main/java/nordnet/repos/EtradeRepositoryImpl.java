package nordnet.repos;

import com.gargoylesoftware.htmlunit.Page;
import critterrepos.beans.StockPriceBean;
import nordnet.downloader.TickerInfo;
import oahu.dto.Tuple2;
import oahu.financial.Derivative;
import oahu.financial.DerivativePrice;
import oahu.financial.Stock;
import oahu.financial.StockPrice;
import oahu.financial.html.EtradeDownloader;
import oahu.financial.repository.EtradeRepository;
import oahu.financial.repository.StockMarketRepository;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.Optional;

public class EtradeRepositoryImpl implements EtradeRepository {
    private EtradeDownloader<Page, TickerInfo, Serializable> downloader;
    private StockMarketRepository stockMarketRepos;

    @Override
    public Optional<DerivativePrice> findDerivativePrice(Object optionInfo) {
        return Optional.empty();
    }

    @Override
    public Optional<StockPrice> stockPrice(String ticker) {
        return Optional.empty();
    }

    @Override
    public Optional<StockPrice> stockPrice(int oid) {
        return Optional.empty();
    }

    @Override
    public Collection<DerivativePrice> puts(String ticker) {
        return null;
    }

    @Override
    public Collection<DerivativePrice> puts(int oid) {
        return null;
    }

    @Override
    public Collection<DerivativePrice> calls(String ticker) {
        return null;
    }

    @Override
    public Collection<DerivativePrice> calls(int oid) {
        return null;
    }

    @Override
    public Collection<Derivative> callPutDefs(String ticker) {
        return null;
    }

    @Override
    public void setDownloadDate(LocalDate localDate) {

    }

    @Override
    public void invalidateCache() {

    }

    public void setDownloader(EtradeDownloader downloader) {
        this.downloader = downloader;
    }

    //-----------------------------------------------------------
    //-------------- Package/private methods --------------------
    //-----------------------------------------------------------
    Document getDocument(TickerInfo tickerInfo) throws IOException {
        Page page = downloader.downloadDerivatives(tickerInfo);
        return Jsoup.parse(page.getWebResponse().getContentAsString());
    }

    public void setStockMarketRepository(StockMarketRepository stockMarketRepos) {
        this.stockMarketRepos = stockMarketRepos;
    }
    Optional<StockPrice> createStockPrice(Document doc, Stock stock) {
        Element top = doc.getElementById("updatetable1");
        if (top == null) {
            return Optional.empty();
        }
        Element closeEl = top.getElementById("ju.l");
        Elements openEl = top.getElementsByAttributeValue("name","ju.op");
        Elements hiEl = top.getElementsByAttributeValue("name","ju.h");
        Elements loEl = top.getElementsByAttributeValue("name","ju.lo");
        Elements volEl = top.getElementsByAttributeValue("name","ju.vo");
        Element timeEl = doc.getElementById("toptime");
        if ((closeEl == null)
                || openEl.isEmpty()
                || hiEl.isEmpty()
                || loEl.isEmpty()
                || volEl.isEmpty()
                || timeEl == null) {
            return Optional.empty();
        }
        try {
            double close = Double.parseDouble(closeEl.text());
            double open = Double.parseDouble(openEl.text());
            double hi = Double.parseDouble(hiEl.text());
            double lo = Double.parseDouble(loEl.text());
            long vol = Long.parseLong(volEl.text().replaceAll("\\s",""));
            Tuple2<LocalDate, LocalTime> timeInfo = getTimeInfo(timeEl);
            StockPriceBean result = new StockPriceBean(timeInfo.first(), timeInfo.second(), open, hi, lo, close, vol);
            result.setStock(stock);
            return Optional.of(result);
        }
        catch (NumberFormatException ex) {
            return Optional.empty();
        }
    }
    Tuple2<LocalDate, LocalTime> getTimeInfo(Element el) {
        // fredag 23/11-2018 18:30:05
        String txt = el.text();
        String[] txts = txt.split("\\s");
        LocalDate ld = LocalDate.parse(txts[1], dateFormatter);
        LocalTime tm = LocalTime.parse(txts[2], timeFormatter);
        return new Tuple2<>(ld,tm);
    }
    private DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM-yyyy");
    private DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");
}

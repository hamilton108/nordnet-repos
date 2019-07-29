package nordnet.downloader;

import com.gargoylesoftware.htmlunit.Page;
import com.gargoylesoftware.htmlunit.WebClient;
import oahu.financial.html.EtradeDownloader;

import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;

public class DownloaderStub implements EtradeDownloader<Page, TickerInfo, Serializable> {
    private boolean javaScriptEnabled = false;
    private WebClient webClient;
    private String storePath;
    private HashMap<String,Integer> counterMap;
    private boolean applyCounter = false;

    public DownloaderStub(String storePath) {
        this();
        this.storePath = storePath;
    }
    public DownloaderStub() {
        webClient = new WebClient();
        webClient.getOptions().setJavaScriptEnabled(isJavaScriptEnabled());
    }

    private int getCounter(String ticker) {
        if (counterMap == null) {
            counterMap = new HashMap<>();
        }
        Integer counter = null;
        if (counterMap.containsKey(ticker) == false) {
            counterMap.put(ticker,1);
            counter = 1;
        }
        else {
            counter = counterMap.get(ticker) + 1;
            counterMap.put(ticker,counter);
        }

        return counter;
    }

    //region Private Methods
    private String tickerUrl(String ticker) {
        if (applyCounter == true) {
            return String.format("file:///%s/%s-%d.html", storePath, ticker, getCounter(ticker));
        }
        else {
            return String.format("file:///%s/%s.html", storePath, ticker);
        }
    }
    //endregion Private Methods

    @Override
    public Page downloadDerivatives() throws IOException {
        return null;
    }
    @Override
    public Page downloadDerivatives(TickerInfo tickerInfo) throws IOException {
        return webClient.getPage(tickerUrl(tickerInfo.getTicker()));
    }

    @Override
    public Page downloadIndex(String stockIndex) throws IOException {
        return null;
    }
    //region interface EtradeDownloader
    /*


    @Override
    public Page downloadPaperHistory(String ticker) throws IOException {
        return null;
    }

    @Override
    public Page downloadDepth(String ticker) throws IOException {
        return null;
    }

    @Override
    public Page downloadPurchases(String ticker) throws IOException {
        return null;
    }
     */

    //endregion interface EtradeDownloader

    //region Properties
    boolean isJavaScriptEnabled() {
        return javaScriptEnabled;
    }

    public void setJavaScriptEnabled(boolean javaScriptEnabled) {
        this.javaScriptEnabled = javaScriptEnabled;
    }

    public void setStorePath(String storePath) {
        this.storePath = storePath;
    }

    public void setApplyCounter(boolean applyCounter) {
        this.applyCounter = applyCounter;
    }
    //endregion Properties
}

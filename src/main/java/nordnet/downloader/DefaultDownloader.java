package nordnet.downloader;

import com.gargoylesoftware.htmlunit.Page;
import oahu.financial.html.EtradeDownloader;
import oahu.financial.html.WebClientManager;

import java.io.IOException;
import java.io.Serializable;

public class DefaultDownloader implements EtradeDownloader<Page, TickerInfo, Serializable> {
    private WebClientManager<Page> webClientManager;

    @Override
    public Page downloadDerivatives() throws IOException {
        return webClientManager.getPage(tickerUrl("EQNR"));
    }

    @Override
    public Page downloadDerivatives(TickerInfo tickerInfo) throws IOException {
        return null;
    }

    @Override
    public Page downloadIndex(String stockIndex) throws IOException {
        return null;
    }

    private String tickerUrl(String ticker) {
        return String.format("file:///home/rcs/opt/java/harborview/feed/%s.html", ticker);
    }
}

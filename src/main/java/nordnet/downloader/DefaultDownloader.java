package nordnet.downloader;

import com.gargoylesoftware.htmlunit.Page;
import oahu.financial.html.EtradeDownloader;
import oahu.financial.html.WebClientManager;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class DefaultDownloader implements EtradeDownloader<PageInfo, TickerInfo, Serializable> {
    private final WebClientManager<Page> webClientManager;
    private final NordnetURL<URLInfo> nordnetURL;
    private Consumer<PageInfo> onPageDownloaded;

    public DefaultDownloader(String host, int port, int db) {
        this.webClientManager = new WebClientManagerImpl();
        this.nordnetURL = new NordnetRedis(host,port,db);
    }

    @Override
    public List<PageInfo> downloadDerivatives(TickerInfo tickerInfo) {
        List<PageInfo> result = new ArrayList<>();

        var urls = nordnetURL.url(tickerInfo.getTicker());

        for (var url : urls) {
            var page = webClientManager.getPage(url.getUrl());
            var pageInfo = new PageInfo(page, tickerInfo, url.getUnixTime());
            result.add(pageInfo);
            if (onPageDownloaded != null) {
                onPageDownloaded.accept(pageInfo); 
            }
        }

        return result;
    }

    public void setOnPageDownloaded(Consumer<PageInfo> onPageDownloaded) {
        this.onPageDownloaded = onPageDownloaded;
    }

    /*
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
     */



    /*
    @Autowired
    public void setWebClientManager(WebClientManager<Page> webClientManager) {
        this.webClientManager = webClientManager;
    }

     */
}

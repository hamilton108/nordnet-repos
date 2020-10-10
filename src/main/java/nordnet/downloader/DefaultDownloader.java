package nordnet.downloader;

import com.gargoylesoftware.htmlunit.Page;
import oahu.financial.html.EtradeDownloader;
import oahu.financial.html.WebClientManager;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class DefaultDownloader implements EtradeDownloader<PageInfo, TickerInfo, Serializable> {
    private final WebClientManager<Page> webClientManager;
    private final NordnetURL<URLInfo> nordnetURL;

    public DefaultDownloader(String host, int port, int db) {
        this.webClientManager = new WebClientManagerImpl();
        this.nordnetURL = new NordnetURLRedis(host,port,db);
    }

    @Override
    public List<PageInfo> downloadDerivatives(TickerInfo tickerInfo) {
        List<PageInfo> result = new ArrayList<>();

        var urls = nordnetURL.url(tickerInfo.getTicker());

        for (var url : urls) {
            System.out.println(url);
            var page = webClientManager.getPage(url.getUrl());
            result.add(new PageInfo(page, url.getUnixTime()));
        }

        return result;
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

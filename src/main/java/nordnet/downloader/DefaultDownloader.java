package nordnet.downloader;

import com.gargoylesoftware.htmlunit.Page;
import oahu.financial.html.EtradeDownloader;

import java.io.IOException;
import java.io.Serializable;

public class DefaultDownloader implements EtradeDownloader<Page, TickerInfo, Serializable> {
    @Override
    public Page downloadDerivatives() throws IOException {
        return null;
    }

    @Override
    public Page downloadDerivatives(TickerInfo tickerInfo) throws IOException {
        return null;
    }

    @Override
    public Page downloadIndex(String stockIndex) throws IOException {
        return null;
    }
}

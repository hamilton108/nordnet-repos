package nordnet.downloader;

import com.gargoylesoftware.htmlunit.Page;

public class PageInfo {
    private final Page page;
    private final TickerInfo tickerInfo;
    private final String unixTime;
    public PageInfo(Page page, TickerInfo tickerInfo, String unixTime) {
        this.page = page;
        this.tickerInfo = tickerInfo;
        this.unixTime = unixTime;
    }

    public Page getPage() {
        return page;
    }

    public String getUnixTime() {
        return unixTime;
    }

    public TickerInfo getTickerInfo() {
        return tickerInfo;
    }
}

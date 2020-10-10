package nordnet.downloader;

import com.gargoylesoftware.htmlunit.Page;

public class PageInfo {
    private final Page page;
    private final String unixTime;
    public PageInfo(Page page, String unixTime) {
        this.page = page;
        this.unixTime = unixTime;
    }

    public Page getPage() {
        return page;
    }

    public String getUnixTime() {
        return unixTime;
    }
}

package nordnet.downloader;

public class URLInfo {
    private final String url;
    private final String unixTime;

    public URLInfo(String url, String unixTime) {
        this.url = url;
        this.unixTime = unixTime;
    }

    public String getUrl() {
        return url;
    }

    public String getUnixTime() {
        return unixTime;
    }
}

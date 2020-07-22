package nordnet.downloader;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.List;

@RunWith(SpringRunner.class)
public class TestDownloader {
    private NordnetURLRedis nordnetURL;
    @Before
    public void init() {
        this.nordnetURL = new NordnetURLRedis("172.20.1.2", 5);
    }

    @Test
    public void test_nordnet_url() {
        var urlFor = nordnetURL.urlFor("NHY", "1600380000000", null);
        var urlForStr = urlFor.toString();
        System.out.println(urlForStr);
        assertThat(urlForStr)
                .isEqualTo("https://www.nordnet.no/market/options?currency=NOK&underlyingSymbol=NHY&expireDate=1600380000000");
    }

    @Test
    public void test_nordnet_redis_tickers_1() {
        var urls = nordnetURL.url("YAR", LocalDate.of(2020,7,1));
        assertThat(urls.size()).isEqualTo(6);
    }
    @Test
    public void test_nordnet_redis_tickers_2() {
        var urls = nordnetURL.url("NHY", LocalDate.of(2020,8,1));
        assertThat(urls.size()).isEqualTo(8);
    }
}

package nordnet.downloader;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
public class TestDownloader {
    private NordnetRedis nordnetURL;
    @Before
    public void init() {
        this.nordnetURL = new NordnetRedis("172.20.1.2", 5);
    }

    @Test
    public void test_nordnet_url() {
        var urlFor = nordnetURL.urlFor("NHY", "1600380000000");
        var urlForStr = urlFor.toString();
        System.out.println(urlForStr);
        assertThat(urlForStr)
                .isEqualTo("https://www.nordnet.no/market/options?currency=NOK&underlyingSymbol=NHY&expireDate=1600380000000");
    }

    @Test
    public void test_nordnet_redis_urls() {
        var urls = nordnetURL.url("YAR", LocalDate.of(2020,7,1));
        assertThat(urls.size()).isEqualTo(6);

        urls = nordnetURL.url("YAR", LocalDate.of(2021,4,16));
        assertThat(urls.size()).isEqualTo(6);

        urls = nordnetURL.url("YAR", LocalDate.of(2021,5,21));
        assertThat(urls.size()).isEqualTo(5);

        urls = nordnetURL.url("YAR", LocalDate.of(2021,5,22));
        assertThat(urls.size()).isEqualTo(4);
    }
}

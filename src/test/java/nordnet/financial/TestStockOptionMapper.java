package nordnet.financial;

import nordnet.downloader.WebClientManagerBasicAuth;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
public class TestStockOptionMapper {

    @Ignore
    @Test
    public void test_map_ticker_to_date() {
        var client = new WebClientManagerBasicAuth();
        //var result = client.encodeUserPwd("hamilton108", "S6sM&dn2");
        var result = client.getPage("https://www.nordnet.no/overview");
        System.out.println(result);
    }
}

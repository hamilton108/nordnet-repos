package nordnet.downloader;

import com.gargoylesoftware.htmlunit.Page;
import com.gargoylesoftware.htmlunit.WebClient;
import oahu.financial.html.WebClientManager;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class WebClientManagerImpl implements WebClientManager<Page> {
    private WebClient webClient;

    @Override
    public Page getPage(String url) {
        return null;
    }

    @Override
    public Optional<Page> logout() {
        return Optional.empty();
    }
    private WebClient getWebClient() {
        if (webClient == null) {
            webClient = new WebClient();
            webClient.getOptions().setJavaScriptEnabled(false);
        }
        return webClient;
    }
}

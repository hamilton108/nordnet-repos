package nordnet.downloader;

import oahu.financial.html.WebClientManager;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Base64;
import java.util.Optional;

public class WebClientManagerBasicAuth implements WebClientManager<HttpResponse<String>> {

    // https://www.nordnet.no/api/2/login

    HttpClient httpClient;
    //Authenticator authenticator;
    String base64encodedUserPwd;


    public WebClientManagerBasicAuth(String userId, String pwd) {
        /*
        authenticator = new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(userId, pwd.toCharArray());
            }
        };
         */
        base64encodedUserPwd = encodeUserPwd(userId, pwd);
    }
    public WebClientManagerBasicAuth() {
        this("hamilton108", "S6sM&dn2");
    }

    @Override
    public HttpResponse<String> getPage(String url) {
        try {
            var uri = new URI(url);
            var request =
                    HttpRequest.newBuilder()
                            .GET()
                            .setHeader("Authorization", base64encodedUserPwd)
                            .uri(uri)
                            .build();
            return getWebClient().send(request, HttpResponse.BodyHandlers.ofString());
        } catch (IOException | InterruptedException | URISyntaxException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public Optional logout() {
        return Optional.empty();
    }

    public String encodeUserPwd(String user, String pwd) {
        var result = Base64.getUrlEncoder().encodeToString(String.format("Basic %s:%s", user, pwd).getBytes());
        // QmFzaWMgaGFtaWx0b24xMDg6UzZzTSZkbjI=
        return result;
    }
    private HttpClient getWebClient() {
        if (httpClient == null) {
            httpClient = HttpClient.newBuilder()
                    .version(HttpClient.Version.HTTP_2)
                    .followRedirects(HttpClient.Redirect.NORMAL)
                    .connectTimeout(Duration.ofSeconds(20))
                    //.proxy(ProxySelector.of(new InetSocketAddress("proxy.yourcompany.com", 80)))
                    //.authenticator(authenticator)
                    .build();
        }
        return httpClient;
    }

}

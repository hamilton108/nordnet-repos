package nordnet.demo;

import com.gargoylesoftware.htmlunit.Page;
import com.gargoylesoftware.htmlunit.WebClient;
import nordnet.downloader.TickerInfo;
import oahu.financial.html.EtradeDownloader;

import java.io.IOException;
import java.io.Serializable;

public class Demo1 {
   class MyDownloader implements EtradeDownloader<Page, TickerInfo, Serializable> {
      private boolean javaScriptEnabled = false;
      private WebClient webClient;
      private String storePath = "c:/opt/lx/nordnet-repos/src/integrationTest/resources/html/derivatives";

      public MyDownloader() {
         webClient = new WebClient();
      }

      @Override
      public Page downloadDerivatives() throws IOException {
         return null;
      }

      @Override
      public Page downloadDerivatives(TickerInfo tickerInfo) throws IOException {
         return webClient.getPage(tickerUrl(tickerInfo.getTicker()));
      }

      @Override
      public Page downloadIndex(String stockIndex) throws IOException {
         return null;
      }

      private String tickerUrl(String ticker) {
         return String.format("file:///%s/%s.html", storePath, ticker);
      }
   }

   public Page ax() {
      MyDownloader myDownloader = new MyDownloader();
      try {
         return myDownloader.downloadDerivatives(new TickerInfo("EQNR"));
      } catch (IOException e) {
         e.printStackTrace();
         return null;
      }
   }
}

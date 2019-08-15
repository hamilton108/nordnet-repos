package nordnet.demo;

import com.gargoylesoftware.htmlunit.Page;
import com.gargoylesoftware.htmlunit.WebClient;
import nordnet.downloader.TickerInfo;
import oahu.financial.html.EtradeDownloader;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.io.Serializable;

public class Demo1 {
   class MyDownloader implements EtradeDownloader<Page, TickerInfo, Serializable> {
      private boolean javaScriptEnabled = false;
      private WebClient webClient;
      //private String storePath = "c:/opt/lx/nordnet-repos/src/integrationTest/resources/html/derivatives";
      private String storePath = "/home/rcs/opt/java/nordnet-repos/src/integrationTest/resources/html/derivatives";
      //private String storePath = "/home/rcs/opt/java/nordnet-repos/src/integrationTest/resources/";

      public MyDownloader() {
         webClient = new WebClient();
         webClient.getOptions().setJavaScriptEnabled(false);
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

   private Page fetchPage(String ticker) {
      MyDownloader myDownloader = new MyDownloader();
      try {
         return myDownloader.downloadDerivatives(new TickerInfo(ticker));
      } catch (IOException e) {
         e.printStackTrace();
         return null;
      }
   }
   public Document getDocument() throws IOException {
      Page page = fetchPage("EQNR");
      //Page page = fetchPage("EQNR-2020-19-06");
      assert page != null;
      return Jsoup.parse(page.getWebResponse().getContentAsString());
   }

   public Elements getForClass(Document doc, String htmlClass) {
      return doc.getElementsByClass(htmlClass);
   }

   public Elements getEl() {
      try {
         Document doc = getDocument();
         return getForClass(doc, "c01408");
      } catch (IOException e) {
         e.printStackTrace();
         return null;
      }
   }
}

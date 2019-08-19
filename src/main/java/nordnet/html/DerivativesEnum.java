package nordnet.html;

import org.w3c.dom.DOMError;

public enum DerivativesEnum {
   STOCK_PRICE_LATEST(8),
   TICKER_BUY(2),
   X(7);

   private final int index;

   DerivativesEnum(int index) {
      this.index = index;
   }

   public int getIndex() {
      return index;
   }
}

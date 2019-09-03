package nordnet.html;

public enum DerivativesEnum {
   TABLE_STOCK_PRICE(0),
   TABLE_DERIVATIVES(2),

   SELECT_INPUT_DATE(2),

   STOCK_PRICE_CLOSE(8),
   STOCK_PRICE_Hi(11),
   STOCK_PRICE_Lo(12),

   CALL_TICKER(2),
   PUT_TICKER(3),
   X(7);

   private final int index;

   DerivativesEnum(int index) {
      this.index = index;
   }

   public int getIndex() {
      return index;
   }

}

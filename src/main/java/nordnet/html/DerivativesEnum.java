package nordnet.html;

public enum DerivativesEnum {
   TABLE_STOCK_PRICE(0),
   TABLE_DERIVATIVES(2),

   SELECT_INPUT_DATE(2),

   STOCK_PRICE_CLOSE(8),
   STOCK_PRICE_Hi(11),
   STOCK_PRICE_Lo(12),

   CALL_TICKER(2),
   CALL_BID(4),
   CALL_ASK(5),
   PUT_TICKER(12),
   PUT_BID(9),
   PUT_ASK(10),
   X(7);

   private final int index;

   DerivativesEnum(int index) {
      this.index = index;
   }

   public int getIndex() {
      return index;
   }

}

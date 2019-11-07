package support.skipjack.adoi.converter;

import org.matrix.androidsdk.MXDataHandler;

import java.util.List;

import support.skipjack.adoi.matrix.MatrixService;

public abstract class BaseConverter<From,To> {
   public MXDataHandler mxDataHandler;

   public BaseConverter() {
      mxDataHandler = MatrixService.get().mxSession.getDataHandler();
   }

   public abstract To convert(From from);
   public abstract List<To> convert(List<From> fromList);
   public abstract From revert(To to);
   public abstract List<From> revert(List<To> toList);
}

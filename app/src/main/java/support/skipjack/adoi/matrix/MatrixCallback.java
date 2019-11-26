package support.skipjack.adoi.matrix;

import org.matrix.androidsdk.core.callback.ApiCallback;
import org.matrix.androidsdk.core.model.MatrixError;

public abstract class MatrixCallback<T> implements ApiCallback<T> {
    public abstract void onAPISuccess(T data);
    public abstract void onAPIFailure(String errorMessage);

    public MatrixError matrixError;
    public Exception exception;
    public ErrorType errorType;

    public enum ErrorType{
        UNEXPECTED_ERROR,
        NETWORK_ERROR,
        MATRIX_ERROR
    }
    public MatrixCallback() {
    }

    @Override
    public void onNetworkError(Exception e) {
       e.printStackTrace();
       MatrixHelper.LOG("onNetworkError : "+e.getLocalizedMessage());

       matrixError = null;
       exception = e;
       errorType = ErrorType.NETWORK_ERROR;
       onAPIFailure(MatrixMessage.ERROR_NO_NETWORK.getMessage());

//        UnrecognizedCertificateException unrecCertEx = CertUtil.getCertificateException(e);
//        if (unrecCertEx != null) {
//            final Fingerprint fingerprint = unrecCertEx.getFingerprint();
//
//            UnrecognizedCertHandler.show(MatrixService.get().homeServerConfig, fingerprint, false, new UnrecognizedCertHandler.Callback() {
//                @Override
//                public void onAccept() {
//                }
//
//                @Override
//                public void onIgnore() {
//                    MatrixHelper.LOG("onIgnore");
//                }
//
//                @Override
//                public void onReject() {
//                    MatrixHelper.LOG("onReject");
//                }
//            });
//
//        }
//         onAPIFailure(MatrixMessage.ERROR_NO_NETWORK.getMessage());

    }

    @Override
    public void onMatrixError(MatrixError matrixError) {
        MatrixHelper.LOG("MatrixError : status = "+matrixError.mStatus+", " +
                "errcode = "+matrixError.errcode+", errorBody = "+matrixError.mErrorBodyAsString);

        matrixError = matrixError;
        exception = null;
        errorType = ErrorType.MATRIX_ERROR;
        onAPIFailure(matrixError.error);
    }

    @Override
    public void onUnexpectedError(Exception e) {
        e.printStackTrace();
        MatrixHelper.LOG("onUnexpectedError : "+e.getLocalizedMessage());

        matrixError = null;
        exception = e;
        errorType = ErrorType.UNEXPECTED_ERROR;
        onAPIFailure(e.getLocalizedMessage());
    }

    @Override
    public void onSuccess(T t) {
//        MatrixHelper.LOG("onSuccess : "+ new Gson().toJson(t));
        onAPISuccess(t);
    }
}

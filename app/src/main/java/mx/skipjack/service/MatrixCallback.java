package mx.skipjack.service;

import com.google.gson.Gson;

import org.matrix.androidsdk.core.callback.ApiCallback;
import org.matrix.androidsdk.core.model.MatrixError;

public abstract class MatrixCallback<T> implements ApiCallback<T> {
    public abstract void onAPISuccess(T data);
    public abstract void onAPIFailure(String errorMessage);
    public abstract void onAPIMxError(int status, String errorcode, String errorBody);


    public MatrixCallback() {
    }

    @Override
    public void onNetworkError(Exception e) {
        e.printStackTrace();
        MatrixUtility.LOG("onNetworkError : "+e.getLocalizedMessage());
        onAPIFailure(MatrixMessage.ERROR_NO_NETWORK.getMessage());
    }

    @Override
    public void onMatrixError(MatrixError matrixError) {
        MatrixUtility.LOG("MatrixError : status = "+matrixError.mStatus+", " +
                "errcode = "+matrixError.errcode+", errorBody = "+matrixError.mErrorBodyAsString);
        onAPIMxError(matrixError.mStatus,matrixError.errcode,matrixError.mErrorBodyAsString);
    }

    @Override
    public void onUnexpectedError(Exception e) {
        e.printStackTrace();
        MatrixUtility.LOG("onUnexpectedError : "+e.getLocalizedMessage());
        onAPIFailure(e.getLocalizedMessage());
    }

    @Override
    public void onSuccess(T t) {
        MatrixUtility.LOG("onSuccess : "+ new Gson().toJson(t));
        onAPISuccess(t);
    }
}

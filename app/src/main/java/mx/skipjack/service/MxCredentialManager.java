package mx.skipjack.service;

import org.matrix.androidsdk.core.JsonUtils;
import org.matrix.androidsdk.core.model.MatrixError;
import org.matrix.androidsdk.rest.client.LoginRestClient;
import org.matrix.androidsdk.rest.model.login.AuthParams;
import org.matrix.androidsdk.rest.model.login.AuthParamsThreePid;
import org.matrix.androidsdk.rest.model.login.Credentials;
import org.matrix.androidsdk.rest.model.login.LoginFlow;
import org.matrix.androidsdk.rest.model.login.RegistrationFlowResponse;
import org.matrix.androidsdk.rest.model.login.RegistrationParams;
import org.matrix.androidsdk.rest.model.pid.ThreePid;

public class MxCredentialManager {
    private RegistrationFlowResponse registrationFlowResponse;

    private MatrixCallback<Credentials> callback;
    public MxCredentialManager(MatrixCallback<Credentials> callback) {
        this.callback = callback;
    }

    private MatrixCallback<Credentials> toCreateSessionCallback =
            new MatrixCallback<Credentials>() {
                @Override
                public void onAPISuccess(Credentials data) {
//                    MatrixService.get().mxSession.startEventStream(data.accessToken);
                    callback.onAPISuccess(data);
                }

                @Override
                public void onAPIFailure(String errorMessage) {
                    callback.onAPIFailure(errorMessage);
                }

                @Override
                public void onAPIMxError(int status, String errorcode, String errorBody) {
                    callback.onAPIMxError(status,errorcode,errorBody);
                }
            };

    /**
     * LOGIN
     * */
    public void loginUsingUsername(String username, String password) {
        MatrixService.get().loginRestClient.loginWithUser( username,password,toCreateSessionCallback);
    }

    public void loginUsingEmail(String email, String password){
        MatrixService.get().loginRestClient.loginWith3Pid(ThreePid.MEDIUM_EMAIL,
                email,password,toCreateSessionCallback);
    }

    public void loginUsingPhone(String phone, String password){
        MatrixService.get().loginRestClient.loginWith3Pid(ThreePid.MEDIUM_MSISDN,
                phone,password,toCreateSessionCallback);
    }

    /**
     * REGISTER
     * */
    private interface CheckUserCallback{
        void onCheckUserExist(boolean isExist);
    }

    private interface RegisterCallback{
        void onRegisterUsername(boolean success, Credentials credentials, String errorMessage);
    }

    public void register(String username, String email, String password){
        checkUserBeforeRegistration(username, new CheckUserCallback() {
            @Override
            public void onCheckUserExist(boolean isExist) {
                if (isExist){
                    callback.onAPIFailure(MatrixMessage.ERROR_USER_IN_USE.getMessage());
                }else {
                    registerUsername(username, password, new RegisterCallback() {
                        @Override
                        public void onRegisterUsername(boolean success, Credentials credentials, String errorMessage) {
                            if (success){
                                callback.onAPISuccess(credentials);
                                //bind email
                                bindEmail(email);
                            }else {
                                callback.onAPIFailure(errorMessage);
                            }
                        }
                    });
                }
            }
        });
    }

    private void checkUserBeforeRegistration(String username, CheckUserCallback callback){
        RegistrationParams params = new RegistrationParams();
        params.username = username;
        MatrixService.get().loginRestClient.register(params, new MatrixCallback<Credentials>() {
            @Override
            public void onAPISuccess(Credentials data) {
                callback.onCheckUserExist(true);
            }

            @Override
            public void onAPIFailure(String errorMessage) {
                callback.onCheckUserExist(false);
            }

            @Override
            public void onAPIMxError(int status, String errorcode, String errorBody) {
                if (status == 401) {
                    registrationFlowResponse = JsonUtils.toRegistrationFlowResponse(errorBody);
                    callback.onCheckUserExist(false);
                } else
                    callback.onCheckUserExist(true);

            }
        });
    }
   private void registerUsername(String username, String password, RegisterCallback callback){
        AuthParams authParams = new AuthParams(LoginRestClient.LOGIN_FLOW_TYPE_DUMMY);
        authParams.session = registrationFlowResponse.session;

        RegistrationParams params = new RegistrationParams();
        params.username = username;
        params.password = password;
        params.initial_device_display_name = username;
        params.auth = authParams;
        params.bind_email = true;

        MatrixService.get().loginRestClient.register(params, new MatrixCallback<Credentials>() {
            @Override
            public void onAPISuccess(Credentials data) {
                callback.onRegisterUsername(true,data,null);
            }

            @Override
            public void onAPIFailure(String errorMessage) {
                callback.onRegisterUsername(false,null, errorMessage);
            }

            @Override
            public void onAPIMxError(int status, String errorcode, String errorBody) {
                callback.onRegisterUsername(false,null, errorBody);
            }
        });

    }
    private void  bindEmail(String email){
        boolean canBindEmail = false;
        for (LoginFlow flow: registrationFlowResponse.flows){
            if (flow.stages.contains(LoginRestClient.LOGIN_FLOW_TYPE_EMAIL_IDENTITY))
                canBindEmail = true;
        }

        if (canBindEmail){
            ThreePid pidEmail = new ThreePid(email, ThreePid.MEDIUM_EMAIL);
            String nextLink = "https://riot.im/app"+  "/#/register?client_secret=" + pidEmail.clientSecret;
            nextLink += "&hs_url=" + MatrixService.get().homeServerConfig.getHomeserverUri().toString();
            nextLink += "&is_url=" + MatrixService.get().homeServerConfig.getIdentityServerUri().toString();
            nextLink += "&session_id=" + registrationFlowResponse.session;

            if (pidEmail.sid == null || pidEmail.sid.isEmpty()){
                pidEmail.requestEmailValidationToken(MatrixService.get().profileRestClient, nextLink,
                    true, new MatrixCallback<Void>() {
                        @Override
                        public void onAPISuccess(Void data) {
                            registerEmail(pidEmail);
                        }

                        @Override
                        public void onAPIFailure(String errorMessage) {
                            callback.onAPIFailure(errorMessage);
                        }

                        @Override
                        public void onAPIMxError(int status, String errorcode, String errorBody) {
                            if (errorcode.equals(MatrixError.THREEPID_IN_USE)){
                                callback.onAPIFailure(MatrixMessage.ERROR_EMAIL_IN_USE.getMessage());
                            }else {
                                callback.onAPIFailure(MatrixMessage.ERROR_VERIFYING_EMAIL.getMessage());
                            }
                        }
                    });
            }
        }else
            callback.onAPIFailure(MatrixMessage.ERROR_UNAUTHORIZE_EMAIL_BIND.getMessage());

    }

    private void registerEmail(ThreePid pid){
        AuthParamsThreePid authParams = new AuthParamsThreePid(LoginRestClient.LOGIN_FLOW_TYPE_EMAIL_IDENTITY);
        authParams.threePidCredentials.clientSecret = pid.clientSecret;
        authParams.threePidCredentials.idServer = MatrixService.get().homeServerConfig.getHomeserverUri().toString();
        authParams.threePidCredentials.sid = pid.sid;

        RegistrationParams params = new RegistrationParams();
        params.auth = authParams;
        params.auth.session = registrationFlowResponse.session;

        MatrixService.get().loginRestClient.register(params,toCreateSessionCallback);
    }

}

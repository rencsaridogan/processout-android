package com.processout.processoutexample;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.google.gson.Gson;
import com.processout.processout_sdk.AlternativeGateway;
import com.processout.processout_sdk.Card;
import com.processout.processout_sdk.ListAlternativeMethodsCallback;
import com.processout.processout_sdk.ProcessOut;
import com.processout.processout_sdk.SDKEPhemPubKey;
import com.processout.processout_sdk.ThreeDSFingerprintResponse;
import com.processout.processout_sdk.ThreeDSGatewayRequest;
import com.processout.processout_sdk.ThreeDSHandler;
import com.processout.processout_sdk.TokenCallback;

import java.util.ArrayList;
import java.util.Map;

import com.adyen.threeds2.AuthenticationRequestParameters;
import com.adyen.threeds2.ChallengeStatusReceiver;
import com.adyen.threeds2.CompletionEvent;
import com.adyen.threeds2.ProtocolErrorEvent;
import com.adyen.threeds2.RuntimeErrorEvent;
import com.adyen.threeds2.ThreeDS2Service;
import com.adyen.threeds2.Transaction;
import com.adyen.threeds2.exception.SDKAlreadyInitializedException;
import com.adyen.threeds2.exception.SDKNotInitializedException;
import com.adyen.threeds2.parameters.ChallengeParameters;
import com.adyen.threeds2.parameters.ConfigParameters;
import com.adyen.threeds2.util.AdyenConfigParameters;


public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Intent intent = getIntent();
        Uri data = intent.getData();
        if (data == null)
            this.initiatePayment();
        else
            Log.d("PROCESSOUT", "TOKEN=" + data.getQueryParameter("token"));
    }

    public void initiatePayment() {
        final ProcessOut p = new ProcessOut(this, "proj_3RJagzMt7WihKaqHBeM51IpuwWkrhQuC");
        Card c = new Card("4212345678901245", 10, 20, "737");
        p.tokenize(c, new TokenCallback() {
            @Override
            public void onError(Exception error) {

            }

            @Override
            public void onSuccess(String token) {
                ThreeDSHandler handler = new ThreeDSHandler() {
                    private Transaction mTransaction;

                    @Override
                    public void doFingerprint(Map<String, String> directoryServerData, DoFingerprintCallback callback) {
                        ConfigParameters config = new AdyenConfigParameters.Builder("F013371337", "eyJrdHkiOiJSU0EiLCJlIjoiQVFBQiIsIm4iOiI4VFBxZkFOWk4xSUEzcHFuMkdhUVZjZ1g4LUpWZ1Y0M2diWURtYmdTY0N5SkVSN3lPWEJqQmQyaTBEcVFBQWpVUVBXVUxZU1FsRFRKYm91bVB1aXVoeVMxUHN2NTM4UHBRRnEySkNaSERkaV85WThVZG9hbmlrU095c2NHQWtBVmJJWHA5cnVOSm1wTTBwZ0s5VGxJSWVHYlE3ZEJaR01OQVJLQXRKeTY3dVlvbVpXV0ZBbWpwM2d4SDVzNzdCR2xkaE9RUVlQTFdybDdyS0pLQlUwNm1tZlktUDNpazk5MmtPUTNEak02bHR2WmNvLThET2RCR0RKYmdWRGFmb29LUnVNd2NUTXhDdTRWYWpyNmQyZkppVXlqNUYzcVBrYng4WDl6a1c3UmlxVno2SU1qdE54NzZicmg3aU9Vd2JiWmoxYWF6VG1GQ2xEb0dyY2JxOV80Nnc9PSJ9").build();
                        try {
                            ThreeDS2Service.INSTANCE.initialize(/*Activity*/ MainActivity.this, config, null, null);
                            mTransaction = ThreeDS2Service.INSTANCE.createTransaction(null, null);
                            AuthenticationRequestParameters t = mTransaction.getAuthenticationRequestParameters();
                            ThreeDSFingerprintResponse gatewayRequest = new ThreeDSFingerprintResponse(t.getDeviceData(), t.getSDKAppID(), new Gson().fromJson(t.getSDKEphemeralPublicKey(), SDKEPhemPubKey.class), t.getSDKReferenceNumber(), t.getSDKTransactionID());
                            callback.continueCallback(gatewayRequest);
                        } catch (SDKAlreadyInitializedException e) {
                            e.printStackTrace();
                        } catch (SDKNotInitializedException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void doChallenge(ThreeDSGatewayRequest authentificationData, final DoChallengeCallback callback) {

                        ChallengeParameters challengeParameters = new ChallengeParameters();
                        challengeParameters.set3DSServerTransactionID(authentificationData.getThreeDSServerTransID());
                        challengeParameters.setAcsTransactionID(authentificationData.getAcsTransID());
                        challengeParameters.setAcsRefNumber(authentificationData.getAcsReferenceNumber());
                        challengeParameters.setAcsSignedContent(authentificationData.getAcsSignedContent());

                        mTransaction.doChallenge(/*Activity*/ MainActivity.this, challengeParameters, new ChallengeStatusReceiver() {
                            @Override
                            public void completed(CompletionEvent completionEvent) {
                                callback.success();
                            }

                            @Override
                            public void cancelled() {
                                // Cancelled by the user or the App.
                                callback.error();
                            }

                            @Override
                            public void timedout() {
                                // The user didn't submit the challenge within the given time, 5 minutes in this case.
                                callback.error();
                            }

                            @Override
                            public void protocolError(ProtocolErrorEvent protocolErrorEvent) {
                                // An error occurred.
                                callback.error();
                            }

                            @Override
                            public void runtimeError(RuntimeErrorEvent runtimeErrorEvent) {
                                // An error occurred.
                                callback.error();
                            }
                        }, 5);
                    }

                    @Override
                    public void onSuccess(String invoiceId) {
                        Log.d("PROCESSOUT", "SUCCESS:" + invoiceId);
                    }

                    @Override
                    public void onError(Exception error) {
                        Log.e("PROCESSOUT", error.toString());
                    }
                };
                p.makeCardPayment("iv_QnFtTzhH8iFhmz81bhcGkmD4TIySwrJ3", token, handler);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}

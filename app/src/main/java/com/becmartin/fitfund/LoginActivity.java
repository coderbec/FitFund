package com.becmartin.fitfund;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.apache.http.Header;
import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HTTP;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;


public class LoginActivity extends ActionBarActivity {

    private static final String TASKS_URL = "http://172.16.20.147:8000/accounts/api-token-auth/";

    private AsyncHttpClient client;

    protected EditText mUsername;
    protected EditText mPassword;
    protected Button mLoginButton;
    private SharedPreferences mPreferences;

    protected TextView mSignUpTextView;

    public String TAG = this.getClass().getSimpleName();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);

        super.onCreate(savedInstanceState);


        setContentView(R.layout.activity_login);


        mPreferences = getSharedPreferences("CurrentUser", MODE_PRIVATE);


        mSignUpTextView = (TextView)findViewById(R.id.signUpText);
        mSignUpTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, SignUpActivity.class);
                startActivity(intent);
            }
        });

        mUsername = (EditText)findViewById(R.id.username);
        mPassword = (EditText)findViewById(R.id.password);
        mLoginButton = (Button)findViewById(R.id.buttonLogin);
        mLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = mUsername.getText().toString();
                String password = mPassword.getText().toString();

                username = username.trim();
                password = password.trim();

                if (username.isEmpty() || password.isEmpty()) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
                    builder.setMessage("Please enter all of the required details")
                            .setTitle("Sorry!")
                            .setPositiveButton(android.R.string.ok, null);
                    AlertDialog dialog = builder.create();
                    dialog.show();
                }
                else {
                    // Login
                    setProgressBarIndeterminateVisibility(true);

                    LoginUser(username, password);
                }
            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_login, menu);
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


    private void LoginUser(String username, String password) {
        client = new AsyncHttpClient();


        // create the JSONobject
        JSONObject jsonUser = new JSONObject();

        try {

            jsonUser.put("username", username);
            jsonUser.put("password", password);
        } catch (JSONException e) {
            e.printStackTrace();
        }



        StringEntity se = null;
        try {
            se = new StringEntity(jsonUser.toString());
            se.setContentEncoding(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));
            Log.d("JSON", jsonUser.toString());
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return;
        }

        client.post(null, TASKS_URL, se, "application/json", new JsonHttpResponseHandler() {

            @Override
            public void onSuccess(int i, Header[] headers, JSONObject response) {

                Log.d("HTTP", "onSuccess: " + response.toString());
                try {
                    if (response!=null) {
                        // everything is ok
                        SharedPreferences.Editor editor = mPreferences.edit();
                        // save the returned auth_token into
                        // the SharedPreferences
                        editor.putString("token", response.getString("token"));
                        editor.commit();

                        Toast.makeText(LoginActivity.this, "Logged In", Toast.LENGTH_LONG).show();
                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                        startActivity(intent);
                    }

                } catch (Exception e) {
                    // something went wrong: show a Toast
                    // with the exception message
                    Toast.makeText(LoginActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                } finally {
                    super.onFinish();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                super.onFailure(statusCode, headers, throwable, errorResponse);

                Log.d("Failure", errorResponse.toString());
            }

        });


    }
}

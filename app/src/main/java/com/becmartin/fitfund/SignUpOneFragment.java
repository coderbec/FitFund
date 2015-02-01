package com.becmartin.fitfund;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.apache.http.Header;
import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HTTP;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;

public class SignUpOneFragment extends Fragment {

    OnButtonSelectedListener mCallback;
    private Button mSignUpButton;
    private EditText mUsernameField;
    private EditText mPasswordField;
    private EditText mFirstNameField;
    private EditText mLastNameField;
    private EditText mSchoolCode;

    private SharedPreferences mPreferences;

    private AsyncHttpClient client;
    private static final String TASKS_URL = "http://172.16.20.147:8000/accounts/users/";
    private static final String TASKS_URL2 = "http://172.16.20.147:8000/accounts/api-token-auth/";
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_sign_up_one, container, false);
        mPreferences = getActivity().getSharedPreferences("CurrentUser", getActivity().MODE_PRIVATE);

        mUsernameField = (EditText) rootView.findViewById(R.id.email);
        mPasswordField = (EditText) rootView.findViewById(R.id.password);
        mFirstNameField = (EditText) rootView.findViewById(R.id.firstName);
        mLastNameField = (EditText) rootView.findViewById(R.id.lastName);
        mSchoolCode = (EditText) rootView.findViewById(R.id.campaignCode);
        mSignUpButton = (Button) rootView.findViewById(R.id.btnSignUp);
        mSignUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //create user
                String username = mUsernameField.getText().toString().trim();
                String password = mPasswordField.getText().toString().trim();
                String firstName = mFirstNameField.getText().toString().trim();
                String lastName = mLastNameField.getText().toString().trim();
                String schoolCode = mSchoolCode.getText().toString().trim();

                if (username.isEmpty() || password.isEmpty() || firstName.isEmpty() || lastName.isEmpty() || schoolCode.isEmpty()) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                    builder.setMessage("Please enter all the required details")
                            .setTitle("Sorry!")
                            .setPositiveButton(android.R.string.ok, null);
                    AlertDialog dialog = builder.create();
                    dialog.show();
                }
                else {
                    // create the new user!
                   // getActivity().setProgressBarIndeterminateVisibility(true);

                    createNewUser(username, password, firstName, lastName);

                }
                // Send the event to the host activity

                mCallback.onButton(schoolCode, username);
            }
        });

        // Inflate the layout for this fragment
        return rootView;
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

        client.post(null, TASKS_URL2, se, "application/json", new JsonHttpResponseHandler() {

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

                        Toast.makeText(getActivity(), "Logged In", Toast.LENGTH_LONG).show();
                    }

                } catch (Exception e) {
                    // something went wrong: show a Toast
                    // with the exception message
                    Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_LONG).show();
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
    private void createNewUser(final String username, final String password, String firstName, String lastName) {

        client = new AsyncHttpClient();

        // create the User object
        JSONObject jsonUser = new JSONObject();

        try {
            jsonUser.put("email", username);
            jsonUser.put("password", password);
            jsonUser.put("first_name", firstName);
            jsonUser.put("last_name", lastName);
        } catch (JSONException e) {
            e.printStackTrace();
        }





        StringEntity se = null;
        try {
            se = new StringEntity(jsonUser.toString());
            se.setContentEncoding(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));
            Log.d("JSON", se.toString());
            client.post(null, TASKS_URL, se, "application/json", new JsonHttpResponseHandler() {



                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject responseString) {
                    super.onSuccess(statusCode, headers, responseString.toString());
                    Log.d("HTTP", "onSuccess: " + responseString.toString());

                    LoginUser(username, password);
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                    super.onFailure(statusCode, headers, throwable, errorResponse);

                    Log.d("HTTP", "onFailure: " + errorResponse.toString());
                }

            });
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return;
        }


    }

    // Container Activity must implement this interface
    public interface OnButtonSelectedListener {
        public void onButton(String schoolCode, String email);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        // This makes sure that the container activity has implemented
        // the callback interface. If not, it throws an exception
        try {
            mCallback = (OnButtonSelectedListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnHeadlineSelectedListener");
        }
    }


}
package com.becmartin.fitfund;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DownloadManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.TextHttpResponseHandler;

import org.apache.http.Header;
import org.apache.http.HeaderElement;
import org.apache.http.ParseException;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HTTP;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.Response;



/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * to handle interaction events.
 * Use the {@link CampaignFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class CampaignFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "schoolCode";
    Button mSaveButton;
    TextView mSchoolCode;
    private EditText mDistanceField;
    private Spinner mFrequency;
    private SharedPreferences mPreferences;

    private AsyncHttpClient client;
    private static final String TASKS_URL = "http://172.16.20.147:8000/campaign/campaigns/";



    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

   // private OnFragmentInteractionListener mListener;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @return A new instance of fragment CampaignFragment2.
     */
    // TODO: Rename and change types and number of parameters
    public static CampaignFragment newInstance(String param1, String param2) {
        CampaignFragment fragment = new CampaignFragment();
        Bundle args = new Bundle();
        args.putString("SchoolCode", param1);
        args.putString("username", param2);
        fragment.setArguments(args);
        return fragment;
    }

    public CampaignFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString("SchoolCode");
            mParam2 = getArguments().getString("username");
        }


        mPreferences = getActivity().getSharedPreferences("CurrentUser", getActivity().MODE_PRIVATE);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_campaign, container, false);
        mFrequency = (Spinner) rootView.findViewById(R.id.frequencySpinner);
// Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getActivity(),
                R.array.frequency, android.R.layout.simple_spinner_item);
// Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
// Apply the adapter to the spinner
        mFrequency.setAdapter(adapter);
        mDistanceField = (EditText) rootView.findViewById(R.id.distance);
        mSaveButton = (Button) rootView.findViewById(R.id.btnSave);
        mSaveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //create campaign
                Double distance = Double.parseDouble(mDistanceField.getText().toString());
                int frequency = mFrequency.getSelectedItemPosition() + 1;
                String schoolCode = mParam1;

                if (distance.isNaN() || schoolCode.isEmpty()) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                    builder.setMessage("Please enter all the required details")
                            .setTitle("Sorry!")
                            .setPositiveButton(android.R.string.ok, null);
                    AlertDialog dialog = builder.create();
                    dialog.show();
                } else {
                    // create the new user!
                    // getActivity().setProgressBarIndeterminateVisibility(true);

                    createNewCampaign(distance, frequency, schoolCode);

                }
            }

        });

        mSchoolCode = (TextView) rootView.findViewById(R.id.schoolCode);
        mSchoolCode.setText(mParam1 + mParam2);

       // mDistanceField = (EditText) rootView.findViewById(R.id.distance);
        return rootView;


    }

    // TODO: Rename method, update argument and hook method into UI event

    private void createNewCampaign(Double distance, int frequency, String schoolCode) {

        Log.d("values", distance.toString() + frequency + schoolCode);

        client = new AsyncHttpClient();


        // create the User object
        JSONObject jsonCampaign = new JSONObject();

        try {
            jsonCampaign.put("freq_goal", frequency);
            jsonCampaign.put("dist_goal", distance);
            jsonCampaign.put("school_code", schoolCode);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        StringEntity se = null;
        try {
            se = new StringEntity(jsonCampaign.toString());

            Log.d("JSON", jsonCampaign.toString());
            // everything is ok



            client.addHeader("Authorization", "Token " + mPreferences.getString("token", ""));
            client.addHeader("Content-Type", "application/json");

            client.post(null, TASKS_URL, se, "application/json", new TextHttpResponseHandler() {


                @Override
                public void onFailure(int i, Header[] headers, String s, Throwable throwable) {
                    Log.d("HTTP", "onFailure: " + s);
                    Log.d("HTTP", "Headers: " + headers.toString());
                    Toast.makeText(getActivity(), s.toString(), Toast.LENGTH_LONG).show();
                }

                @Override
                public void onSuccess(int i, Header[] headers, String s) {
                    Log.d("HTTP", "onSuccess: " + s.toString());
                    Intent intent = new Intent(getActivity(), MainActivity.class);
                    startActivity(intent);
                }


            });
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return;
        }


    }



}

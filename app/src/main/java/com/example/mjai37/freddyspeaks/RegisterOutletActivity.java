package com.example.mjai37.freddyspeaks;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.mjai37.constants.AppConstants;
import com.example.mjai37.database.DBHelper;
import com.example.mjai37.database.Reward;
import com.example.mjai37.database.Outlet;
import com.example.mjai37.database.Question;
import com.example.mjai37.webservice.RestClient;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.j256.ormlite.dao.Dao;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import cz.msebera.android.httpclient.Header;

public class RegisterOutletActivity extends AppCompatActivity {

    EditText outletName, alias, addrLine1, addrLine2, pinCode, email, phoneNumber;
    Button nextButton;
    Dao<Outlet, Integer> outletDao;
    Dao<Reward, Integer> rewardDao;
    Dao<Question, Integer> questionDao;
    ProgressDialog prgDialog;
    TextView errorMsg;
    Gson gson;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_outlet);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        outletName = (EditText) findViewById(R.id.inputOutletNameText);
        alias = (EditText) findViewById(R.id.inputAliasNameText);
        addrLine1 = (EditText) findViewById(R.id.inputaddressLine1Text);
        addrLine2 = (EditText) findViewById(R.id.inputaddressLine2Text);
        pinCode = (EditText) findViewById(R.id.inputPinCodeText);
        email = (EditText) findViewById(R.id.inputEmailText);
        phoneNumber = (EditText) findViewById(R.id.inputPhoneNumberText);
        nextButton = (Button) findViewById(R.id.next_button);

        GsonBuilder builder = new GsonBuilder();
        gson = builder.create();

        try {
            outletDao = OpenHelperManager.getHelper(this, DBHelper.class).getOutletDao();
            rewardDao = OpenHelperManager.getHelper(this, DBHelper.class).getRewardDao();
            questionDao = OpenHelperManager.getHelper(this, DBHelper.class).getQuestionDao();
        } catch (Exception e) {
            e.printStackTrace();
        }
        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Outlet newOutlet = new Outlet();
                newOutlet.setOutletName(outletName.getText().toString());
                newOutlet.setAliasName(alias.getText().toString());
                newOutlet.setAddrLine1(addrLine1.getText().toString());
                newOutlet.setAddrLine2(addrLine2.getText().toString());
                newOutlet.setPinCode(pinCode.getText().toString());
                newOutlet.setEmail(email.getText().toString());
                newOutlet.setCellNumber(phoneNumber.getText().toString());
                try {
                    outletDao.create(newOutlet);
                } catch (Exception e) {
                    e.printStackTrace();
                    //TODO logging
                }
                //TODO web service call to fetch
                RequestParams params = new RequestParams();

                params.put("outletType", newOutlet.getOutletType());
                RestClient.post(AppConstants.FETCH_QUESTIONS, params, new AsyncHttpResponseHandler() {

                            @Override
                            public void onSuccess(int statusCode, Header[] headers, byte[] responseBytes) {
                                // Hide Progress Dialog
                                try {
                                    String str = new String(responseBytes, "UTF-8");

                                    JSONObject response = new JSONObject(str);
                                    // When the JSON response has status boolean value assigned with true
                                    if (response.getBoolean("success")) {

                                        List<Question> questions = gson.fromJson(response.toString(), List.class);
                                        questions.add(new Question("How is the design variety","Very Poor,Poor,Average,Good,Excellent"));
                                        try {
                                            for (Question question : questions) {
                                                question.setSelected("Y");
                                                questionDao.create(question);
                                            }
                                        } catch (SQLException e) {
                                            e.printStackTrace();
                                        }
                                    } else {
                                        errorMsg.setText(response.getString("msg"));
                                    }
                                } catch (JSONException e) {
                                    // TODO Auto-generated catch block
                                    e.printStackTrace();

                                } catch (UnsupportedEncodingException e) {
                                    e.printStackTrace();
                                }
                            }

                            // When the response returned by REST has Http response code other than '200'
                            @Override
                            public void onFailure(int statusCode, Header[] headers,
                                                  byte[] errorResponse, Throwable e) {
                                if (statusCode == 404) {
                                    Toast.makeText(getApplicationContext(), "Device might not be connected to Internet", Toast.LENGTH_LONG).show();
                                }
                                // When Http response code is '500'
                                else if (statusCode == 500) {
                                    Toast.makeText(getApplicationContext(), "Not able to register now! Please try again later.", Toast.LENGTH_LONG).show();
                                }
                                // When Http response code other than 404, 500
                                else {
                                    Toast.makeText(getApplicationContext(), "Device might not be connected to Internet", Toast.LENGTH_LONG).show();
                                }

                                List<Question> questions = new ArrayList<Question>();
                                questions.add(new Question("How is the design variety","Very Poor,Poor,Average,Good,Excellent"));
                                try {
                                    for (Question question : questions) {
                                        question.setSelected("Y");
                                        questionDao.create(question);
                                    }
                                } catch (SQLException sqle) {
                                    e.printStackTrace();
                                }
                            }
                        }
                );
                RestClient.post(AppConstants.FETCH_REWARDS, params, new AsyncHttpResponseHandler() {

                            @Override
                            public void onSuccess(int statusCode, Header[] headers, byte[] responseBytes) {
                                // Hide Progress Dialog

                                try {
                                    String str = new String(responseBytes, "UTF-8");

                                    JSONObject response = new JSONObject(str);
                                    // When the JSON response has status boolean value assigned with true
                                    if (response.getBoolean("success")) {
                                        List<Reward> rewards = gson.fromJson(response.getString("rewards"), List.class);
                                        try {
                                            for (Reward reward : rewards) {
                                                rewardDao.create(reward);
                                            }
                                        } catch (SQLException e) {
                                            e.printStackTrace();
                                        }
                                    } else {
                                        errorMsg.setText(response.getString("msg"));
                                    }
                                } catch (JSONException e) {
                                    // TODO Auto-generated catch block
                                    e.printStackTrace();

                                } catch (UnsupportedEncodingException e) {
                                    e.printStackTrace();
                                }
                            }

                            // When the response returned by REST has Http response code other than '200'
                            @Override
                            public void onFailure(int statusCode, Header[] headers,
                                                  byte[] errorResponse, Throwable e) {
                                // Hide Progress Dialog

                                // When Http response code is '404'
                                if (statusCode == 404) {
                                    Toast.makeText(getApplicationContext(), "Device might not be connected to Internet", Toast.LENGTH_LONG).show();
                                }
                                // When Http response code is '500'
                                else if (statusCode == 500) {
                                    Toast.makeText(getApplicationContext(), "Not able to register now! Please try again later.", Toast.LENGTH_LONG).show();
                                }
                                // When Http response code other than 404, 500
                                else {
                                    Toast.makeText(getApplicationContext(), "Device might not be connected to Internet", Toast.LENGTH_LONG).show();
                                }
                            }
                        }
                );
                Intent homePage = new Intent(RegisterOutletActivity.this, HomePageActivity.class);
                startActivity(homePage);
            }

        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_register_company, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}

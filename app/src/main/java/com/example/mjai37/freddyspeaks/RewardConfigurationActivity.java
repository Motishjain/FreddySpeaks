package com.example.mjai37.freddyspeaks;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Toast;

import com.example.mjai37.constants.AppConstants;
import com.example.mjai37.database.Outlet;
import com.example.mjai37.database.Question;
import com.example.mjai37.value_objects.Feedback;
import com.example.mjai37.webservice.RestClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import cz.msebera.android.httpclient.Header;

public class RewardConfigurationActivity extends AppCompatActivity {

    Outlet newOutlet;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reward_configuration);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Bundle extras = getIntent().getExtras();
        newOutlet = (Outlet)extras.get("newOutlet");

    }

    public void saveOutletConfig(View v) {
        RequestParams params = new RequestParams();
        params.put("outletName", newOutlet.getOutletName());
        params.put("aliasName", newOutlet.getAliasName());
        params.put("addrLine1", newOutlet.getAddrLine1());
        params.put("addrLine2", newOutlet.getAddrLine2());
        params.put("pinCode", newOutlet.getPinCode());
        params.put("email", newOutlet.getEmail());
        params.put("cellNumber", newOutlet.getCellNumber());


    }
}
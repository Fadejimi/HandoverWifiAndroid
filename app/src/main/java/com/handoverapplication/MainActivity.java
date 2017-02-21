package com.handoverapplication;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    private Button masterBtn, slaveBtn, singleBtn;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        masterBtn = (Button) findViewById(R.id.btn_load_master);
        slaveBtn = (Button) findViewById(R.id.btn_load_slave);
        singleBtn = (Button) findViewById(R.id.btn_load_single);

        masterBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setMaster();
            }
        });

        slaveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setSlave();
            }
        });

        singleBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setSingle();
            }
        });

    }

    private void setMaster() {
        Intent intent = new Intent(getBaseContext(), MasterActivity.class);
        startActivity(intent);
    }

    private void setSlave() {
        Intent intent = new Intent(getBaseContext(), SlaveActivity.class);
        startActivity(intent);
    }

    private void setSingle() {
        Intent intent = new Intent(getBaseContext(), MasterActivity.class);
        intent.putExtra("status", 1);
        startActivity(intent);
    }
}

package com.misker.mike.hasher;

import android.app.Activity;
import android.content.ClipboardManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;

import java.io.FileNotFoundException;
import java.io.InputStream;


public class Main extends AppCompatActivity {

    private static final int READ_REQUEST_CODE = 42;
    ClipboardManager clipboard;
    private Uri fileURI;
    private InputStream is;
    private String hashtype = "MD5";
    private Button hashButton;
    private AdView mAdView;
    private TextView HashCmpText;
    protected static ProgressBar progress;
    protected static TextView hashOutput;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final Button fileButton = (Button) findViewById(R.id.fileButton);
        hashButton = (Button) findViewById(R.id.hashButton);
        hashOutput = (TextView) findViewById(R.id.hashOutput);
        final Spinner selector = (Spinner) findViewById(R.id.hashSelectionSpinner);
        HashCmpText = (TextView) findViewById(R.id.hashCmpText);
        clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        progress = (ProgressBar) findViewById(R.id.progress);

        HashCmpText.setText("Tap to paste reference hash");
        hashButton.setEnabled(false);
        progress.setVisibility(View.INVISIBLE);

        MobileAds.initialize(getApplicationContext(), "ca-app-pub-5863757662079397~1363106066");
        mAdView = (AdView) findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().setGender(AdRequest.GENDER_MALE).addKeyword("Encryption").build();
        mAdView.loadAd(adRequest);

        //Done setting up

        fileButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                showFileChooser();
            }
        });

        hashButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                ContentResolver cr = getContentResolver();
                HashRunnable hasher = new HashRunnable(hashtype, cr);

                hasher.execute(fileURI);
            }
        });

        HashCmpText.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                compareHashes();
            }
        });

        HashCmpText.setOnFocusChangeListener(new View.OnFocusChangeListener(){
            @Override
            public void onFocusChange(View view, boolean b) {
                compareHashes();
            }
        });

        selector.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener(){

            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                hashtype = adapterView.getItemAtPosition(i).toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                //do nothing
            }
        });

    }

    private void compareHashes(){
        Toast toast;
        Context context = getApplicationContext();

        if(!clipboard.hasPrimaryClip()){
            toast = Toast.makeText(context, "Clipboard empty.", Toast.LENGTH_SHORT);
            toast.show();
            return;
        }

        HashCmpText.setText(clipboard.getPrimaryClip().getItemAt(0).coerceToText(getApplicationContext()));

        if(HashCmpText.getText().toString().equals(hashOutput.getText().toString()))
            toast = Toast.makeText(context, "Hashes match!", Toast.LENGTH_SHORT);
        else
            toast = Toast.makeText(context, "Hashes do not match.", Toast.LENGTH_SHORT);

        toast.show();
    }

    private void showFileChooser() {

        if (Build.VERSION.SDK_INT <19){
            Intent intent = new Intent();
            intent.setType("*/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(Intent.createChooser(intent, "Select a file"),
                    READ_REQUEST_CODE);
        } else {
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("*/*");
            startActivityForResult(intent, READ_REQUEST_CODE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent resultData) {

        super.onActivityResult(requestCode, resultCode, resultData);

        if (requestCode == READ_REQUEST_CODE && resultCode == Activity.RESULT_OK && resultData != null) {

            if (resultData != null) {
                fileURI = resultData.getData();
                // Check for the freshest data.
                getContentResolver().takePersistableUriPermission(fileURI,
                        (Intent.FLAG_GRANT_READ_URI_PERMISSION
                        | Intent.FLAG_GRANT_WRITE_URI_PERMISSION));

                ContentResolver cr = getContentResolver();
                try {
                    is = cr.openInputStream(fileURI);

                    if(is != null)
                        hashButton.setEnabled(true);

                }
                catch (FileNotFoundException e) {
                    Log.e("FileDebug", e.getMessage());
                }

            }
        }
    }

}
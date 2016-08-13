package fr.sonline.enacmyedt;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;

import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.ArrayList;

public class WelcomeActivity extends AppCompatActivity {
    ArrayList<CLSStruct> mycourses = new ArrayList<>();
    CLSParser myclsparser = new CLSParser(mycourses); //Tout ca était dans ShowCLS() pour info
    CLSParser parseme;
    EDTWeek classesget;
    ListView mListView;
    String class_name = new String();
    int classid = 0;


    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);
        mListView = (ListView) findViewById(R.id.content_cls);
        mListView.setChoiceMode(AbsListView.CHOICE_MODE_SINGLE);
        Handler handle;
        handle = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                if (msg.what == 0) {
                    //TODO : threader ce code
                    try {
                        myclsparser.ParseIt(classesget.getRawStr());
                    } catch (IOException e) {
                    } catch (XmlPullParserException e) {
                    }
                    ShowCLS();
                }
            }
        };
        classesget = new EDTWeek(0, 0, handle, 1);


        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();


        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                // TODO Auto-generated method stub
                class_name = mycourses.get(position).getName();
                classid = mycourses.get(position).getClid();
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client.connect();
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "Welcome Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app deep link URI is correct.
                Uri.parse("android-app://fr.sonline.enacmyedt/http/host/path")
        );
        AppIndex.AppIndexApi.start(client, viewAction);
    }

    @Override
    public void onStop() {
        super.onStop();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "Welcome Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app deep link URI is correct.
                Uri.parse("android-app://fr.sonline.enacmyedt/http/host/path")
        );
        AppIndex.AppIndexApi.end(client, viewAction);
        client.disconnect();
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        Intent myIntent = new Intent(getApplicationContext(), HomeActivity.class);
        startActivityForResult(myIntent, 0);
        return true;

    }

    public void onClickOk(View view) {
        if(classid == 0){}
        else{
                SharedPreferences prefs = getPreferences(0);
                SharedPreferences.Editor edit = prefs.edit();
                edit.putInt("clid", classid);
                edit.putString("clname", class_name);
                edit.commit();
            finishAct(view);
        }
    }

    public void finishAct(View view){
        Intent intent = new Intent(this, HomeActivity.class);
        SharedPreferences settings = getSharedPreferences("Prefs", 0); //Saving class to cache
        if(classid != 0){
            intent.putExtra("CLID", classid);
            intent.putExtra("CLASSNAME", class_name);
            SharedPreferences.Editor editor = settings.edit();
            editor.putString("CLASSNAME", class_name);
            editor.putInt("CLID", classid);
            editor.commit();
        }
        startActivity(intent);
        finish();
    }

    public void finishNoChange(View view){
        Intent intent = new Intent(this, HomeActivity.class);
        startActivity(intent);
        finish();
    }


    public void ShowCLS() {

        CLSAdaptater adapt = new CLSAdaptater(WelcomeActivity.this, mycourses);
        mListView.destroyDrawingCache();
        mListView.setVisibility(ListView.INVISIBLE);
        mListView.setVisibility(ListView.VISIBLE);
        mListView.setAdapter(adapt);
        Log.w("MyEDT", "Ok");
        if (myclsparser.status == 1) {
            TextView text = (TextView) findViewById(R.id.promosel);
            text.setText("Erreur de connexion, vérifiez votre connexion ou l'état de myedt.enac.fr .");
            classesget.stimulate();
            mListView.invalidate();
            mListView.setAdapter(adapt);
        } else {
            TextView text = (TextView) findViewById(R.id.promosel);
            text.setText("Connexion réussie.");
        }
    }
}

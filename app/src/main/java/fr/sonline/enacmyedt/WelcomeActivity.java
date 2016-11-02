/*
 * Copyright Melvin Diez (c) 2016.
 * This file is part of MyEDT for Android.
 *
 *     MyEDT is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     Foobar is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with Foobar.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package fr.sonline.enacmyedt;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
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



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);
        mListView = (ListView) findViewById(R.id.content_cls);
        mListView.setChoiceMode(AbsListView.CHOICE_MODE_SINGLE);
        classesget = new EDTWeek(0, 0, 1);
        new CLSworker().execute(myclsparser); //Threaded parsing

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
                edit.apply();
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


    public void ShowCLS(CLSParser cls) {

        CLSAdaptater adapt = new CLSAdaptater(WelcomeActivity.this, mycourses);
        mListView.destroyDrawingCache();
        mListView.setVisibility(ListView.INVISIBLE);
        mListView.setVisibility(ListView.VISIBLE);
        mListView.setAdapter(adapt);
        Log.w("MyEDT", "Ok");
        if (cls.status == 1) {
            Toast.makeText(getApplicationContext(), "Erreur de connexion, vérifiez votre connexion ou l'état de myedt.enac.fr .", Toast.LENGTH_LONG).show();
            classesget.stimulate();
            mListView.invalidate();
            mListView.setAdapter(adapt);
        } else {
            Toast.makeText(getApplicationContext(), "Connexion réussie.", Toast.LENGTH_LONG).show();
        }
    }


    private class CLSworker extends AsyncTask<CLSParser, Void, CLSParser> {
        @Override
       protected void onPostExecute(CLSParser parser) {
            ShowCLS(parser);
        }

        @Override
        protected CLSParser doInBackground(CLSParser... params) {
            try {
                 myclsparser.ParseIt(classesget.getRawStr());
            } catch (IOException e) {
            } catch (XmlPullParserException e) {
            }
            return myclsparser;
        }
    }
}

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

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;


public class HomeActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    EDTWeek mainweek;
    ListView mListView;
    int curweek = 0;
    int numclass = 0;
    int errmgr = 0;
    String class_name = new String();
    int panicbutton = 0; //IF panic button == 1 stops network, prevents DoS on myEDT and bandwidth use !
    int errorcounter = 5; //Counts how many times we stimulate the network preventing errors and DoS


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        mListView = (ListView) findViewById(R.id.listView);


        SharedPreferences state = getSharedPreferences("Prefs", 0);
        if(state.getInt("CLID", 0) != 0){
            numclass = state.getInt("CLID", 0);
            class_name = state.getString("CLASSNAME", "Classe inconnue");
        } else { //Go directly to class selection
            toOptions();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        Handler handl = new Handler(){
            @Override
            public void handleMessage(Message msg){
                if(msg.what == 0){
                    ShowEDT();
                }
            }
        };
        Calendar cal = Calendar.getInstance();
        curweek = cal.get(Calendar.WEEK_OF_YEAR);
        if(cal.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY)
            curweek++; //When it's Sunday, return the calendar of the next week
        mainweek = new EDTWeek(curweek, numclass, handl,0);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.home, menu);
        return true;
    }

    public void goNextWeek(View view){
        curweek++;
        Handler handl = new Handler(){
            @Override
            public void handleMessage(Message msg){
                if(msg.what == 0){
                    ShowEDT();
                }
            }
        };
        mainweek = new EDTWeek(curweek, numclass,handl,0);
    }

    public void goPrevWeek(View view){
        curweek--;
        Handler handl = new Handler(){
            @Override
            public void handleMessage(Message msg){
                if(msg.what == 0){
                    ShowEDT();
                }
            }
        };
        mainweek = new EDTWeek(curweek, numclass,handl,0);
    }

    public void goActWeek(View view){
        Calendar cal = Calendar.getInstance();
        curweek = cal.get(Calendar.WEEK_OF_YEAR);
        Handler handl = new Handler(){
            @Override
            public void handleMessage(Message msg){ //Waits for the parsing and download to have finished
                if(msg.what == 0){
                    ShowEDT();
                }
            }
        };
        mainweek = new EDTWeek(curweek, numclass,handl,0);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_selprom) {
            toOptions();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }



    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

         if (id == R.id.nav_settings) {
             toOptions();
        } else if (id == R.id.nav_about) {
             toAbout();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public void ShowEDT(){
        ArrayList<EDTCourse> myweek = new ArrayList<>();
        EDTparser myweekparser = new EDTparser(myweek);

        try {
           myweekparser.ParseIt(mainweek.getRawStr());
            EDTAdaptater adapt = new EDTAdaptater(HomeActivity.this, myweek);
            mListView.destroyDrawingCache();
            mListView.setVisibility(ListView.INVISIBLE);
            mListView.setVisibility(ListView.VISIBLE);
            mListView.setAdapter(adapt); // Previous lines are used to update the display
            if(myweekparser.status == 1){
                errorcounter--;
                if(errorcounter > 0){
                    //Toasting time !
                    Toast.makeText(getApplicationContext(), "Erreur de connexion, essai en cours...", Toast.LENGTH_SHORT).show();
                    mainweek.stimulate();}
                else {
                    panicbutton = 1;
                    Toast.makeText(getApplicationContext(), "Erreur de connexion, vérifiez votre connexion ou l'état de myedt.enac.fr .", Toast.LENGTH_LONG).show();
                }
                mListView.invalidate();
                mListView.setAdapter(adapt);
            }
            else{
                Toast.makeText(getApplicationContext(), "Connexion réussie.", Toast.LENGTH_SHORT).show();
                errorcounter = 10;
                getSupportActionBar().setTitle(class_name.substring(0, class_name.length() - 6) + " Semaine " + (curweek==0?1:curweek%53)); //Changes action bar title
            }
        } catch(XmlPullParserException e){
            int errmgr = 1; //Error Bad website parsing : incomplete or non-working

        } catch(IOException e){
            int errmgr = 2 ; //No connection, website not available
        }

    }

    public void toOptions(){
        Intent intent = new Intent(this, WelcomeActivity.class);
        startActivity(intent);
    }

    public void toAbout(){
        Intent intent = new Intent(this, APropos.class);
        startActivity(intent);
    }
}

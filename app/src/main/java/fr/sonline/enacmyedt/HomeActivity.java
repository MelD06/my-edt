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
import android.os.AsyncTask;
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
import android.widget.Toast;

import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.concurrent.ExecutionException;


public class HomeActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    AsyncTask<Void, Void, EDTWeek> mainweek;
    ListView mListView;
    int curweek = 0;
    int numclass = 0;
    int parserstatus = 0;
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
        if (state.getInt("CLID", 0) != 0) {
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
        Calendar cal = Calendar.getInstance();
        curweek = cal.get(Calendar.WEEK_OF_YEAR);
        if (cal.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY)
            curweek++; //When it's Sunday, return the calendar of the next week
        EDTWeek mweek = new EDTWeek(curweek, numclass, 0);
        new EDTWorker().execute(mweek);
        new EDTWorker().execute(mweek);
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

    public void goNextWeek(View view) {
        curweek++;
        EDTWeek mweek = new EDTWeek(curweek, numclass, 0);
        new EDTWorker().execute(mweek);
    }

    public void goPrevWeek(View view) {
        curweek--;
        EDTWeek mweek = new EDTWeek(curweek, numclass, 0);
        new EDTWorker().execute(mweek);
    }

    public void goActWeek(View view) {
        Calendar cal = Calendar.getInstance();
        curweek = cal.get(Calendar.WEEK_OF_YEAR);
        if (cal.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY)
            curweek++; //When it's Sunday, return the calendar of the next week
        EDTWeek mweek = new EDTWeek(curweek, numclass, 0);
        new EDTWorker().execute(mweek);
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

    public void ShowEDT(EDTAdaptater adapt) {
        if (parserstatus == 1) {
            panicbutton = 1;
            Toast.makeText(getApplicationContext(), "Erreur de connexion, vérifiez votre connexion ou l'état de myedt.enac.fr .", Toast.LENGTH_LONG).show();
            mListView.invalidate();
            mListView.setAdapter(adapt);
        } else {
            errorcounter = 10;
            getSupportActionBar().setTitle(class_name.substring(0, class_name.length() - 6) + " Semaine " + (curweek == 0 ? 1 : curweek % 53)); //Changes action bar title
            mListView.destroyDrawingCache();
            mListView.setVisibility(ListView.INVISIBLE);
            mListView.setVisibility(ListView.VISIBLE);
            mListView.setAdapter(adapt); // Previous lines are used to update the display
        }
    }

    public void toOptions() {
        Intent intent = new Intent(this, WelcomeActivity.class);
        startActivity(intent);
    }

    public void toAbout() {
        Intent intent = new Intent(this, APropos.class);
        startActivity(intent);
    }

    private class EDTWorker extends AsyncTask<EDTWeek, Void, EDTAdaptater> {
        @Override
        protected void onPreExecute(){
            Toast.makeText(getApplicationContext(), "Chargement...", Toast.LENGTH_SHORT).show();
        }

        @Override
        protected void onPostExecute(EDTAdaptater week) {
            ShowEDT(week);
        }

        @Override
        protected EDTAdaptater doInBackground(EDTWeek... params) {

            ArrayList<EDTCourse> myweek = new ArrayList<>();
            EDTparser myweekparser = new EDTparser(myweek);

            try {
                myweekparser.ParseIt(params[0].getRawStr());
                EDTAdaptater adapt = new EDTAdaptater(HomeActivity.this, myweek);
                return adapt;
            } catch (XmlPullParserException e) {
                e.printStackTrace();
                return null;
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }
    }
}

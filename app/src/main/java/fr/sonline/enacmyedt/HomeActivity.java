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
import android.widget.ListView;
import android.widget.TextView;

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
    int errorcounter = 10; //Counts how many times we stimulate the network preventing errors


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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.action_nextw) {
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

           return true;
        }
        if(id == R.id.action_prevw){
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

            return true;
        }
        if(id == R.id.action_actw){
            Calendar cal = Calendar.getInstance();
            curweek = cal.get(Calendar.WEEK_OF_YEAR);
            Handler handl = new Handler(){
                @Override
                public void handleMessage(Message msg){
                    if(msg.what == 0){
                        ShowEDT();
                    }
                }
            };
            mainweek = new EDTWeek(curweek, numclass,handl,0);

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
        } else if (id == R.id.nav_send) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    //Need to use that somewhere
    public void EDTinit(){
        ConnectivityManager connector = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netinfo = connector.getActiveNetworkInfo();
        if(netinfo != null && netinfo.isConnected()){

        }
        else{
            //Print somewhere NO NETWORK !
        }
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
            mListView.setAdapter(adapt);
            if(myweekparser.status == 1){
                TextView text = (TextView) findViewById(R.id.content);
                errorcounter--;
                if(errorcounter > 0){
                    text.setText("Erreur de connexion, essai en cours...");
                    mainweek.stimulate();}
                else {
                    panicbutton = 1;
                    text.setText("Erreur de connexion, vérifiez votre connexion ou l'état de myedt.enac.fr .");
                }
                mListView.invalidate();
                mListView.setAdapter(adapt);
            }
            else{
                TextView text = (TextView) findViewById(R.id.content);
                text.setText("Connexion réussie.");
                errorcounter = 10;
                getSupportActionBar().setTitle(class_name.substring(0, class_name.length() - 6) + " Semaine " + curweek); //Changes action bar title
            }
        } catch(XmlPullParserException e){
            int errmgr = 1; //Error Bad webist parsing : incomplete or non-working

        } catch(IOException e){
            int errmgr = 2 ; //No connection, website not available
        }

    }

    public void toOptions(){
        Intent intent = new Intent(this, WelcomeActivity.class);
        startActivity(intent);
        finish();
    }
}

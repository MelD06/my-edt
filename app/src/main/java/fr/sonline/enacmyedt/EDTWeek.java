package fr.sonline.enacmyedt;


import android.os.AsyncTask;
import android.os.Handler;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Calendar;


/**
 * Created by Melvin on 26/04/2016.
 */
public class EDTWeek {
    int week;
    int classid;
    String pagestr;
    int sel;

    Handler handl;
    InputStream is = null;
    AsyncTask<String, Void, String> asyncTask;

    public EDTWeek(int weeknum, int clid, Handler handler, int select){
        if(week < 0 || week > 52) {
        }
        week = weeknum;
        classid = clid;
        handl = handler;
        sel = select;
        String urlbase = null;
        //If select = 0 then get EDTWeek else get classes
        if(sel == 0){
            urlbase = "http://myedt.enac.fr/edt.php";
        } else{
            urlbase = "http://myedt.enac.fr/";
        }
        Calendar mycal = Calendar.getInstance();
        //Week 1 is last week of August, week 52...
        String conturl = urlbase + "?subj=" + classid + "&week=" + getCorrectedWeek(week); // TO BE CHANGED IF APP IS BROKEN !!!
        asyncTask = new DownloadPage().execute(conturl);

    }

    private int getCorrectedWeek(int week){
        Calendar cal = Calendar.getInstance();
        int corweek = 0;
        if(cal.get(Calendar.MONTH) >= 7) { //Preventing year differences
            cal.set(Calendar.YEAR, 7, 26); //to de sure that it's the last week the 23th day
            corweek = week - cal.get(Calendar.WEEK_OF_YEAR) + 1;
        }
        else {
            cal.set(Calendar.YEAR - 1, 7, 26);
            // (52 - cal start week) + int week
            corweek = (52 - cal.get(Calendar.WEEK_OF_YEAR)) + week + 1;
        }
        if(corweek >= 51)
            corweek = corweek%51; //Tired, correcting a bug the lazy way
        if(corweek < 0)
            corweek = (51 + corweek)%51; //Little bit of Dark Magic to prevent negative weeks
        return corweek;
    }

    public int getStatus(){
        if(asyncTask.getStatus() == AsyncTask.Status.FINISHED){
            return 0;
        }
        else {
            return 1;
        }
    }

    public void stimulate(){
        new EDTWeek(week, classid, handl, sel);
    }

    public String getRawStr(){
        return pagestr;
    }


    public InputStream getIs(){
        return is;
    }

    private class DownloadPage extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... url){
            try{
                return DownloadURL(url[0]);
            } catch (IOException e){
                return "Invalid URL";
            }
        }
        @Override
        protected void onPostExecute(String result) {
            handl.sendEmptyMessage(0);
            pagestr = result;
        }


        private String DownloadURL(String theurl) throws IOException {
            int maxlen = 100000; //Prevents loads of shit

            try {
                URL weburl = new URL(theurl);
                HttpURLConnection conn = (HttpURLConnection) weburl.openConnection();
                conn.setReadTimeout(10000);
                conn.setConnectTimeout(15000);
                conn.setRequestMethod("GET");
                conn.setDoInput(true);

                conn.connect();
                int recieved = conn.getResponseCode();
                is = conn.getInputStream();

                String contstr = Readstr(is, maxlen);
                return contstr;
            } finally {
                if (is != null) {
                    is.close();
                }
            }
        }
            public String Readstr(InputStream is, int len) throws IOException {
            Reader reader = null;
            reader = new InputStreamReader(is, "UTF8");
            char[] buffer = new char[len];
            reader.read(buffer);
            return new String(buffer);
        }
        }

    }




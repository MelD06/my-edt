package fr.sonline.enacmyedt;

import android.util.Log;
import android.util.Xml;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;

/**
 * Created by Melvin on 02/05/2016.
 */
public class CLSParser {
    public ArrayList<CLSStruct> classes = new ArrayList<CLSStruct>();
    String page;
    int status = 0;

    private static final String namespace = null;

    public CLSParser(ArrayList<CLSStruct> list){
        classes = list;
    }

    public void addPage(String pagestr){
        page = pagestr;
    }

    public void ParseIt(String pagestr) throws XmlPullParserException, IOException {
        if(pagestr != null && pagestr.length() > 40) {
            page = pagestr.substring(pagestr.indexOf("<body>"));

            XmlPullParser parser = Xml.newPullParser();
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
            parser.setInput(new StringReader(page)); //Corrects a Bug on Android 4
            parser.nextTag();
            doParse(parser);
        }
        else {
            status = 1;
        }
    }

    private void doParse(XmlPullParser parseme) throws XmlPullParserException, IOException{
        int ok = 0;
        while(ok == 0) {
            NextIt(1,parseme);
            if (parseme.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String balise = parseme.getName();
            if (balise.equals("a")) {
                readClass(parseme);
            }
            if(balise.equals("body")){
                ok = 1;
            }
        }
    }

    private void readClass(XmlPullParser parser) throws XmlPullParserException, IOException {
        parser.require(XmlPullParser.START_TAG, namespace, "a");
        String clid = null;
        String Name = null;
        int clas;

        int count = 0;
        int ok = 0;

        clid = parser.getAttributeValue(null, "href");
        clas = Integer.parseInt(clid.substring(clid.indexOf("=") + 1));
        Name = readText(parser);
        NextIt(3,parser);

        classes.add(new CLSStruct(clas, Name));

    }

    private String readText(XmlPullParser parser) throws XmlPullParserException, IOException {
        while(parser.getText() == null){
            parser.next();
        }

        String text = parser.getText();
        return text;

    }

    private void NextIt(int num, XmlPullParser parse) throws XmlPullParserException, IOException{ //Iterations of next for p to p parsing
        for(int i = 0; i < num; i++){
            try{
                parse.next();
            }
            catch(XmlPullParserException e){         }
        }
    }
    public void toLog(String str){
        Log.w("MyEDT", str);
    }
}


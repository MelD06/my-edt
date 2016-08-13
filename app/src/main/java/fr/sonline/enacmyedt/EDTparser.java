package fr.sonline.enacmyedt;

import android.util.Xml;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;

/**
 * Created by Melvin on 27/04/2016.
 */
public class EDTparser {
    public ArrayList<EDTCourse> courselist = new ArrayList<EDTCourse>();
    String page;
    int status = 0;

    private static final String namespace = null;

    public EDTparser(ArrayList<EDTCourse> list){
        courselist = list;
    }


    public void ParseIt(String pagestr) throws XmlPullParserException, IOException {
        //TODO ; add if length > 60000 error ! homepage generated because class doesnt exists
        if(pagestr != null && !pagestr.isEmpty()) {
            if (pagestr.length() > 100000) {
                status = 1;
            } else if (pagestr.length() > 40) {
                page = pagestr.substring(pagestr.indexOf("<tbody>"));

                XmlPullParser parser = Xml.newPullParser();
                parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
                parser.setInput(new StringReader(page)); //Corrects a Bug on Android 4
                parser.nextTag();
                doParse(parser);
            } else {
                status = 1;
            }
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
            if (balise.equals("tr")) {
                readCourse(parseme);
            }
            else{
                ok = 1;
            }
        }
    }

    private void readCourse(XmlPullParser parser) throws XmlPullParserException, IOException {
        parser.require(XmlPullParser.START_TAG, namespace, "tr");
        String Start = null;
        String End = null;
        String Title = null;
        String Room = null;
        String curDay = null;

        int count = 0;
        int ok = 0;
            while (ok != 2) {
                if (parser.getAttributeValue(null, "rowspan") != null) {
                    curDay = readText(parser);
                    parser.nextTag();
                }
                if (parser.getAttributeValue(null, "class") != null && ok == 0) {
                    Start = readText(parser);
                    ok = 1;
                }
                if(parser.getName() != null){
                    if(parser.getName().equals("br")){
                        End = readText(parser);
                        ok = 2;
                        break;
                    }
                }
                parser.next();
            }
        ok = 0;
        try{
            parser.next();
        }
        catch(XmlPullParserException e){ //Catches the bad <br> of the page

        }
        NextIt(3, parser);//Big shit of code to adapt to the table
        Title = readText(parser);
        NextIt(4, parser);
        Room = parser.getText();
        NextIt(6, parser);
        courselist.add(new EDTCourse(curDay, Start, End, Title, Room)); //Finally adds a new course to the list

    }

    private String readText(XmlPullParser parser) throws XmlPullParserException, IOException {
        while(parser.getText() == null){
                parser.next();
            }

        String text = parser.getText();
            return text;

    }

    private void skip(XmlPullParser parser) throws XmlPullParserException, IOException {
        if(parser.getEventType() != XmlPullParser.START_TAG){
            throw new IllegalStateException();
        }
        int depth = 1;
        while(depth != 0) {
            switch (parser.next()) {
                case XmlPullParser.START_TAG:
                    depth++;
                    break;
                case XmlPullParser.END_TAG:
                    depth--;
                    break;
            }
            String bobo = parser.getName();
        }
    }

    private void NextIt(int num, XmlPullParser parse) throws XmlPullParserException, IOException{ //Iterations of next for p to p parsing
        for(int i = 0; i < num; i++){
            try{
                parse.next();
            }
            catch(XmlPullParserException e){         }
        }
    }
}

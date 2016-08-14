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
        if(pagestr != null && !pagestr.isEmpty()) {
            if (pagestr.length() > 100000) {
                status = 1;
            } else if (pagestr.length() > 40) {
                try {
                    page = pagestr.substring(pagestr.indexOf("<tbody>"));
                }
                catch(StringIndexOutOfBoundsException e){
                    status = 1; //TODO: handle that catch : this means that the class doesn't exists
                }
            if(status ==0) {
                XmlPullParser parser = Xml.newPullParser();
                parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
                parser.setInput(new StringReader(page)); //Corrects a Bug on Android 4
                parser.nextTag();
                doParse(parser);
            }
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

    private void NextIt(int num, XmlPullParser parse) throws XmlPullParserException, IOException{ //Iterations of next for p to p parsing
        for(int i = 0; i < num; i++){
            try{
                parse.next();
            }
            catch(XmlPullParserException e){         }
        }
    }
}

package fr.sonline.enacmyedt;

/**
 * Created by Melvin on 02/05/2016.
 */
public class CLSStruct {
    int clid;
    String name = null;

    public CLSStruct(int classid, String namez){
        clid = classid;
        name = namez;
    }

    public String getName(){
        return name;
    }

    public int getClid() {
        return clid;
    }
}


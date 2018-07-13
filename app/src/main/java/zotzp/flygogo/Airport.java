package zotzp.flygogo;

import com.google.firebase.database.IgnoreExtraProperties;



@IgnoreExtraProperties
public class Airport {
    public String code;
    public String name;

    public Airport() {

    }

    public Airport(String code, String name) {
        this.code = code;
        this.name = name;
    }

    @Override
    public String toString() {
        return name + " - " + code;
    }

}

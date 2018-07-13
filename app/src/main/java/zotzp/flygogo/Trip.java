package zotzp.flygogo;



public class Trip {
    public String name;
    public String id;
    public String user;
    public boolean roundTrip;
    public String fromAirportCode;
    public String toAirportCode;
    public String fromAirportName;
    public String toAirportName;
    public String departDate;
    public String returnDate;
    public int userPrice;

    // set by backend
    public int currentPrice;
    public boolean notifyUser;
    public boolean userPriceMet;
    public String tripUrl;

    public Trip() {
        this.currentPrice = 0;
        this.notifyUser = true;
        this.userPriceMet = false;
        this.tripUrl = "";
        this.name = "";
    }
}

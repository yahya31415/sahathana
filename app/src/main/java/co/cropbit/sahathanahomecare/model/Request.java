package co.cropbit.sahathanahomecare.model;

/**
 * Created by yahya on 02/08/17.
 */

public class Request {

    public String uid;
    public Location location;
    public Long datetime;
    public int status;
    public String key;

    public static final int SENT = 0;
    public static final int PROCESSING = 1;
    public static final int APPROVED = 2;

    public Request() {

    }

    Request(String u, Location l, Long dt, int st) {
        uid = u;
        location = l;
        datetime = dt;
        status = st;
    }

    public String statusString() {
        switch (status) {
            case SENT: return "Sent";
            case PROCESSING: return "Processing";
            case APPROVED: return "Approved";
        }
        return null;
    }
}

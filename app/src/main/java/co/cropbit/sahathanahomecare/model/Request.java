package co.cropbit.sahathanahomecare.model;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

/**
 * Created by yahya on 02/08/17.
 */

public class Request {

    public String uid;
    public Location location;
    public Long datetime;
    public int status;
    public String key;
    public String type;
    public boolean isEmergency;
    public String approvedBy;
    public String approved = "Waiting for approval";

    public static final int SENT = 0;
    public static final int PROCESSING = 1;
    public static final int APPROVED = 2;

    public Request() {

    }

    Request(String u, Location l, Long dt, int st, String tp, boolean isE, String apb) {
        uid = u;
        location = l;
        datetime = dt;
        status = st;
        type = tp;
        isEmergency = isE;
        approvedBy = apb;
    }

    public String statusString() {
        switch (status) {
            case SENT: return "Sent";
            case PROCESSING: return "Processing";
            case APPROVED: return "Approved";
        }
        return null;
    }

    public void setApproved(DatabaseReference ref, final Runnable runnable) {
        if(approvedBy == null) {
            runnable.run();
            return;
        }
        final Request r = this;
        ref.child(approvedBy).child("displayName").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String name = dataSnapshot.getValue(String.class);
                r.approved = name;
                runnable.run();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
}

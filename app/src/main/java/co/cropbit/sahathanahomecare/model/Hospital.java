package co.cropbit.sahathanahomecare.model;

/**
 * Created by yahya on 01/08/17.
 */

public class Hospital {

    public String name;
    public String address;
    public Location location;

    Hospital () {

    }

    Hospital (String name, String address, Location location) {
        this.name = name;
        this.address = address;
        this.location = location;
    }
}

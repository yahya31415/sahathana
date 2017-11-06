package co.cropbit.sahathanahomecare.model

import java.util.HashMap

/**
 * Created by yahya on 01/08/17.
 */

class Location {

    var lat: Double = 0.0
    var lng: Double = 0.0

    constructor(lat: Double, lng: Double) {
        this.lat = lat
        this.lng = lng
    }


    fun toMap(): Map<String, Object> {
        var result = HashMap<String, Any>()
        result.put("lat", lat.toString())
        result.put("lng", lng.toString())
        return result as Map<String, Object>
    }
}

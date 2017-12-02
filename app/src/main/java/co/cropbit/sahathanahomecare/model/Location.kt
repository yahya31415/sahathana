package co.cropbit.sahathanahomecare.model

import java.util.HashMap

/**
 * Created by yahya on 01/08/17.
 */

class Location(val lat: Double, val lng: Double) {

    fun toMap(): Map<String, Object> {
        var result = HashMap<String, Any>()
        result.put("lat", lat.toString())
        result.put("lng", lng.toString())
        return result as Map<String, Object>
    }
}

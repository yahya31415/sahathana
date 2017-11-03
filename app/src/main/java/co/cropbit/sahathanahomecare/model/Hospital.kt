package co.cropbit.sahathanahomecare.model

/**
 * Created by yahya on 01/08/17.
 */

class Hospital {

    var address: String? = null
    var location: Location? = null
    var phoneNumber: String? = null

    internal constructor() {

    }

    internal constructor(address: String, location: Location, phoneNumber: String) {
        this.address = address
        this.location = location
        this.phoneNumber = phoneNumber
    }
}

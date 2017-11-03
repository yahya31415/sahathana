package co.cropbit.sahathanahomecare.model

/**
 * Created by yahya on 01/08/17.
 */

class User {

    var name: String? = null
    var phoneNumber: String? = null
    var defaultHospital: String? = null

    internal constructor() {

    }

    internal constructor(name: String, phoneNumber: String, defaultHospital: String) {
        this.name = name
        this.phoneNumber = phoneNumber
        this.defaultHospital = defaultHospital
    }
}

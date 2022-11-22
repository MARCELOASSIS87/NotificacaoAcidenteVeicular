package com.marceloassis.notificacaoacidenteveicular.model

class User {
    var name = ""
    var lastname = ""
    var latitude: Double = 0.0
    var longitude: Double = 0.0

    override fun toString(): String {
        return "User(name=$name, latitude=$latitude, longitude=$longitude, lastname=$lastname)"
    }

}
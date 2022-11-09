package com.marceloassis.notificacaoacidenteveicular

data class UserData(
    val name: String,
    val lastName: String,
    val latitude: Double,
    val longitude: Double
) {
    override fun toString(): String {
        return "Localization " +
                "[nome: ${this.name}, " +
                "sobrenome: ${this.lastName}, " +
                "latitude: ${this.latitude}, " +
                "longitude: ${this.longitude}]"
    }
}

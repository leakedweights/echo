package hu.bme.aut.echo.models

data class Message (
    var content: String,
    var sender: Sender,
    var successful: Boolean = true,
) {
    enum class Sender {
        Echo, User;
    }
}
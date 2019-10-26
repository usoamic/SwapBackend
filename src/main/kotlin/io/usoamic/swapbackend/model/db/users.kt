package io.usoamic.swapbackend.model.db

import org.jetbrains.exposed.sql.*

object users : Table() {
    val id = integer("id").primaryKey().autoIncrement()
    val email = varchar("email", 512)
    val password = varchar("password", 512)
    val confirm_code = varchar("confirm_code", 512)
    val tfa_status = varchar("tfa_status", 512)
    val secret_key = varchar("secret_key", 512)
    val address = varchar("address", 512)
    val received = varchar("received", 512)
    val reset_code = varchar("reset_code", 512)
    val received_by_yobit_codes = varchar("received_by_yobit_codes", 512)
    val withdrawn = varchar("withdrawn", 512)
}
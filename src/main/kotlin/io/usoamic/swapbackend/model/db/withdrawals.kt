package io.usoamic.swapbackend.model.db

import org.jetbrains.exposed.sql.*

object withdrawals : Table() {
    val id = integer("id").primaryKey().autoIncrement()
    val amount = varchar("amount", 512)
    val address = varchar("address", 512)
    val time = varchar("time", 512)
    val status = varchar("status", 512)
    val email = varchar("email", 512)
    val type = varchar("type", 512)
}
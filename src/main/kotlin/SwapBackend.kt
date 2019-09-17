package io.usoamic.swapbackend

import io.usoamic.swapbackend.model.withdrawals
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
//https://github.com/JetBrains/Exposed
class SwapBackend {
    init {
//        Base.open("com.mysql.jdbc.Driver", "jdbc:mysql://157.245.182.2", "admin_default", "FnVhMPh68A")

        Database.connect("jdbc:mysql://157.245.182.2:3306/admin_default", driver = "com.mysql.jdbc.Driver",
            user = "admin_default", password = "FnVhMPh68A")

        transaction {
            SchemaUtils.create(withdrawals)

            withdrawals.selectAll().forEachIndexed { index, resultRow ->
                println("id: $index, $resultRow")
            //    resultRow[amount] = "a6" //change value
            }

            /*
            val stPeteId = Withdraws.insert {
                it[amount] = "1"
                it[address] = "2"
                it[time] = "3"
                it[status] = "4"
                it[email] = "5"
                it[type] = "6"
            } get Withdraws.id

            for (city in Withdraws.selectAll()) {
                println("${city[Withdraws.id]}: ${city[Withdraws.address]}")
            }

            println("size: $stPeteId")

             */
        }
    }
}
package io.usoamic.swapbackend.other

import java.math.BigDecimal
import java.math.BigInteger

sealed class Transfer(
    open val address: String
) {
    data class Ether(
        override val address: String,
        val coinAmount: BigDecimal
    ) : Transfer(
        address = address
    )

    data class Usoamic(
        override val address: String,
        val satAmount: BigInteger
    ) : Transfer(
        address = address
    )
}

package io.usoamic.swapbackend.other

enum class TxType {
    TX_ALL,
    TX_LEGACY_SENT,
    TX_MOVED,
    TX_MINED,
    TX_RECEIVED,
    TX_UNKNOWN,
    TX_SENT,
    TX_SENT_ETH;

    fun toId(): String {
        return (this.ordinal + 1).toString()
    }

    companion object {
        fun valueOf(id: Int): TxType {
            return values()[id - 1]
        }
    }
}
package io.usoamic.swapbackend.other

enum class TxStatus {
    TX_PENDING,
    TX_CONFIRMED,
    TX_REJECTED;

    fun isPending(): Boolean {
        return (this == TX_PENDING)
    }

    fun isConfirmed(): Boolean {
        return (this == TX_CONFIRMED)
    }

    fun isRejected(): Boolean {
        return (this == TX_REJECTED)
    }

    fun toId(): String {
        return (this.ordinal + 1).toString()
    }

    companion object {
        fun valueOf(id: Int): TxStatus {
            return values()[id - 1]
        }
    }
}
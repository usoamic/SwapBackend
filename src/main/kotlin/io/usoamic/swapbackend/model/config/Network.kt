package io.usoamic.swapbackend.model.config

import com.google.gson.annotations.SerializedName
import io.usoamic.usoamickt.enumcls.NetworkType
import io.usoamic.usoamickt.enumcls.NodeProvider

data class Network(
    @SerializedName("type") private val type: String,
    @SerializedName("node") private val node: String,
    @SerializedName("arguments") private val arguments: String = ""
) {
    val Type: NetworkType get() = NetworkType.valueOf(type.toUpperCase())
    val Node: NodeProvider
        get() = when (node.toUpperCase()) {
            "INFURA" -> NodeProvider.Infura(arguments)
            "MYETHERWALLET" -> NodeProvider.MyEtherWallet
            "ETHERSCAN" -> NodeProvider.EtherScan
            else -> throw ClassNotFoundException()
        }
}
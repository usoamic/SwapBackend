package io.usoamic.swapbackend.model.config

import com.google.gson.annotations.SerializedName
import io.usoamic.usoamickt.enum.NetworkType
import io.usoamic.usoamickt.enum.NodeProvider

data class Network (
    @SerializedName("type") private val type: String,
    @SerializedName("node") private val node: String
)
{
    val Type: NetworkType get() = NetworkType.valueOf(type.toUpperCase())
    val Node: NodeProvider get() = NodeProvider.valueOf(node.toUpperCase())
}
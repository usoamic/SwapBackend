package io.usoamic.swapbackend.other

class Config {
    companion object {
        //DB
        const val DB_URL = LocalConfig.DB_URL
        const val DB_DRIVER = LocalConfig.DB_DRIVER
        const val DB_USER = LocalConfig.DB_USER
        const val DB_PASSWORD = LocalConfig.DB_PASSWORD

        //AES
        const val AES_METHOD = LocalConfig.AES_METHOD
        const val AES_KEY = LocalConfig.AES_KEY
        const val AES_IV = LocalConfig.AES_IV

        //USOAMIC
        const val NODE = LocalConfig.NODE
        const val ACCOUNT_FILENAME = LocalConfig.ACCOUNT_FILENAME
        const val ACCOUNT_PASSWORD = LocalConfig.ACCOUNT_PASSWORD
        const val ACCOUNT_PRIVATE_KEY = LocalConfig.ACCOUNT_PRIVATE_KEY
        const val CONTRACT_ADDRESS = LocalConfig.CONTRACT_ADDRESS
    }
}
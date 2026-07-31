package com.example.data.api

import com.example.data.db.MonnifyConfigEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Payment Service for Monnify Gateway Integration.
 * Initializes gateway with credentials fetched from Admin Config settings
 * and handles user subscription payments (e.g. 600 Naira / Month All-Access).
 */
class MonnifyPaymentService(
    private val monnifyService: MonnifyService = MonnifyService()
) {
    @Volatile
    private var currentConfig: MonnifyConfigEntity = MonnifyConfigEntity()

    /**
     * Initializes the gateway with credentials fetched from Admin settings.
     */
    fun initializeGateway(config: MonnifyConfigEntity) {
        this.currentConfig = config
    }

    /**
     * Returns the currently active gateway configuration.
     */
    fun getGatewayConfig(): MonnifyConfigEntity = currentConfig

    /**
     * Authenticates with Monnify using initialized Admin credentials.
     */
    suspend fun authenticateGateway(): MonnifyAuthResponse {
        return monnifyService.authenticate(
            apiKey = currentConfig.apiKey,
            secretKey = currentConfig.secretKey,
            useSandbox = currentConfig.useSandbox
        )
    }

    /**
     * Process user subscription payment initialized with admin credentials.
     * Default amount is 600.0 NGN for 600 Naira / Month All-Access.
     */
    suspend fun processSubscriptionPayment(
        customerName: String,
        customerEmail: String,
        planTitle: String = "600 Naira / Month All-Access VIP Subscription",
        amountNgn: Double = currentConfig.vipPassPriceNgn
    ): MonnifyInitResponse {
        return withContext(Dispatchers.IO) {
            monnifyService.initializeTransaction(
                config = currentConfig,
                amount = amountNgn,
                customerName = customerName,
                customerEmail = customerEmail,
                itemTitle = planTitle
            )
        }
    }

    /**
     * Verifies a transaction using initialized Admin gateway credentials.
     */
    suspend fun verifyPayment(paymentRef: String): MonnifyVerifyResponse {
        return monnifyService.verifyTransaction(currentConfig, paymentRef)
    }
}

package com.jujodevs.cursotestingandroid.productlist.data.remote

import com.jujodevs.cursotestingandroid.checkout.data.remote.response.OrderConfirmationResponse
import com.jujodevs.cursotestingandroid.productlist.data.remote.response.ProductsResponse
import com.jujodevs.cursotestingandroid.productlist.data.remote.response.PromotionsResponse
import retrofit2.http.GET

interface MiniMarketApiService {
    @GET("data/products.json")
    suspend fun getProducts(): ProductsResponse

    @GET("data/promotions.json")
    suspend fun getPromotions(): PromotionsResponse

    @GET("data/order_confirmation.json")
    suspend fun placeOrder(): OrderConfirmationResponse
}

package com.jujodevs.cursotestingandroid.productlist.data.remote

import com.jujodevs.cursotestingandroid.checkout.data.remote.response.OrderConfirmationResponse
import com.jujodevs.cursotestingandroid.core.domain.model.AppError
import com.jujodevs.cursotestingandroid.productlist.data.remote.response.ProductResponse
import com.jujodevs.cursotestingandroid.productlist.data.remote.response.PromotionResponse
import kotlinx.coroutines.CancellationException
import retrofit2.HttpException
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import javax.inject.Inject

class RemoteDataSource
    @Inject
    constructor(
        private val miniMarketApiService: MiniMarketApiService,
    ) {
        suspend fun getProducts(): Result<List<ProductResponse>> =
            safeCall { miniMarketApiService.getProducts().products }

        suspend fun getPromotions(): Result<List<PromotionResponse>> =
            safeCall { miniMarketApiService.getPromotions().promotions }

        suspend fun placeOrder(): Result<OrderConfirmationResponse> = safeCall { miniMarketApiService.placeOrder() }

        private suspend fun <T> safeCall(call: suspend () -> T): Result<T> =
            try {
                Result.success(call())
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Result.failure(mapToDomainError(e))
            }

        private fun mapToDomainError(e: Exception): AppError =
            when (e) {
                is UnknownHostException -> AppError.NetworkError
                is SocketTimeoutException -> AppError.NetworkError
                is IOException -> AppError.NetworkError
                is HttpException -> {
                    when (e.code()) {
                        404 -> AppError.NotFoundError
                        else -> AppError.NetworkError
                    }
                }
                else -> AppError.UnknownError(e.message)
            }
    }

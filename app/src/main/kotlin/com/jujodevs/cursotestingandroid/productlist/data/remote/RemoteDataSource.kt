package com.jujodevs.cursotestingandroid.productlist.data.remote

import com.jujodevs.cursotestingandroid.core.domain.model.AppError
import com.jujodevs.cursotestingandroid.productlist.data.remote.response.ProductResponse
import com.jujodevs.cursotestingandroid.productlist.data.remote.response.PromotionResponse
import kotlinx.coroutines.CancellationException
import retrofit2.HttpException
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import javax.inject.Inject

class RemoteDataSource @Inject constructor(
    private val miniMarketApiService: MiniMarketApiService
) {
    suspend fun getProducts(): Result<List<ProductResponse>> {
        return try {
            val response = miniMarketApiService.getProducts()
            Result.success(response.products)
        } catch (e: CancellationException) {
          throw e
        } catch (e: Exception) {
            Result.failure(mapToDomainError(e))
        }
    }

    suspend fun getPromotions(): Result<List<PromotionResponse>> {
        return try {
            val response = miniMarketApiService.getPromotions()
            Result.success(response.promotions)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(mapToDomainError(e))
        }
    }

    private fun mapToDomainError(e: Exception): AppError {
        return when(e) {
            is UnknownHostException -> AppError.NetworkError
            is SocketTimeoutException -> AppError.NetworkError
            is IOException -> AppError.NetworkError
            is HttpException -> {
                when(e.code()) {
                    404 -> AppError.NotFoundError
                    else -> AppError.NetworkError
                }
            }
            else -> AppError.UnknownError(e.message)
        }
    }
}
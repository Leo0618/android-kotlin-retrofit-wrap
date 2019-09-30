package vip.okfood.network

import retrofit2.http.GET

/**
 * function:API
 *
 * <p></p>
 * Created by Leo on 2019/9/29.
 */
interface API {

    @GET("test.json")
    suspend fun info(): String
}
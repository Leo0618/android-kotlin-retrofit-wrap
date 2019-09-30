package vip.okfood.network.lib

import android.util.Log
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.ResponseBody
import okhttp3.logging.HttpLoggingInterceptor
import okio.Buffer
import retrofit2.Converter
import retrofit2.Retrofit
import java.io.IOException
import java.io.OutputStreamWriter
import java.lang.reflect.Type
import java.nio.charset.Charset
import java.util.concurrent.TimeUnit

/**
 * function:RetrofitWrap
 *
 * Created by Leo on 2019/9/30.
 */
class RetrofitWrap private constructor() {

    private var retrofit: Retrofit? = null

    companion object {
        private val instance: RetrofitWrap by lazy(mode = LazyThreadSafetyMode.SYNCHRONIZED) {
            RetrofitWrap()
        }

        /**
         * 是否开启打印日志，默认关闭
         */
        fun init(baseUrl: String, debugEnable: Boolean = false) {
            instance.retrofit = Retrofit.Builder().apply {
                baseUrl(baseUrl)
                client(okhttpClient(debugEnable))
                addConverterFactory(StringConverterFactory.create())
            }.build()
        }

        /**
         * 获取API服务
         */
        fun <T> service(service: Class<T>): T {
            if (instance.retrofit != null) {
                return instance.retrofit!!.create(service)
            } else {
                throw IllegalStateException("请先调用RetrofitWrap.init()方法进行初始化")
            }
        }


        //OkHttpClient客户端
        private fun okhttpClient(debugEnable: Boolean = false): OkHttpClient {
            return OkHttpClient.Builder().apply {
                connectTimeout(15, TimeUnit.SECONDS)
                readTimeout(15, TimeUnit.SECONDS)
                if (debugEnable) addInterceptor(interceptorLog())
            }.build()
        }


        //日志打印拦截器
        private fun interceptorLog(): HttpLoggingInterceptor {
            val interceptor = HttpLoggingInterceptor(object : HttpLoggingInterceptor.Logger {
                override fun log(message: String) {
                    Log.d("RetrofitWrap", message)
                }
            })
            interceptor.level = HttpLoggingInterceptor.Level.BODY
            return interceptor
        }
    }
}

//StringConverterFactory
private class StringConverterFactory private constructor() : Converter.Factory() {

    override fun responseBodyConverter(
        type: Type?,
        annotations: Array<Annotation>?,
        retrofit: Retrofit?
    ): Converter<ResponseBody, *>? {
        return StringResponseBodyConverter()
    }

    override fun requestBodyConverter(
        type: Type?,
        parameterAnnotations: Array<Annotation>?,
        methodAnnotations: Array<Annotation>?,
        retrofit: Retrofit?
    ): Converter<*, RequestBody>? {
        return StringRequestBodyConverter()
    }

    class StringRequestBodyConverter internal constructor() : Converter<String, RequestBody> {
        override fun convert(value: String): RequestBody? {
            val buffer = Buffer()
            val writer = OutputStreamWriter(buffer.outputStream(), UTF_8)
            writer.write(value)
            writer.close()
            return buffer.readByteString().toRequestBody(MEDIA_TYPE)
        }

        companion object {
            private val MEDIA_TYPE = "application/json; charset=UTF-8".toMediaTypeOrNull()
            private val UTF_8 = Charset.forName("UTF-8")
        }
    }

    class StringResponseBodyConverter : Converter<ResponseBody, String> {
        @Throws(IOException::class)
        override fun convert(value: ResponseBody): String? {
            value.use { v ->
                return v.string()
            }
        }
    }

    companion object {
        internal fun create(): StringConverterFactory {
            return StringConverterFactory()
        }
    }
}

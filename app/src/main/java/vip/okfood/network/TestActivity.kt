package vip.okfood.network

import android.os.Bundle
import android.text.TextUtils
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.*
import vip.okfood.network.lib.RetrofitWrap

class TestActivity : AppCompatActivity(), CoroutineScope by MainScope() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        RetrofitWrap.init("https://okfood.vip/", false)

        btn.setOnClickListener {
            content.text = "加载中..."
            launch {
                val data = withContext(Dispatchers.IO) {
                    RetrofitWrap.service(API::class.java).info()
                }
                content.text = if (TextUtils.isEmpty(data)) "error" else data
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        cancel()
    }
}

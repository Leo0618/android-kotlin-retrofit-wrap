# android-kotlin-retrofit-wrap

> andriod项目中网络请求使用`kotlin`和`Retrofit`的最优雅方式，抛弃`Callback`，抛弃`RxJava`，接受**协程**吧
> 
> (网上一大堆都是Retrofit+Rxjava或者使用回调方式，其实使用协程才是真的简单好用)
> 
> ---
> 
> android项目现在基本都是用kotlin开发，而kotlin的优势显而易见，其中的协程也是一大利器
> 
> 网络交互一般使用okhttp+Retrofit，而在okhttp4.0+已经使用了kotlin作为维护语言，Retrofit在2.6.0版本也开始支持了挂起（**suspend**）修饰符，这使得android项目中使用kotlin个retrofi进行网络交互尤为方便
> 
> 该项目内容为kotlin协程配合Retrofit实现网络请求的一个示例

### Step0: android项目中加入kotlin及其协程的支持

- 项目根目录`build.gradle`文件中加入：

		buildscript {
		    ext.kotlin_version = '1.3.50'
		    ext.kotlin_coroutines = '1.3.2'
		    
			...

		    dependencies {
		        classpath 'com.android.tools.build:gradle:3.5.0'
		        classpath "org.jetbrains.kotlin:kotlin-gradle-plugin:$kotlin_version"
		        ...
		    }
		}

- app目录下的`build.gradle`中加入支持并添加依赖：

		apply plugin: 'kotlin-android'
		apply plugin: 'kotlin-android-extensions'

		dependencies {
		    implementation "org.jetbrains.kotlin:kotlin-stdlib-jdk7:$kotlin_version"
		    implementation "org.jetbrains.kotlinx:kotlinx-coroutines-core:$kotlin_coroutines"
		    implementation "org.jetbrains.kotlinx:kotlinx-coroutines-android:$kotlin_coroutines"
		    
			implementation 'androidx.core:core-ktx:1.1.0'
		    implementation 'androidx.appcompat:appcompat:1.1.0'
		
		    implementation 'com.squareup.retrofit2:retrofit:2.6.2'
		    implementation "com.squareup.okhttp3:okhttp:4.2.0"
		    implementation "com.squareup.okhttp3:logging-interceptor:4.2.0"
			
			...
		}

### Step1: 对Retrofit的简单封装 详见[RetrofitWrap.kt](https://github.com/Leo0618/android-kotlin-retrofit-wrap/blob/master/app/src/main/java/vip/okfood/network/lib/RetrofitWrap.kt)

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

就两个对外方法：

- `init`：初始化，配置baseUrl和是否打印日志，其他参数可以基于源码修改加入
- `service`：获取服务实例，传入定义api的接口类，然后调用对应方法即可完成数据请求

### Step2: 根据业务定义网络交互方法，例如[API.kt](https://github.com/Leo0618/android-kotlin-retrofit-wrap/blob/master/app/src/main/java/vip/okfood/network/API.kt)

	interface API {
	    @GET("test.json")
	    suspend fun info(): String
	}

### 使用Example,详见[TestActivity.kt](https://github.com/Leo0618/android-kotlin-retrofit-wrap/blob/master/app/src/main/java/vip/okfood/network/TestActivity.kt)

- 完整示例：

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

- 说明
	- 初始化` RetrofitWrap.init`需要放置在第一次请求网络之前，可以是application中
	- 不建议使用`GlobalScope`去`launch`一个协程任务，android中便于生命周期的管理提供了`MainScope`，在页面销毁处调用`cancel()`避免内存泄露

### 闲外话

- 该项目内容仅是一种极简使用方式的展示，具体项目中，可以根据项目架构做对应调整，比如加上使用ViewModel以及LiveData等

- 同理，实现方式也可以用于数据库的操作，毕竟网络和数据库都是android数据存储的方式
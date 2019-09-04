package com.hiczp.myapplication

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.*
import com.google.gson.JsonArray
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.features.json.GsonSerializer
import io.ktor.client.features.json.JsonFeature
import io.ktor.client.features.logging.LogLevel
import io.ktor.client.features.logging.Logging
import io.ktor.client.request.get
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private val httpClient = HttpClient(CIO) {
    install(Logging) {
        level = LogLevel.ALL
    }
    install(JsonFeature) {
        serializer = GsonSerializer()
    }
}

class MainActivity : AppCompatActivity() {
    private lateinit var model: MainViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        model = ViewModelProvider(this)[MainViewModel::class.java]
        setContentView(R.layout.activity_main)

        model.repos.observe(this) {
            textView.text = it
        }
        model.start()
    }
}

class MainViewModel : ViewModel() {
    val repos = MutableLiveData<String>("Loading")

    fun start() {
        viewModelScope.launch(IO) {
            while (true) {
                repos.postValue("Loading...")
                httpClient.get<JsonArray>("https://api.github.com/orgs/JetBrains/repos")
                    .let { jsonArray ->
                        repos.postValue(
                            jsonArray.joinToString(separator = "\n") {
                                it.asJsonObject["name"].asString
                            }
                        )
                    }
                delay(10_000)
            }
        }
    }
}

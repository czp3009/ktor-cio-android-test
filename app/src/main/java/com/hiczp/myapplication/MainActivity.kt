package com.hiczp.myapplication

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.*
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.features.logging.LogLevel
import io.ktor.client.features.logging.Logging
import io.ktor.client.request.get
import io.ktor.client.statement.HttpResponse
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private val httpClient = HttpClient(CIO) {
    install(Logging) {
        level = LogLevel.ALL
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
        model.times.observe(this) {
            Toast.makeText(this, "$it times", Toast.LENGTH_SHORT).show()
        }
        model.start()
    }
}

class MainViewModel : ViewModel() {
    val repos = MutableLiveData("Loading")
    val times = MutableLiveData(0)

    fun start() {
        viewModelScope.launch(IO) {
            while (true) {
                repos.postValue("Loading...")
                val httpResponse = httpClient.get<HttpResponse>("https://api.github.com/orgs/JetBrains/repos")
                repos.postValue(
                        httpResponse.headers.entries().joinToString(separator = "\n") { (key, values) ->
                            values.joinToString(separator = "\n") { "$key=$it" }
                        }
                )
                times.postValue(times.value!! + 1)
                delay(10_000)
            }
        }
    }
}

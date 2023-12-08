package com.ironmeddie.installreferer

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.android.installreferrer.api.InstallReferrerClient
import com.android.installreferrer.api.InstallReferrerStateListener
import com.android.installreferrer.api.ReferrerDetails
import com.ironmeddie.installreferer.ui.theme.InstallRefererTheme
import io.appmetrica.analytics.AppMetrica
import io.appmetrica.analytics.DeferredDeeplinkListener
import io.appmetrica.analytics.DeferredDeeplinkParametersListener

@OptIn(ExperimentalMaterial3Api::class)
class MainActivity : ComponentActivity() {

    private lateinit var referrerClient: InstallReferrerClient


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        referrerClient = InstallReferrerClient.newBuilder(this).build()
        referrerClient.startConnection(object : InstallReferrerStateListener {

            override fun onInstallReferrerSetupFinished(responseCode: Int) {
                when (responseCode) {
                    InstallReferrerClient.InstallReferrerResponse.OK -> {
                        Log.d("checkCode", "InstallReferrerResponse.OK")
                    }

                    InstallReferrerClient.InstallReferrerResponse.FEATURE_NOT_SUPPORTED -> {
                        Log.d("checkCode", "InstallReferrerResponse.FEATURE_NOT_SUPPORTED")
                    }

                    InstallReferrerClient.InstallReferrerResponse.SERVICE_UNAVAILABLE -> {
                        Log.d("checkCode", "InstallReferrerResponse.SERVICE_UNAVAILABLE")
                    }
                }
            }

            override fun onInstallReferrerServiceDisconnected() {

            }
        })



        setContent {
            InstallRefererTheme {
                Box(
                    modifier = Modifier.fillMaxSize(),
                ) {
                    val state = rememberScrollState()
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .scrollable(state, Orientation.Vertical),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        val deeplink = rememberSaveable { mutableStateOf("") }
                        val error = rememberSaveable { mutableStateOf("") }
                        val installReferer = rememberSaveable { mutableStateOf("") }
                        val appmetricaDataParams = rememberSaveable { mutableStateOf("") }
                        val appmetricaDeeplink = rememberSaveable { mutableStateOf("") }

                        LaunchedEffect(key1 = referrerClient.isReady) {
                            val response: ReferrerDetails = referrerClient.installReferrer
                            val referrerUrl: String = response.installReferrer
                            val referrerClickTime: Long = response.referrerClickTimestampSeconds
                            val appInstallTime: Long = response.installBeginTimestampSeconds
                            val instantExperienceLaunched: Boolean = response.googlePlayInstantParam

                            installReferer.value = referrerUrl + "\n\n\n" +
                                    "referrerClickTime: " + referrerClickTime + "\n"
                            "appInstallTime: " + appInstallTime + "\n"
                            "instantExperienceLaunched: " + instantExperienceLaunched
                            referrerClient.endConnection()
                        }

                        Text(
                            text = error.value,
                            color = Color.Red,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 21.dp)
                        )
                        Spacer(modifier = Modifier.height(34.dp))

                        Text(
                            text = installReferer.value,
                            color = Color.Black,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 21.dp)
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        Text(
                            text = appmetricaDataParams.value,
                            color = Color.Red,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 21.dp)
                        )
                        Text(
                            text = appmetricaDeeplink.value,
                            color = Color.Red,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 21.dp)
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        TextField(value = deeplink.value, onValueChange = {
                            deeplink.value = it
                            error.value = ""
                        })

                        Spacer(modifier = Modifier.height(24.dp))

                        Button(onClick = {
                            try {
                                val uri = Uri.parse(deeplink.value)
                                val intent = Intent(Intent.ACTION_VIEW, uri)
                                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                startActivity(intent)
                                error.value = "success"
                            } catch (t: Exception) {
                                error.value = t?.localizedMessage ?: "unknown Error"
                            }

                        }, enabled = deeplink.value.isNotEmpty()) {
                            Text(text = "open deeplink")
                        }

                        Spacer(modifier = Modifier.height(24.dp))
                        Button(onClick = {
                            AppMetrica.requestDeferredDeeplink(object : DeferredDeeplinkListener {
                                override fun onDeeplinkLoaded(deeplink: String) {
                                    appmetricaDeeplink.value = deeplink
                                }

                                override fun onError(
                                    error: DeferredDeeplinkListener.Error,
                                    referrer: String?
                                ) {
                                    appmetricaDeeplink.value =
                                        "Error: ${error.description}, unparsed referrer: $referrer"
                                }
                            })
                            AppMetrica.requestDeferredDeeplinkParameters(object :
                                DeferredDeeplinkParametersListener {
                                override fun onParametersLoaded(parameters: Map<String, String>) {
                                    appmetricaDataParams.value = ""
                                    for (key in parameters.keys) {
                                        appmetricaDataParams.value += "key: $key value: ${parameters[key]} \n"
                                    }
                                }

                                override fun onError(
                                    error: DeferredDeeplinkParametersListener.Error,
                                    referrer: String
                                ) {
                                    appmetricaDataParams.value = "Error! " + error.description
                                }
                            })

                        }) {
                            Text(text = "get appmetrica params")
                        }
                        Spacer(modifier = Modifier.height(24.dp))
                    }

                }
            }
        }
    }
}




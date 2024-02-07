package com.ironmeddie.installreferer

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.android.installreferrer.api.InstallReferrerClient
import com.ironmeddie.installreferer.ui.theme.InstallRefererTheme
import io.appmetrica.analytics.AppMetrica
import io.appmetrica.analytics.DeferredDeeplinkListener
import io.appmetrica.analytics.DeferredDeeplinkParametersListener


class MainActivity : ComponentActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            InstallRefererTheme {
                Box(
                    modifier = Modifier.fillMaxSize(),
                ) {

                    val appmetricaDataParams = rememberSaveable { mutableStateOf("") }
                    val appmetricaDeeplink = rememberSaveable { mutableStateOf("") }

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {

                        item {
                            Text(
                                text = appmetricaDataParams.value,
                                color = Color.Red,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 21.dp)
                            )
                            Spacer(modifier = Modifier.height(24.dp))
                        }
                        item {
                            Text(
                                text = appmetricaDeeplink.value,
                                color = Color.Red,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 21.dp)
                            )
                            Spacer(modifier = Modifier.height(24.dp))
                        }

                        item {
                            Spacer(modifier = Modifier.height(24.dp))
                            Button(onClick = {
                                AppMetrica.requestDeferredDeeplink(object :
                                    DeferredDeeplinkListener {
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
                                Text(text = "get appmetrica data")
                            }
                            Spacer(modifier = Modifier.height(24.dp))
                        }

                    }

                }
            }
        }
    }


}




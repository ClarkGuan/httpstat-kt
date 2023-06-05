package com.httpstat.kotlin

import com.beust.jcommander.JCommander
import com.beust.jcommander.Parameter
import okhttp3.OkHttpClient
import okhttp3.Request
import okio.Okio
import kotlin.system.exitProcess

class Args {
    @Parameter
    var parameters: List<String> = ArrayList()

    @Parameter(names = ["-c", "-count"], description = "HTTP request retry count")
    var retryCount = 0
}

fun main(cmdArgs: Array<String>) {
    runCatching {
        val args = Args()
        JCommander.newBuilder().addObject(args).build().parse(*cmdArgs)
        if (args.parameters.isEmpty()) {
            println("can't find any urls")
            exitProcess(1)
        }
        val count = (if (args.retryCount <= 0) 1 else 1 + args.retryCount)
//            .also {
//                println("count: $it")
//            }
        val url = args.parameters[0]
//            .also {
//                println("url: $it")
//            }

        val client = OkHttpClient.Builder()
            .eventListener(DemoEventListener())
            .socketFactory(InnerSocketFactory())
            .build();

        for (i in 0 until count) {
            if (i > 0) {
                Thread.sleep(1000)
                println("\n--------------------")
            }

            val response = client.newCall(
                Request.Builder()
                    .url(url)
                    .build()
            ).execute()
            response.body()?.also {
                it.source().readAll(Okio.blackhole())
                it.close()
            }
        }

        client.dispatcher().executorService().shutdownNow()
        client.connectionPool().evictAll()
        client.cache()?.close()
    }.onFailure {
        it.printStackTrace()
        exitProcess(1)
    }
}
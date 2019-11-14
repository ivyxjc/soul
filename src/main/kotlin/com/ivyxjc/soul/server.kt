package com.ivyxjc.soul

import com.google.gson.Gson
import io.vertx.core.Vertx
import io.vertx.core.json.JsonObject
import io.vertx.ext.web.Router
import io.vertx.ext.web.handler.StaticHandler
import io.vertx.ext.web.handler.TemplateHandler
import io.vertx.ext.web.templ.thymeleaf.impl.ThymeleafTemplateEngineImpl
import okhttp3.OkHttpClient
import okhttp3.Request
import org.apache.commons.io.IOUtils
import org.apache.commons.lang3.StringUtils
import org.slf4j.LoggerFactory
import kotlin.random.Random


internal class Sample


internal fun loadSoul(): List<String> {
    val input = Sample::class.java.classLoader.getResourceAsStream("soul.txt")
    val data = IOUtils.toString(input, Charsets.UTF_8)
    if (StringUtils.isBlank(data)) {
        throw RuntimeException("fail to load soul")
    }
    return data.split("\n");
}


fun main() {
    val log = LoggerFactory.getLogger(Sample::class.java)
    val soulList = loadSoul()
    val soulSize = soulList.size
    if (soulList.isEmpty()) {
        throw RuntimeException("fail to load soul")
    }


    val vertx = Vertx.vertx()
    val client = OkHttpClient()
    val gson = Gson()

    val request = Request.Builder()
        .url("https://v1.hitokoto.cn/")
        .build()


    val engine = ThymeleafTemplateEngineImpl(vertx)
    val router = Router.router(vertx);

    val templateHandler = TemplateHandler.create(engine)


    router.route("/static/*").handler(StaticHandler.create())
    router.route("/").handler { ctx ->
        val bingo = Random.nextBoolean()
        val color = if (bingo) {
            "#996600"
        } else {
            "#1aaf5d"
        }
        val oneWord = if (bingo) {
            soulList[kotlin.math.abs(kotlin.random.Random.nextInt() % soulSize)]
        } else {
            try {
                val response = client.newCall(request).execute()
                val map = gson.fromJson(response.body!!.string(), Map::class.java)
                map["hitokoto"] as String
            } catch (e: Exception) {
                soulList[kotlin.math.abs(kotlin.random.Random.nextInt() % soulSize)]
            }
        }
        val data = JsonObject()
            .put("color", color)
            .put("data", oneWord)
        // and now delegate to the engine to render it.
        engine.render(data, "webroot/templates/index.html") { res ->
            if (res.succeeded()) {
                ctx.response().end(res.result())
            } else {
                ctx.fail(res.cause())
            }
        }
    }
    vertx.createHttpServer().requestHandler(router).listen(8080)
}
package ar.jornadas

import android.content.Context
import androidx.room.Room
import ar.jornadas.data.*
import ar.jornadas.lab.ServicioLocal
import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import org.json.JSONArray
import retrofit2.Converter
import retrofit2.Retrofit
import java.lang.reflect.Type

class JsonConverterFactory : Converter.Factory() {
    override fun responseBodyConverter(
        type: Type,
        annotations: Array<out Annotation>,
        retrofit: Retrofit
    ): Converter<ResponseBody, *> {
        return Converter<ResponseBody, List<FichaRemota>> { body ->
            val jsonString = body.string()
            val array = JSONArray(jsonString)
            val list = mutableListOf<FichaRemota>()
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                list.add(
                    FichaRemota(
                        id = obj.getString("id"),
                        title = obj.getString("title"),
                        track = obj.getString("track"),
                        day = obj.getInt("day"),
                        time = obj.getString("time"),
                        room = obj.getString("room"),
                        speaker = obj.getString("speaker"),
                        summary = obj.getString("summary")
                    )
                )
            }
            list
        }
    }
}

class Servicios(context: Context) {
    val mesa = ServicioLocal(context)
    private val archivo = Room.databaseBuilder(context, Archivo::class.java, "cartelera.db").build()
    private val api = Retrofit.Builder().baseUrl("https://programa.invalid/")
        .client(OkHttpClient.Builder().addInterceptor(mesa).build())
        .addConverterFactory(JsonConverterFactory()).build().create(ProgramaApi::class.java)
    val agenda = AgendaRepository(api, archivo.cartelera(), context)
}

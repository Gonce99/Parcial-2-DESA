package ar.jornadas.data

import android.content.Context

class AgendaRepository(
    private val api: ProgramaApi,
    private val dao: CarteleraDao,
    context: Context
) {
    private val prefs = context.getSharedPreferences("agenda_prefs", Context.MODE_PRIVATE)
    val programa = dao.observar()

    fun haSincronizado(): Boolean = prefs.getBoolean("ha_sincronizado", false)

    suspend fun sincronizar() {
        val remotas = api.consultar()
        dao.actualizarPrograma(remotas)
        prefs.edit().putBoolean("ha_sincronizado", true).apply()
    }

    suspend fun cambiarFavorita(id: String) = dao.alternar(id)
}

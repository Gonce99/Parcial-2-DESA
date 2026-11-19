package ar.jornadas.ui

import android.app.Application
import androidx.lifecycle.*
import ar.jornadas.Servicios
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class ProgramaModel(
    app: Application,
    private val savedStateHandle: SavedStateHandle
) : AndroidViewModel(app) {
    private val servicios = Servicios(app)
    val mesa = servicios.mesa
    private val repo = servicios.agenda
    val charlas = repo.programa.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val dia: StateFlow<Int> = savedStateHandle.getStateFlow("dia", 0)
    val tema: StateFlow<String> = savedStateHandle.getStateFlow("tema", "Todos")
    val seccion: StateFlow<String> = savedStateHandle.getStateFlow("seccion", "Programa")
    val detalle: StateFlow<String?> = savedStateHandle.getStateFlow("detalle", null)

    val aviso = MutableStateFlow("")
    val cargando = MutableStateFlow(false)
    private var sesionIniciada = false
    private var peticionesEnCurso = 0

    val haSincronizado: Boolean
        get() = repo.haSincronizado()

    fun setDia(valor: Int) { savedStateHandle["dia"] = valor }
    fun setTema(valor: String) { savedStateHandle["tema"] = valor }
    fun setSeccion(valor: String) { savedStateHandle["seccion"] = valor }
    fun setDetalle(valor: String?) { savedStateHandle["detalle"] = valor }

    fun reiniciarEstadoInicial() {
        savedStateHandle["dia"] = 0
        savedStateHandle["tema"] = "Todos"
        savedStateHandle["seccion"] = "Programa"
        savedStateHandle["detalle"] = null as String?
    }

    fun iniciarSesionSiEsNecesario() {
        if (!sesionIniciada) {
            sesionIniciada = true
            recargar()
        }
    }

    fun recargar() {
        viewModelScope.launch {
            synchronized(this@ProgramaModel) {
                peticionesEnCurso++
                cargando.value = true
            }
            aviso.value = ""
            try {
                repo.sincronizar()
                aviso.value = ""
            } catch (e: Exception) {
                if (repo.haSincronizado()) {
                    aviso.value = "Se están usando datos guardados"
                } else {
                    aviso.value = "No se pudo consultar el programa"
                }
            } finally {
                synchronized(this@ProgramaModel) {
                    peticionesEnCurso--
                    if (peticionesEnCurso <= 0) {
                        peticionesEnCurso = 0
                        cargando.value = false
                    }
                }
            }
        }
    }

    fun marcar(id: String) { viewModelScope.launch { repo.cambiarFavorita(id) } }
}

package br.com.zup.edu.grpc.dominio.repository

import br.com.zup.edu.grpc.dominio.modelo.ChavePix
import io.micronaut.data.annotation.Query
import io.micronaut.data.annotation.Repository
import io.micronaut.data.jpa.repository.JpaRepository
import java.util.*

@Repository
interface ChavePixRepository : JpaRepository<ChavePix, String> {

    fun existsByChave(chave: String): Boolean

    @Query("SELECT c FROM ChavePix c WHERE c.idInterno = :idInterno AND c.clienteId = :clienteId")
    fun consultarPeloIdInternoEPeloCliente(idInterno: String, clienteId: String): Optional<ChavePix>

    @Query("DELETE FROM ChavePix c WHERE c.idInterno = :idInterno")
    fun deleteByIdInterno(idInterno: String)

    @Query("UPDATE ChavePix c SET c.chave = :chave WHERE c.identificador = :identificador")
    fun atualizarChavePixComNovaChave(chave: String, identificador: String)

    @Query("SELECT c FROM ChavePix c WHERE c.clienteId = :clienteId")
    fun consultarPorClienteId(clienteId: String): List<ChavePix>

}

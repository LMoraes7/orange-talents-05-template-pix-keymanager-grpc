package br.com.zup.edu.grpc.dominio.repository

import br.com.zup.edu.grpc.dominio.modelo.ChavePix
import io.micronaut.context.annotation.Parameter
import io.micronaut.data.annotation.Query
import io.micronaut.data.annotation.Repository
import io.micronaut.data.jpa.repository.JpaRepository
import java.util.*

@Repository
interface ChavePixRepository : JpaRepository<ChavePix, Long> {

    fun existsByChave(chave: String): Boolean

    fun findByChave(chave: String): Optional<ChavePix>

    @Query("DELETE FROM ChavePix c WHERE c.pixIdInterno = :pixIdInterno")
    fun deletarPeloIdInterno(pixIdInterno: String)

    @Query("SELECT c FROM ChavePix c WHERE c.pixIdInterno = :pixIdInterno")
    fun buscarPeloIdInterno(pixIdInterno: String): Optional<ChavePix>

    @Query("UPDATE ChavePix c SET c.chave = :chave WHERE c.pixIdInterno = :pixIdInterno")
    fun atualizarChavePeloIdInterno(chave: String, pixIdInterno: String): Optional<ChavePix>

    @Query("SELECT c FROM ChavePix c WHERE c.clienteId = :clienteId AND c.pixIdInterno = :pixIdInterno")
    fun buscarChavePeloIdDoClienteEPeloIdInterno(clienteId: String, pixIdInterno: String): Optional<ChavePix>
}

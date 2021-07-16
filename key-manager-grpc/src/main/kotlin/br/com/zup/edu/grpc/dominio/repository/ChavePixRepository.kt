package br.com.zup.edu.grpc.dominio.repository

import br.com.zup.edu.grpc.dominio.modelo.ChavePix
import io.micronaut.data.annotation.Query
import io.micronaut.data.annotation.Repository
import io.micronaut.data.jpa.repository.JpaRepository
import java.util.*

@Repository
interface ChavePixRepository: JpaRepository<ChavePix, Long> {

    fun existsByChave(chave: String): Boolean

    @Query("DELETE FROM ChavePix c WHERE c.pixIdInterno = :pixIdInterno")
    fun deletarPeloIdInterno(pixIdInterno: String)

    @Query("SELECT c FROM ChavePix c WHERE c.pixIdInterno = :pixIdInterno")
    fun buscarPeloIdInterno(pixIdInterno: String): Optional<ChavePix>
}

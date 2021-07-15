package br.com.zup.edu.grpc.dominio.repository

import br.com.zup.edu.grpc.dominio.modelo.ChavePix
import io.micronaut.data.annotation.Repository
import io.micronaut.data.jpa.repository.JpaRepository

@Repository
interface ChavePixRepository: JpaRepository<ChavePix, Long> {

    fun existsByChave(chave: String): Boolean
}

package br.com.zup.edu.grpc.service

import br.com.zup.edu.grpc.dominio.exception.ClienteInexistenteException
import br.com.zup.edu.grpc.dominio.modelo.ChavePix
import br.com.zup.edu.grpc.dominio.repository.ChavePixRepository
import br.com.zup.edu.grpc.enpoint.consultar.todas.request.ConsultarTodasChavePixRequestDto
import br.com.zup.edu.grpc.http.client.ItauClient
import io.micronaut.http.HttpStatus
import io.micronaut.validation.Validated
import javax.inject.Singleton
import javax.validation.Valid

@Singleton
@Validated
class ConsultarTodasChavePixService(
    private val repository: ChavePixRepository,
    private val itauClient: ItauClient,
) {

    fun consultar(
        @Valid request: ConsultarTodasChavePixRequestDto,
    ): List<ChavePix> {
        this.verificarSeClienteExisteNoSistemaDoItau(request)
        return this.repository.consultarPorClienteId(request.clienteId!!)
    }

    private fun verificarSeClienteExisteNoSistemaDoItau(
        request: ConsultarTodasChavePixRequestDto,
    ) {
        this.itauClient.buscarCliente(request.clienteId!!)
            .run {
                if (!this.status.equals(HttpStatus.OK))
                    throw ClienteInexistenteException("Cliente informado não está cadastrado no Itaú")
            }
    }
}

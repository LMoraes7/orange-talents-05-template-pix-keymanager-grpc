package br.com.zup.edu.grpc.endpoint.consultar.util

import br.com.zup.edu.grpc.dominio.exception.notfound.ClienteNaoEncontradoException
import br.com.zup.edu.grpc.http.client.itau.ItauClient
import io.micronaut.http.HttpStatus

class VerificarSeClienteExisteNoSistemaItau {

    companion object {
        fun verifica(itauClient: ItauClient, clienteId: String) {
            itauClient.buscarCliente(clienteId)
                .run {
                    if (this.status == HttpStatus.NOT_FOUND)
                        throw ClienteNaoEncontradoException("Cliente informado não existe no sistema Itaú")
                }
        }
    }
}
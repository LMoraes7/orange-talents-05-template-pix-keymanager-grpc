package br.com.zup.edu.grpc.http.client

import br.com.zup.edu.grpc.dominio.modelo.Conta
import br.com.zup.edu.grpc.dominio.modelo.Instituicao
import br.com.zup.edu.grpc.dominio.modelo.Titular
import br.com.zup.edu.grpc.http.client.itau.response.DadosDaContaResponse
import br.com.zup.edu.grpc.http.client.itau.response.DadosDoClienteResponse
import io.micronaut.http.HttpResponse
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.PathVariable
import io.micronaut.http.annotation.QueryValue
import io.micronaut.http.client.annotation.Client
import javax.inject.Singleton

@Singleton
@Client("http://localhost:9091")
interface ItauClient {

    @Get("/api/v1/clientes/{clienteId}")
    fun buscarCliente(@PathVariable clienteId: String):
            HttpResponse<DadosDoClienteResponse>

    @Get("/api/v1/clientes/{clienteId}/contas")
    fun buscarClienteEConta(
        @PathVariable clienteId: String,
        @QueryValue("tipo") tipoConta: String,
    ): HttpResponse<DadosDaContaResponse>
}



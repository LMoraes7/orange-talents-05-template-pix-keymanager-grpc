package br.com.zup.edu.grpc.http.client

import io.micronaut.http.HttpResponse
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.PathVariable
import io.micronaut.http.annotation.QueryValue
import io.micronaut.http.client.annotation.Client

@Client("http://localhost:9091")
interface ItauClient {

    @Get("/api/v1/clientes/{clienteId}/contas")
    fun buscarContaCliente(
        @PathVariable clienteId: String,
        @QueryValue("tipo") tipo: String,
    ): HttpResponse<ContaAssociadaResponse>
}

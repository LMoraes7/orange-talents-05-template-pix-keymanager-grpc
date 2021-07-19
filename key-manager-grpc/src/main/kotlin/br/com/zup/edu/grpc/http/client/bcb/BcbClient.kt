package br.com.zup.edu.grpc.http.client.bcb

import br.com.zup.edu.grpc.http.client.bcb.dto.request.CreatePixKeyRequest
import br.com.zup.edu.grpc.http.client.bcb.dto.request.DeletePixKeyRequest
import br.com.zup.edu.grpc.http.client.bcb.dto.response.CreatePixKeyResponse
import br.com.zup.edu.grpc.http.client.bcb.dto.response.DeletePixKeyResponse
import io.micronaut.http.HttpResponse
import io.micronaut.http.MediaType
import io.micronaut.http.annotation.*
import io.micronaut.http.client.annotation.Client

@Client("http://localhost:8082")
interface BcbClient {

    @Post(value = "/api/v1/pix/keys")
    @Produces(MediaType.APPLICATION_XML)
    @Consumes(MediaType.APPLICATION_XML)
    fun cadastrar(@Body createPixKeyRequest: CreatePixKeyRequest):
            HttpResponse<CreatePixKeyResponse>

    @Delete(value = "/api/v1/pix/keys/{key}")
    @Produces(MediaType.APPLICATION_XML)
    @Consumes(MediaType.APPLICATION_XML)
    fun remover(@PathVariable key: String, @Body deletePixKeyRequest: DeletePixKeyRequest):
            HttpResponse<DeletePixKeyResponse>
}
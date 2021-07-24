package br.com.zup.edu.grpc.http.client.bcb


import br.com.zup.edu.grpc.http.client.bcb.request.CreatePixKeyRequest
import br.com.zup.edu.grpc.http.client.bcb.request.DeletePixKeyRequest
import br.com.zup.edu.grpc.http.client.bcb.response.CreatePixKeyResponse
import br.com.zup.edu.grpc.http.client.bcb.response.PixKeyDetailsResponse
import io.micronaut.http.HttpResponse
import io.micronaut.http.MediaType
import io.micronaut.http.annotation.*
import io.micronaut.http.client.annotation.Client
import javax.inject.Singleton

@Singleton
@Client("http://localhost:8082")
interface BcbClient {

    @Post("/api/v1/pix/keys")
    @Produces(MediaType.APPLICATION_XML)
    @Consumes(MediaType.APPLICATION_XML)
    fun cadastrar(@Body createPixKeyRequest: CreatePixKeyRequest):
            HttpResponse<CreatePixKeyResponse>

    @Get("/api/v1/pix/keys/{key}")
    @Produces(MediaType.APPLICATION_XML)
    @Consumes(MediaType.APPLICATION_XML)
    fun consultarPorChave(@PathVariable key: String)
            : HttpResponse<PixKeyDetailsResponse>

    @Delete("/api/v1/pix/keys/{key}")
    @Produces(MediaType.APPLICATION_XML)
    @Consumes(MediaType.APPLICATION_XML)
    fun remover(@PathVariable key: String, @Body deletePixKeyRequest: DeletePixKeyRequest)
            : HttpResponse<Any>
}

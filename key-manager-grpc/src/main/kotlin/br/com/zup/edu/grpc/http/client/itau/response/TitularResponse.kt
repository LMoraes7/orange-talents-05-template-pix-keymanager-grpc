package br.com.zup.edu.grpc.http.client.itau.response

import br.com.zup.edu.grpc.dominio.modelo.Titular

data class  TitularResponse(val id: String, val nome: String, val cpf: String) {
    fun paraTitularModel(): Titular =
        Titular(
            id = this.id,
            nome = this.nome,
            cpf = this.cpf
        )
}
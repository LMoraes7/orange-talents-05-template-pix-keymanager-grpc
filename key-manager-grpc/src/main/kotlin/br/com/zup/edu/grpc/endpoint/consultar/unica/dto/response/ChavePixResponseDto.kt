package br.com.zup.edu.grpc.endpoint.consultar.unica.dto.response

import br.com.zup.edu.grpc.dominio.modelo.ChavePix

data class ChavePixResponseDto(
    val pixId: String? = null,
    val clienteId: String? = null,
    val tipoChave: String,
    val chave: String,
    val titular: TitularResponseDto,
    val contaAssociada: ContaAssociadaResponseDto,
    val dataRegistro: String,
) {

    companion object {
        fun build(chavePix: ChavePix): ChavePixResponseDto =
            ChavePixResponseDto(
                pixId = chavePix.pixIdInterno,
                clienteId = chavePix.clienteId,
                tipoChave = chavePix.tipoChave.name,
                chave = chavePix.chave,
                titular = TitularResponseDto(
                    nome = chavePix.conta.titular.nome,
                    cpf = chavePix.conta.titular.cpf
                ),
                contaAssociada = ContaAssociadaResponseDto(
                    instituicao = chavePix.conta.instituicao.nome,
                    agencia = chavePix.conta.agencia,
                    numero = chavePix.conta.numero,
                    tipoConta = chavePix.tipoConta.name
                ),
                dataRegistro = chavePix.criadaEm.toString()
            )

    }
}

class ContaAssociadaResponseDto(
    val instituicao: String,
    val agencia: String,
    val numero: String,
    val tipoConta: String,
)

data class TitularResponseDto(
    val nome: String,
    val cpf: String,
)

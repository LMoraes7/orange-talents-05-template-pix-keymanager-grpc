package br.com.zup.edu.grpc.enpoint.consultar.unica.request

import br.com.zup.edu.ConsultarChavePixResponseGrpc
import br.com.zup.edu.ConsultarChavePixResponseGrpc.*
import br.com.zup.edu.grpc.dominio.modelo.ChavePix

class ChavePixResponseDto(
    val pixId: String = "",
    val clienteId: String = "",
    val tipoChave: String,
    val chave: String,
    val titular: TitularResponseDto,
    val conta: ContaAssociadaResponseDto,
    val registradaEm: String,
) {
    companion object {
        fun build(chavePix: ChavePix) =
            ChavePixResponseDto(
                pixId = chavePix.idInterno,
                clienteId = chavePix.clienteId,
                tipoChave = chavePix.tipoChave.name,
                chave = chavePix.chave,
                titular = TitularResponseDto(
                    nome = chavePix.conta.titular.nome,
                    cpf = chavePix.conta.titular.cpf
                ),
                conta = ContaAssociadaResponseDto(
                    instituicao = chavePix.conta.instituicao.nome,
                    agencia = chavePix.conta.agencia,
                    numero = chavePix.conta.numero,
                    tipoConta = chavePix.tipoConta.name
                ),
                registradaEm = chavePix.registradaEm.toString()
            )
    }

    fun paraResponseGrpc(): ConsultarChavePixResponseGrpc =
        newBuilder()
            .setPixId(this.pixId)
            .setClienteId(this.clienteId)
            .setTipoChave(this.tipoChave)
            .setChave(this.chave)
            .setTitular(
                Titular.newBuilder()
                    .setNome(this.titular.nome)
                    .setCpf(this.titular.cpf)
                    .build()
            )
            .setConta(
                Conta.newBuilder()
                    .setInstituicao(this.conta.instituicao)
                    .setAgencia(this.conta.agencia)
                    .setNumero(this.conta.numero)
                    .setTipoConta(this.conta.tipoConta)
                    .build()
            )
            .setRegistradaEm(this.registradaEm)
            .build()

}

class ContaAssociadaResponseDto(
    val instituicao: String,
    val agencia: String,
    val numero: String,
    val tipoConta: String,
)

class TitularResponseDto(
    val nome: String,
    val cpf: String,
)

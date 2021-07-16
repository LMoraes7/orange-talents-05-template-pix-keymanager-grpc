package br.com.zup.edu.grpc.http.client.bcb.dto.request

import br.com.zup.edu.grpc.dominio.modelo.ChavePix

class CreatePixKeyRequest(
    chavePix: ChavePix
) {

    val keyType: String = chavePix.tipoChave.keyType().name
    val key: String = chavePix.chave
    val bankAccount: BankAccountRequest = BankAccountRequest(
        participant = chavePix.conta.instituicao.ispb,
        branch = chavePix.conta.agencia,
        accountNumber = chavePix.conta.numero,
        accountType = chavePix.tipoConta.accountType().name
    )
    val owner: OwnerRequest = OwnerRequest(
        type = Type.NATURAL_PERSON.name,
        name = chavePix.conta.titular.nome,
        taxIdNumber = chavePix.conta.titular.cpf
    )


}
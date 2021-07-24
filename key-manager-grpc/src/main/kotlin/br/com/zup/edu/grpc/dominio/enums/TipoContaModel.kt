package br.com.zup.edu.grpc.dominio.enums

import br.com.zup.edu.grpc.http.client.bcb.request.AccountType

enum class TipoContaModel {

    CONTA_CORRENTE {
        override fun getAccountType(): AccountType = AccountType.CACC
    },
    CONTA_POUPANCA {
        override fun getAccountType(): AccountType = AccountType.SVGS
    };

    abstract fun getAccountType(): AccountType
}

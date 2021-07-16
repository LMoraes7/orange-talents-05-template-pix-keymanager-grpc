package br.com.zup.edu.grpc.dominio.enums

import br.com.zup.edu.grpc.http.client.bcb.dto.request.AccountType

enum class TipoContaModel {

    CONTA_CORRENTE {
        override fun accountType(): AccountType = AccountType.CACC
    },
    CONTA_POUPANCA {
        override fun accountType(): AccountType = AccountType.SVGS
    };

    abstract fun accountType(): AccountType;
}
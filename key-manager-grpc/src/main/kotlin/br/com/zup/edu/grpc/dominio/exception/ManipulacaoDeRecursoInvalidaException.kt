package br.com.zup.edu.grpc.dominio.exception

open abstract class ManipulacaoDeRecursoInvalidaException(message: String? = null) : Exception(message)

class ExclusaoDeChavePixInvalidaException(message: String? = null) : ManipulacaoDeRecursoInvalidaException(message)

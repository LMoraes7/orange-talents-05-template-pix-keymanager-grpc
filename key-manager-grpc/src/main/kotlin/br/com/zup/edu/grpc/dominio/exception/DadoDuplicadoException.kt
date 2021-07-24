package br.com.zup.edu.grpc.dominio.exception

open abstract class DadoDuplicadoException(message: String? = null): Exception(message)

class ChavePixDuplicadaException(message: String? = null) : DadoDuplicadoException(message)



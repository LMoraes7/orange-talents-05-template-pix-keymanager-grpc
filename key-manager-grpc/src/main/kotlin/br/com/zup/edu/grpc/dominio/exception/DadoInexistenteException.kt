package br.com.zup.edu.grpc.dominio.exception

open abstract class DadoInexistenteException(message: String? = null): Exception(message)

class ChavePixInexistenteException(message: String? = null) : DadoInexistenteException(message)

class ClienteInexistenteException(message: String? = null) : DadoInexistenteException(message)

class ContaInexistenteException(message: String? = null) : DadoInexistenteException(message)

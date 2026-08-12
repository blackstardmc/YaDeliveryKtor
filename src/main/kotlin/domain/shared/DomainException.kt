package com.blackneko.domain.shared

sealed class DomainException(
    message: String
) : RuntimeException(message)


class InvalidDomainStateException(
    message: String
) : DomainException(message)


class InvalidOrderStatusTransitionException(
    message: String
) : DomainException(message)


class InvalidOrderException(
    message: String
) : DomainException(message)


class ProductNotAvailableException(
    message: String
) : DomainException(message)


class RestaurantNotAvailableException(
    message: String
) : DomainException(message)


class UnauthorizedDomainOperationException(
    message: String
) : DomainException(message)
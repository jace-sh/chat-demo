package jace.shim.webfluxfun.domain.idgenerate.number

import jace.shim.webfluxfun.domain.idgenerate.IdentifierGenerator
import org.springframework.stereotype.Component

@Component
class NumberIdentifierGenerator : IdentifierGenerator<Long> {
    private val snowflake = Snowflake()

    override fun generate(): Long {
        return snowflake.nextId()
    }
}
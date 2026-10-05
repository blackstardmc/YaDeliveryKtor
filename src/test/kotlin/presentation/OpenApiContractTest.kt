package com.blackneko.presentation

import io.swagger.v3.parser.OpenAPIV3Parser
import org.junit.jupiter.api.Test
import kotlin.test.*

class OpenApiContractTest {
    @Test
    fun `OpenAPI parses without errors and documents security and money contract`() {
        val source = assertNotNull(javaClass.classLoader.getResource("openapi/documentation.json")).readText()
        val parsed = OpenAPIV3Parser().readContents(source)
        assertTrue(parsed.messages.isNullOrEmpty(), parsed.messages.joinToString("\n"))
        val api = assertNotNull(parsed.openAPI)
        assertEquals(47, api.paths.values.sumOf { it.readOperations().size })
        assertEquals("bearer", api.components.securitySchemes["BearerAuth"]?.scheme)
        val order = api.paths["/orders"]!!.post
        assertTrue(order.security.any { it.containsKey("BearerAuth") })
        assertTrue(order.parameters.any { it.name == "Idempotency-Key" && it.`in` == "header" })
        val input = api.components.schemas["OrderRequest"]!!
        assertEquals(setOf("restaurantId", "deliveryAddressId", "items", "notes"), input.properties.keys)
        assertEquals(false, input.additionalProperties)
        assertEquals("int64", api.components.schemas["OrderSummary"]!!.properties["totalCents"]?.format)
        assertTrue(api.paths["/auth/login"]!!.post.responses.containsKey("429"))
        assertTrue(api.paths["/ready"]!!.get.responses.containsKey("503"))
    }
}

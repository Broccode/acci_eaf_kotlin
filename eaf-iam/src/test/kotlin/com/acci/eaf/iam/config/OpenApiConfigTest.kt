package com.acci.eaf.iam.config

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@SpringBootTest
@AutoConfigureMockMvc
class OpenApiConfigTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Test
    fun `OpenAPI endpoint should be accessible and return valid documentation`() {
        mockMvc.perform(
            get("/v3/api-docs")
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.openapi").value("3.0.1"))
            .andExpect(jsonPath("$.info").exists())
            .andExpect(jsonPath("$.info.title").value("EAF IAM API"))
            .andExpect(jsonPath("$.paths").exists())
            // Prüfe, ob der User Management Controller-Pfad existiert
            .andExpect(jsonPath("$.paths./api/controlplane/tenants/{tenantId}/users").exists())
    }
}

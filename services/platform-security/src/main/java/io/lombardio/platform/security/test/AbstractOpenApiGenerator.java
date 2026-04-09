/*
 * Lombardio Source-Available No-Distribution License 1.0
 *
 * Copyright (c) 2026 Benjamin Letzel. All rights reserved.
 *
 * This project is source-available for educational and review purposes only.
 * Redistribution, sublicensing, or commercial use is strictly prohibited.
 *
 * For partnership or cooperation inquiries, please contact the author.
 */
package io.lombardio.platform.security.test;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Abstract base class for OpenAPI specification generation during integration tests.
 * Services can extend this to automatically export their API contracts.
 */
public abstract class AbstractOpenApiGenerator {

  /**
   * Generates the OpenAPI YAML specification and writes it to the configured output directory.
   *
   * @param mockMvc The MockMvc instance of the concrete service.
   * @param serviceName The name of the service (used for the filename).
   * @param relativeOutputPath The path relative to the monorepo root (default: frontend/app/api-spec).
   * @throws Exception If generation or file writing fails.
   */
  @SuppressFBWarnings(value = "PATH_TRAVERSAL_IN", justification = "Build-time tool using controlled properties")
  protected void generateSpec(MockMvc mockMvc, String serviceName, String relativeOutputPath) throws Exception {
    byte[] response =
        mockMvc
            .perform(get("/v3/api-docs.yaml"))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsByteArray();

    Path outputPath = Paths.get(relativeOutputPath, serviceName + "-openapi.yaml");
    
    Path parentDir = outputPath.getParent();
    if (parentDir != null) {
      Files.createDirectories(parentDir);
    }
    Files.write(outputPath, response);
  }

  protected void generateSpec(MockMvc mockMvc, String serviceName) throws Exception {
    generateSpec(mockMvc, serviceName, System.getProperty("lombardio.openapi.output.dir", "../../frontend/app/api-spec"));
  }
}

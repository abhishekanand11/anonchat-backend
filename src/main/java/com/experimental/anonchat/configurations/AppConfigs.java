package com.experimental.anonchat.configurations;

import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.embedding.EmbeddingRequest;
import org.springframework.ai.embedding.EmbeddingResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppConfigs {
    @Bean
    public EmbeddingModel embeddingModel() {
        // Replace YourEmbeddingModelImpl with the actual implementation of EmbeddingModel you are using
        return new SimpleEmbeddingModel();
    }

    // A simple in-memory embedding model (for testing purposes)
    public static class SimpleEmbeddingModel implements EmbeddingModel {

        @Override
        public EmbeddingResponse call(EmbeddingRequest request) {
            return null;
        }

        @Override
        public float[] embed(String text) {
            // Just return a dummy embedding for testing purposes
            return new float[]{1.0f, 0.0f, 0.0f};  // Dummy 3D embedding vector
        }

        @Override
        public float[] embed(Document document) {
            return new float[0];
        }
    }
}

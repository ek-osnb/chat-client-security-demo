package ek.osnb.demo.ai;

import ek.osnb.demo.ai.openai.OpenAiProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(OpenAiProperties.class)
class AiConfig {
}

package com.example.GoSonGim_BE.global.config;

import com.microsoft.cognitiveservices.speech.OutputFormat;
import com.microsoft.cognitiveservices.speech.SpeechConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AzureConfig {

    @Value("${azure.speech.key}")
    private String speechKey;

    @Value("${azure.speech.region}")
    private String speechRegion;

    @Bean
    public SpeechConfig speechConfig() {
        SpeechConfig config = SpeechConfig.fromSubscription(speechKey, speechRegion);
        config.setSpeechRecognitionLanguage("ko-KR");
        config.setOutputFormat(OutputFormat.Detailed);
        return config;
    }
}

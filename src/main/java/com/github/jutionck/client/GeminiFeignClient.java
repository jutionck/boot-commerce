package com.github.jutionck.client;

import com.github.jutionck.client.dto.GeminiDto;
import com.github.jutionck.config.GeminiFeignConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(
        name = "gemini-feign-client",
        url = "${gemini.api.url}",
        configuration = GeminiFeignConfig.class
)
public interface GeminiFeignClient {
    @PostMapping("/v1beta/models/gemini-2.5-flash:generateContent")
    GeminiDto.ApiResponse askGemini(@RequestBody GeminiDto.ApiRequest request);
}



package ek.osnb.demo.weather;


import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/prompt")
class AiWeatherController {

    private final AiWeatherService aiWeatherService;

    AiWeatherController(AiWeatherService aiWeatherService) {
        this.aiWeatherService = aiWeatherService;
    }

    record QueryRequest(String prompt) {
    }

    @PostMapping
    ResponseEntity<AiWeatherService.ResponseDto> ask(@RequestBody QueryRequest queryRequest) {
        var response = aiWeatherService.prompt(queryRequest.prompt);
        return ResponseEntity.ok(response);
    }
}

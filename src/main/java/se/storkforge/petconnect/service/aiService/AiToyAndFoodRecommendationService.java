package se.storkforge.petconnect.service.aiService;

import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AiToyAndFoodRecommendationService {
    AiRecommendationExecutor aiRecommendationExecutor;

    @Autowired
    public AiToyAndFoodRecommendationService(AiRecommendationExecutor aiRecommendationExecutor) {
        this.aiRecommendationExecutor = aiRecommendationExecutor;
    }

    public String getToyRecommendation (int age, String species){
        try{
        String promptText = java.lang.String.format("""
                Suggest 3 appropriate toys for a %s that is %d years old.
                For each toy, include a brief explanation of why it's suitable.
                Format the response with clear headings for each recommendation.
                """,species, age);
        Prompt prompt = new Prompt(promptText);
        return aiRecommendationExecutor.callAi(prompt);}
        catch (Exception e) {
            return "Sorry, we couldn't generate toy recommendations at this time. Please try again later.";
        }
}

public String getFoodRecommendation (int age, String species){
        try {String promptText = String.format("""
                Sugget 3 healthy meals for a %s that is %d years old.
                For each meal, include nutritional benefits.
                Format the response with clear headings for each recommendation.
                """,species, age);
        Prompt prompt = new Prompt(promptText);
        return aiRecommendationExecutor.callAi(prompt);} catch (Exception e) {
            return "Sorry, we couldn't generate toy recommendations at this time. Please try again later.";
        }
}

}

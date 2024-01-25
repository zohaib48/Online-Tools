package com.example.demo.Controllers;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class LineController {

    @RequestMapping("/TextManipulator")
    public String showForm() {
        return "Line";
    }

    @PostMapping("/manipulateText")
    public String manipulateText(String originalText, String manipulationOption, String appendString, Model model) {
        String manipulatedText = manipulate(originalText, manipulationOption, appendString);
        model.addAttribute("manipulatedText", manipulatedText);
        return "Line";
    }

    private String manipulate(String originalText, String manipulationOption, String appendString) {
        String[] lines = originalText.split("\\r?\\n");
    
        StringBuilder result = new StringBuilder();
    
        for (String line : lines) {
            if ("before".equals(manipulationOption)) {
                result.append(appendString).append(" ").append(line).append("\n");
            } else if ("after".equals(manipulationOption)) {
                result.append(line).append(" ").append(appendString).append("\n");
            }
        }
    
        return result.toString().trim();
    }
    
}

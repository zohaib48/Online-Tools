package com.example.demo.Controllers;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class LineController {

    @RequestMapping("/line")
    public String showForm() {
        return "Line";
    }       

    @PostMapping(value = "/manipulateText", consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public String manipulateText(@RequestParam String originalText, @RequestParam String manipulationOption, @RequestParam String appendString, Model model) {
        String manipulatedText = manipulate(originalText, manipulationOption, appendString);
        model.addAttribute("manipulatedText", manipulatedText);
        return manipulatedText;
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
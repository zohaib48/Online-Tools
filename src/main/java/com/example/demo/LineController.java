package com.example.demo;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import com.example.demo.models.TextManipulationModel;

@Controller
public class LineController {

    @RequestMapping("/line")
    public String showForm(Model model) {
        model.addAttribute("textManipulationModel", new TextManipulationModel());
        return "Line";
    }

    @PostMapping("/manipulateText")
    public String manipulateText(@ModelAttribute TextManipulationModel textManipulationModel, Model model) {
        String manipulatedText = manipulate(textManipulationModel.getOriginalText(), textManipulationModel.getManipulationOption(), textManipulationModel.getAppendString());
        textManipulationModel.setManipulatedText(manipulatedText);
        model.addAttribute("textManipulationModel", textManipulationModel);
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

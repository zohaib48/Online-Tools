package com.example.demo.models;

public class TextManipulationModel {

    private String originalText;
    private String manipulationOption;
    private String appendString;
    private String manipulatedText;

    // Constructors (default and parameterized)

    // Getters and setters for all properties

    public String getOriginalText() {
        return originalText;
    }

    public void setOriginalText(String originalText) {
        this.originalText = originalText;
    }

    public String getManipulationOption() {
        return manipulationOption;
    }

    public void setManipulationOption(String manipulationOption) {
        this.manipulationOption = manipulationOption;
    }

    public String getAppendString() {
        return appendString;
    }

    public void setAppendString(String appendString) {
        this.appendString = appendString;
    }

    public String getManipulatedText() {
        return manipulatedText;
    }

    public void setManipulatedText(String manipulatedText) {
        this.manipulatedText = manipulatedText;
    }
}

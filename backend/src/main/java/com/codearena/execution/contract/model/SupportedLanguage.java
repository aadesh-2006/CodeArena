package com.codearena.execution.contract.model;

public enum SupportedLanguage {
    JAVA("java", "Java 21"),
    PYTHON("python", "Python 3"),
    CPP("cpp", "C++17");

    private final String id;
    private final String displayName;

    SupportedLanguage(String id, String displayName) {
        this.id = id;
        this.displayName = displayName;
    }

    public String getId() {
        return id;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static SupportedLanguage fromId(String id) {
        for (SupportedLanguage lang : values()) {
            if (lang.id.equalsIgnoreCase(id) || lang.name().equalsIgnoreCase(id)) {
                return lang;
            }
        }
        throw new IllegalArgumentException("Unsupported language: " + id);
    }
}

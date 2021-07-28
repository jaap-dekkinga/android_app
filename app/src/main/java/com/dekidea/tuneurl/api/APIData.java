package com.dekidea.tuneurl.api;

public class APIData {

    private long id;
    private String name;
    private String description;
    private String type;
    private String info;
    private int matchPercentage;

    private String date;
    private long date_absolute;

    // --- CONSTRUCTORS ---

    public APIData() { }

    public APIData(long id, String name, String description, String type, String info, int matchPercentage) {

        this.id = id;
        this.name = name;
        this.description = description;
        this.type = type;
        this.info = info;
        this.matchPercentage = matchPercentage;
    }

    // --- GETTER ---

    public long getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public String getType() { return type; }
    public String getInfo() { return info; }
    public int getMatchPercentage() { return matchPercentage; }

    public String getDate() { return date; }
    public long getDateAbsulote() { return date_absolute; }

    // --- SETTER ---

    public void setId(long id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setDescription(String description) { this.description = description; }
    public void setType(String type) { this.type = type; }
    public void setInfo(String info) { this.info = info; }
    public void setMatchPercentage(int matchPercentage) { this.matchPercentage = matchPercentage; }

    public void setDate(String date) { this.date = date; }
    public void setDateAbsolute(long date_absolute) { this.date_absolute = date_absolute; }
}

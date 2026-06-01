package com.example.login;

public class Book {
    private int id;
    private String title;
    private String author;
    private String genre;
    private int year;

    private String type;
    private String topic;
    private int duration;
    private int progress;
    private boolean favorite;
    private boolean downloaded;

    public Book(int id, String title, String author, String genre, int year) {
        this(id, title, author, genre, year, "Audiobook", "General", 3600, 0, false, false);
    }

    public Book(int id, String title, String author, String genre, int year,
                String type, String topic, int duration, int progress,
                boolean favorite, boolean downloaded) {
        this.id = id;
        this.title = title;
        this.author = author;
        this.genre = genre;
        this.year = year;
        this.type = type;
        this.topic = topic;
        this.duration = duration;
        this.progress = progress;
        this.favorite = favorite;
        this.downloaded = downloaded;
    }

    public int getId() { return id; }
    public String getTitle() { return title; }
    public String getAuthor() { return author; }
    public String getGenre() { return genre; }
    public int getYear() { return year; }
    public String getType() { return type; }
    public String getTopic() { return topic; }
    public int getDuration() { return duration; }
    public int getProgress() { return progress; }
    public boolean isFavorite() { return favorite; }
    public boolean isDownloaded() { return downloaded; }

    public void setTitle(String title) { this.title = title; }
    public void setAuthor(String author) { this.author = author; }
    public void setGenre(String genre) { this.genre = genre; }
    public void setYear(int year) { this.year = year; }
    public void setType(String type) { this.type = type; }
    public void setTopic(String topic) { this.topic = topic; }
    public void setDuration(int duration) { this.duration = duration; }
    public void setProgress(int progress) { this.progress = progress; }
    public void setFavorite(boolean favorite) { this.favorite = favorite; }
    public void setDownloaded(boolean downloaded) { this.downloaded = downloaded; }
}
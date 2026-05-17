package models;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class News implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String newsId;
    private final String title;
    private final String content;
    private final String author;
    private final LocalDateTime createdAt;

    public News(String newsId, String title, String content, String author) {
        this.newsId = newsId;
        this.title = title;
        this.content = content;
        this.author = author;
        this.createdAt = LocalDateTime.now();
    }

    public String getNewsId() {
        return newsId;
    }

    public String getTitle() {
        return title;
    }

    public String getContent() {
        return content;
    }

    public String getAuthor() {
        return author;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    @Override
    public String toString() {
        return String.format("[%s] %s | by %s | %s%n%s",
                newsId,
                title,
                author,
                createdAt.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")),
                content);
    }
}

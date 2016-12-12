package com.americanminion;

/**
 * Created by Technovibe on 17-04-2015.
 */
public class ChatMessage {
    private long id;
    private boolean isMe;
    private String message;
    private Long userId;
    private String dateTime;
    private String author;
    private String tags;
    private String votes;


    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public boolean getIsme() {
        return isMe;
    }

    public void setMe(boolean isMe) {
        this.isMe = isMe;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setAuthor(String message) {
        this.author = message;
    }

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public String getDate() {
        return dateTime;
    }

    public String getAuthor() {
        return author;
    }

    public void setDate(String dateTime) {
        this.dateTime = dateTime;
    }

    public String getVotes() {return votes;}

    public void setVotes(String votes) { this.votes = votes; }


    public String getTags() {return tags;}

    public void setTags(String Tags) { this.tags = Tags; }

}

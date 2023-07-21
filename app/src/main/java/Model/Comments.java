package Model;

public class Comments {
    private String publisher;
    private String comment;
    private String commentId;

    public Comments() {
    }

    public Comments(String publisher, String comment, String commentId) {
        this.publisher = publisher;
        this.comment = comment;
        this.commentId = commentId;
    }

    public String getCommentId() {
        return commentId;
    }

    public void setCommentId(String commentId) {
        this.commentId = commentId;
    }

    public String getPublisher() {
        return publisher;
    }

    public void setPublisher(String publisher) {
        this.publisher = publisher;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }
}

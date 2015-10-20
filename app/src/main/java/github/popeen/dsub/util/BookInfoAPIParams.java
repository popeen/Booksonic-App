package github.popeen.dsub.util;

/**
 * Created by P on 2015-10-20.
 */
public class BookInfoAPIParams{
    public String url;
    public String author;
    public String title;

    public BookInfoAPIParams(String url, String author, String title){
        this.url = url;
        this.author = author;
        this.title = title;
    }

    public String getURL(){
        return url;
    }

    public String getAuthor(){
        return author;
    }

    public String getTitle(){
        return title;
    }
}

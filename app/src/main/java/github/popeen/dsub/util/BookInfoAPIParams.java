package github.popeen.dsub.util;

/**
 * Created by P on 2015-10-20.
 */
public class BookInfoAPIParams{
    public String url;
    public String author;
    public String title;
    public int year;

    public BookInfoAPIParams(String url, String author, String title, int year){
        this.url = url;
        this.author = author;
        this.title = title;
        this.year = year;
    }

    public String getURL() { return url; }

    public String getAuthor(){
        return author;
    }

    public String getTitle(){
        return title;
    }

    public int getYear() { return year; }
}

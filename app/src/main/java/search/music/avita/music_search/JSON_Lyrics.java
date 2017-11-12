package search.music.avita.music_search;

/**
 * Created by avita on 9/2/2017.
 * POJO class created in order to hold the results we get from calling lyrics.wikia.com
 */

public class JSON_Lyrics
{
    private String song;

    private String artist;

    private String lyrics;

    private String url;

    public String getSong ()
    {
        return song;
    }

    public void setSong (String song)
    {
        this.song = song;
    }

    public String getArtist ()
    {
        return artist;
    }

    public void setArtist (String artist)
    {
        this.artist = artist;
    }

    public String getLyrics ()
    {
        return lyrics;
    }

    public void setLyrics (String lyrics)
    {
        this.lyrics = lyrics;
    }

    public String getUrl ()
    {
        return url;
    }

    public void setUrl (String url)
    {
        this.url = url;
    }

    @Override
    public String toString()
    {
        return "ClassPojo [song = "+song+", artist = "+artist+", lyrics = "+lyrics+", url = "+url+"]";
    }
}


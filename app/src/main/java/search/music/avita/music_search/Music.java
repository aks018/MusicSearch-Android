package search.music.avita.music_search;

import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;

/**
 * Created by avita on 9/1/2017.
 * Music Object holding all information to be displayed to the user after querying iTunes
 */
public class Music implements Parcelable{
    //Default Constructor
    public Music()
    {

    }
    //Define Music Object with just trackname
    public Music(String trackName)
    {
        this.trackName = trackName;
    }
    public Music(String trackName, String artistName, String albumName, Bitmap albumImage)
    {
        this.trackName= trackName;
        this.artistName = artistName;
        this.albumName = albumName;
        this.albumImage = albumImage;
    }
    /*
    Getters and Setters for Music data
     */
    public String getTrackName() {
        return trackName;
    }

    public void setTrackName(String trackName) {
        this.trackName = trackName;
    }

    public String getArtistName() {
        return artistName;
    }

    public void setArtistName(String artistName) {
        this.artistName = artistName;
    }

    public String getAlbumName() {
        return albumName;
    }

    public void setAlbumName(String albumName) {
        this.albumName = albumName;
    }

    public Bitmap getAlbumImage() {
        return albumImage;
    }

    public void setAlbumImage(Bitmap albumImage) {
        this.albumImage = albumImage;
    }
    String trackName;
    String artistName;
    String albumName;
    Bitmap albumImage;


    @Override
    public int describeContents() {
        return 0;
    }
    //Writing out Music data to a parcel
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(trackName);
        dest.writeString(artistName);
        dest.writeString(albumName);
        dest.writeParcelable(albumImage,flags);

    }
    //Retrieving information
    private Music(Parcel in) {
        trackName = in.readString();
        artistName = in.readString();
        albumName = in.readString();
        albumImage= in.readParcelable(Bitmap.class.getClassLoader());
    }

    //Override toString mainly for testing purposes but might be useful to print out information
    @Override
    public String toString() {
        return "Track Name: " + trackName + "\nArtist Name: " + artistName + "\nAlbum Name: " + albumName;
    }
    //Parcelable creator allowing for passing Music object between activities
    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
        public Music createFromParcel(Parcel in) {
            return new Music(in);
        }

        public Music[] newArray(int size) {
            return new Music[size];
        }
    };
}

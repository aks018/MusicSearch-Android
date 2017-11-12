package search.music.avita.music_search;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.util.ArrayList;

/**
 * Created by avita on 9/1/2017.
 * Create a MusicBaseAdapter extending BaseAdapter allowing user to view results from iTunes search
 */

public class MusicBaseAdapter extends BaseAdapter{

    private static LayoutInflater inflater=null;
    Context context;
    ArrayList<Music> musicArrayList;

    //Constructor for MusicBaseAdapter passing in the context of the activity using the adapter and the arraylist holding the music objects
    public MusicBaseAdapter(Context mainActivity, ArrayList musicArrayList)
    {
        this.musicArrayList = musicArrayList;
        context = mainActivity;
        inflater = ( LayoutInflater )context.
                getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }
    @Override
    public int getCount() {
        return musicArrayList.size();
    }

    @Override
    public Object getItem(int position) {
        return musicArrayList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    //Create a Holder class that will hold all of our layout objects
    public class Holder
    {
        TextView trackName;
        TextView artistName;
        TextView albumName;
        ImageView albumCover;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Holder holder = new Holder();
        View rowView;  

        //Inflate the row view with layout created to store results
        rowView = inflater.inflate(R.layout.music_base_adapter, parent, false);

        holder.trackName = (TextView) rowView.findViewById(R.id.textViewSongName);
        holder.artistName = (TextView) rowView.findViewById(R.id.textViewArtistName);
        holder.albumName = (TextView) rowView.findViewById(R.id.textViewAlbumName);
        holder.albumCover = (ImageView) rowView.findViewById(R.id.imageViewAlbumCover);

        //Store the music object from the position within the arraylist
        Music musicObject = musicArrayList.get(position);

        holder.trackName.setText(musicObject.getTrackName());
        holder.artistName.setText(musicObject.getArtistName());
        holder.albumName.setText(musicObject.getAlbumName());
        holder.albumCover.setImageBitmap(musicObject.getAlbumImage());

        //Finally return rowView holding the layout for the music listview items
        return rowView;
    }
}

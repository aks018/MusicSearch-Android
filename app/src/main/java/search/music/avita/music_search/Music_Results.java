package search.music.avita.music_search;

/**
 * Created by avita on 8/31/2017.
 * POJO class which is mapping the results we get from iTunes into a Music_Results Object
 */



public class Music_Results
{
    private Results[] results;

    private String resultCount;

    public Results[] getResults ()
    {
        return results;
    }

    public void setResults (Results[] results)
    {
        this.results = results;
    }

    public String getResultCount ()
    {
        return resultCount;
    }

    public void setResultCount (String resultCount)
    {
        this.resultCount = resultCount;
    }

    @Override
    public String toString()
    {
        return "ClassPojo [results = "+results+", resultCount = "+resultCount+"]";
    }
}

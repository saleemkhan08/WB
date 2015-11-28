package in.org.whistleblower;

import com.parse.Parse;

/**
 * Created by Saleem Khan on 11/26/2015.
 */
public class WhistleBlower extends android.app.Application
{
    @Override
    public void onCreate()
    {
        super.onCreate();

        // initialize parse
        Parse.enableLocalDatastore(this);
        Parse.initialize(this, "q3ehyRaszu34hd9mrFp2rwxIoYP2WmPVLO1Aojhp", "xb4VdvieLAwlPQ9meXSWSzmrkZ0mu2GGgaGzHiV6");
    }
}


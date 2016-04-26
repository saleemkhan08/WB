package in.org.whistleblower.singletons;

import com.squareup.otto.Bus;
import com.squareup.otto.ThreadEnforcer;

public class Otto
{
    private static final Bus BUS = new Bus(ThreadEnforcer.ANY);
    public static Bus getBus(){
        return BUS;
    }
    private Otto(){}
}

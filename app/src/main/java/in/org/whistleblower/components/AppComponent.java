package in.org.whistleblower.components;

import javax.inject.Singleton;

import dagger.Component;
import in.org.whistleblower.MainActivity;
import in.org.whistleblower.adapters.IssueAdapter;
import in.org.whistleblower.fragments.MainFragment;
import in.org.whistleblower.fragments.MapFragment;
import in.org.whistleblower.modules.MiscModule;
import in.org.whistleblower.utilities.FABUtil;
import in.org.whistleblower.utilities.NavigationUtil;

@Singleton
@Component(modules = {MiscModule.class})
public interface AppComponent
{
    void inject(MainActivity mainActivity);
    void inject(MapFragment mapFragment);
    void inject(NavigationUtil navigationUtil);
    void inject(FABUtil fabUtil);
    void inject(MainFragment mainFragment);
    void inject(IssueAdapter issueAdapter);
}

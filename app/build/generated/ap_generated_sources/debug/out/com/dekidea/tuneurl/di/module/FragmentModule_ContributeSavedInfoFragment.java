package com.dekidea.tuneurl.di.module;

import android.support.v4.app.Fragment;
import com.dekidea.tuneurl.fragment.SavedInfoFragment;
import dagger.Binds;
import dagger.Module;
import dagger.Subcomponent;
import dagger.android.AndroidInjector;
import dagger.android.support.FragmentKey;
import dagger.multibindings.IntoMap;

@Module(
  subcomponents = FragmentModule_ContributeSavedInfoFragment.SavedInfoFragmentSubcomponent.class
)
public abstract class FragmentModule_ContributeSavedInfoFragment {
  private FragmentModule_ContributeSavedInfoFragment() {}

  @Binds
  @IntoMap
  @FragmentKey(SavedInfoFragment.class)
  abstract AndroidInjector.Factory<? extends Fragment> bindAndroidInjectorFactory(
      SavedInfoFragmentSubcomponent.Builder builder);

  @Subcomponent
  public interface SavedInfoFragmentSubcomponent extends AndroidInjector<SavedInfoFragment> {
    @Subcomponent.Builder
    abstract class Builder extends AndroidInjector.Builder<SavedInfoFragment> {}
  }
}

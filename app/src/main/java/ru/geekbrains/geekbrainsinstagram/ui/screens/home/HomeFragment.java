package ru.geekbrains.geekbrainsinstagram.ui.screens.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.tabs.TabLayout;

import javax.inject.Inject;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.viewpager.widget.ViewPager;
import ru.geekbrains.geekbrainsinstagram.MainApplication;
import ru.geekbrains.geekbrainsinstagram.R;
import ru.geekbrains.geekbrainsinstagram.base.BaseFragment;
import ru.geekbrains.geekbrainsinstagram.util.IActivityUtils;
import ru.geekbrains.geekbrainsinstagram.util.IFragmentUtils;

public final class HomeFragment extends BaseFragment implements IFragmentUtils.EventHandler {

    @Inject
    IActivityUtils activityUtils;

    @Inject
    IFragmentUtils fragmentUtils;

    private FloatingActionButton homeFab;
    private CoordinatorLayout homeLayout;

    public static HomeFragment newInstance() {
        return new HomeFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        inject();
    }

    @NonNull
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        fragmentUtils.init(this);
        activityUtils.setupToolbar(view.findViewById(R.id.home_toolbar));

        HomeFragmentPagerAdapter adapter = new HomeFragmentPagerAdapter(getChildFragmentManager(),
                getResources().getStringArray(R.array.home_tabs));

        ViewPager viewPager = view.findViewById(R.id.home_pager);
        viewPager.setAdapter(adapter);
        viewPager.addOnPageChangeListener(createPageChangeListener());

        TabLayout tabLayout = view.findViewById(R.id.home_tabs);
        tabLayout.setupWithViewPager(viewPager);

        homeFab = view.findViewById(R.id.home_fab);
        homeFab.hide();
        homeLayout = view.findViewById(R.id.home_layout);

        return view;
    }

    @Override
    public void setFabListener(View.OnClickListener listener) {
        homeFab.setOnClickListener(listener);
    }

    @Override
    public void makeNotifyingMessage(int messageId, int duration) {
        Snackbar.make(homeLayout, messageId, Snackbar.LENGTH_SHORT).show();
    }

    private void inject() {
        MainApplication.getApp().getComponentsManager().getFragmentComponent().inject(this);
    }

    private ViewPager.OnPageChangeListener createPageChangeListener() {
        return new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                switch (position) {
                    case 0:
                    case 1:
                        homeFab.hide();
                        break;
                    case 2:
                        homeFab.show();
                        break;
                    default:
                        throw new IllegalArgumentException("Illegal position " + position);
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        };
    }
}
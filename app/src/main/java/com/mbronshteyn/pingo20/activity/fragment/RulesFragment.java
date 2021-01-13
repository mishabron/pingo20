package com.mbronshteyn.pingo20.activity.fragment;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.mbronshteyn.pingo20.R;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link RulesFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class RulesFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private  int content[] = {R.drawable.rules_layer1, R.drawable.rules_layer2};
    private static double[] payouts;

    public RulesFragment() {
        // Required empty public constructor
    }


    public static RulesFragment newInstance(double[] payoutsParams) {
        RulesFragment fragment = new RulesFragment();
        payouts = payoutsParams;
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        if(container != null) {
            container.removeAllViews();
        }
        return inflater.inflate(R.layout.fragment_rules, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ViewPager bonusViewpager = (ViewPager) view.findViewById(R.id.rulesViewpager);
        bonusViewpager.setAdapter(new RulesFragment.CustomPagerAdapter(this));
    }

    private class CustomPagerAdapter extends PagerAdapter {

        private RulesFragment mContext;

        public CustomPagerAdapter(RulesFragment rulesFragment) {
            mContext = rulesFragment;
        }

        @Override
        public Object instantiateItem(ViewGroup collection, int position) {

            LayoutInflater inflater = LayoutInflater.from(getActivity());

            View itemView;
            if (position == 0){
                itemView = inflater.inflate(R.layout.rules_menu_item1, collection, false);
            }
            else {
                itemView = inflater.inflate(R.layout.rules_menu_item2, collection, false);
            }

            ImageView imageView = (ImageView) itemView.findViewById(R.id.imageView);
            imageView.setImageResource(content[position]);

            collection.addView(itemView);

            return itemView;
        }

        @Override
        public void destroyItem(ViewGroup collection, int position, Object view) {
            collection.removeView((View) view);
        }

        @Override
        public int getCount() {
            return content.length;
        }

        @Override
        public boolean isViewFromObject(@NonNull View view, @NonNull Object o) {
            return view == ((LinearLayout) o);
        }
    }
}
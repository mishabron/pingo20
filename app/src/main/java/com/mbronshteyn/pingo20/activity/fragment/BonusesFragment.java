package com.mbronshteyn.pingo20.activity.fragment;

import android.content.Context;
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
import android.widget.Toast;

import com.mbronshteyn.pingo20.R;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link BonusesFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class BonusesFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private  int content[] = {R.drawable.bonuses_layer1, R.drawable.bonuses_layer2};

    public BonusesFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment BonusesFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static BonusesFragment newInstance(String param1, String param2) {
        BonusesFragment fragment = new BonusesFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        if(container != null) {
            container.removeAllViews();
        }
        return inflater.inflate(R.layout.fragment_bonuses, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ViewPager bonusViewpager = (ViewPager) view.findViewById(R.id.bonusViewpager);
        bonusViewpager.setAdapter(new CustomPagerAdapter(this));
    }

    private class CustomPagerAdapter extends PagerAdapter {

        private BonusesFragment mContext;

        public CustomPagerAdapter(BonusesFragment bonusesFragment) {
            mContext = bonusesFragment;
        }

        @Override
        public Object instantiateItem(ViewGroup collection, int position) {

            LayoutInflater inflater = LayoutInflater.from(getActivity());

            View itemView = inflater.inflate(R.layout.bonus_menu_item, collection, false);

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
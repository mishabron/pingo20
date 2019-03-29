package com.mbronshteyn.pingo20.activity.fragment;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.mbronshteyn.pingo20.R;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * to handle interaction com.mbronshteyn.pingo20.events.
 * Use the {@link PingoProgressBar#newInstance} factory method to
 * create an instance of this fragment.
 */
public class PingoProgressBar extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private ImageView dot1;
    private ImageView dot2;
    private ImageView dot3;
    private ProgressIndicator progressIndicator;
    private SuccessIndicator successIndicator;
    private Thread progressThread;
    private Thread successThread;

    public PingoProgressBar() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment PingoProgressBar.
     */
    // TODO: Rename and change types and number of parameters
    public static PingoProgressBar newInstance(String param1, String param2) {
        PingoProgressBar fragment = new PingoProgressBar();
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

        View view = inflater.inflate(R.layout.fragment_progress_bar, container, false);

        dot1 = (ImageView) view.findViewById(R.id.progress1);
        dot2 = (ImageView) view.findViewById(R.id.progress2);
        dot3 = (ImageView) view.findViewById(R.id.progress3);

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        progressIndicator = new ProgressIndicator(getView());
        successIndicator = new SuccessIndicator(getView());
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    public void startProgress(){
        progressThread = new Thread(progressIndicator);
        progressThread.start();
    }

    public void stopProgress(){

        dot1.setImageResource(R.drawable.progress_blk_ring);
        dot2.setImageResource(R.drawable.progress_blk_ring);
        dot3.setImageResource(R.drawable.progress_blk_ring);

        progressThread.interrupt();
    }

    public void startSaccess(){
        successThread = new Thread(successIndicator);
        successThread.start();
    }

    public void stopSuccess(){
        successThread.interrupt();
    }

    private class ProgressIndicator implements Runnable {

        View view;

        public ProgressIndicator(View view) {
            this.view = view;
        }

        @Override
        public void run() {

            try {
                while (!Thread.currentThread().isInterrupted()) {

                    Thread.sleep(500);
                    view.post(new Runnable() {
                        @Override
                        public void run() {
                            dot1.setImageResource(R.drawable.progress_yellow);
                            dot2.setImageResource(R.drawable.progress_blk_ring);
                            dot3.setImageResource(R.drawable.progress_blk_ring);
                        }
                    });
                    Thread.sleep(500);
                    view.post(new Runnable() {
                        @Override
                        public void run() {
                            dot1.setImageResource(R.drawable.progress_blk_ring);
                            dot2.setImageResource(R.drawable.progress_yellow);
                            dot3.setImageResource(R.drawable.progress_blk_ring);
                        }
                    });
                    Thread.sleep(500);
                    view.post(new Runnable() {
                        @Override
                        public void run() {
                            dot1.setImageResource(R.drawable.progress_blk_ring);
                            dot2.setImageResource(R.drawable.progress_blk_ring);
                            dot3.setImageResource(R.drawable.progress_yellow);
                        }
                    });
                }
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        }
    }

    private class SuccessIndicator implements Runnable {

        View view;

        public SuccessIndicator(View view) {
            this.view = view;
        }

        @Override
        public void run() {

            try {
                while (!Thread.currentThread().isInterrupted()) {

                    Thread.sleep(500);
                    view.post(new Runnable() {
                        @Override
                        public void run() {
                            dot1.setImageResource(R.drawable.progress_blk_ring);
                            dot2.setImageResource(R.drawable.progress_blk_ring);
                            dot3.setImageResource(R.drawable.progress_blk_ring);
                        }
                    });
                    Thread.sleep(500);
                    view.post(new Runnable() {
                        @Override
                        public void run() {
                            dot1.setImageResource(R.drawable.progress_yellow);
                            dot2.setImageResource(R.drawable.progress_yellow);
                            dot3.setImageResource(R.drawable.progress_yellow);
                        }
                    });
                }
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        }
    }
}

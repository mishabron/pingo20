package com.mbronshteyn.pingo20.activity.fragment;

import android.app.Activity;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.AttributeSet;
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
    private FailureIndicator failureIndicator;
    private Thread progressThread;
    private Thread successThread;
    private Thread faiureThread;
    private int defaultDot;
    private int progressDot;
    private int successDot;
    private int failureDot;

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
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_progress_bar, container, false);

        dot1 = (ImageView) view.findViewById(R.id.progress1);
        dot2 = (ImageView) view.findViewById(R.id.progress2);
        dot3 = (ImageView) view.findViewById(R.id.progress3);

        dot1.setImageResource(defaultDot);
        dot2.setImageResource(defaultDot);
        dot3.setImageResource(defaultDot);

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        progressIndicator = new ProgressIndicator(getView());
        successIndicator = new SuccessIndicator(getView());
        failureIndicator = new FailureIndicator(getView());
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public void onInflate(Activity activity, AttributeSet attrs, Bundle savedInstanceState) {
        super.onInflate(activity, attrs, savedInstanceState);

        TypedArray a = activity.obtainStyledAttributes(attrs,R.styleable.FragmentDots);

        Resources res = getResources();

        String dot = (String) a.getText(R.styleable.FragmentDots_initiak);
        defaultDot = res.getIdentifier(dot , "drawable", getActivity().getPackageName());

        dot = (String) a.getText(R.styleable.FragmentDots_progress);
        progressDot = res.getIdentifier(dot , "drawable", getActivity().getPackageName());

        dot = (String) a.getText(R.styleable.FragmentDots_success);
        successDot = res.getIdentifier(dot , "drawable", getActivity().getPackageName());

        dot = (String) a.getText(R.styleable.FragmentDots_failure);
        failureDot = res.getIdentifier(dot , "drawable", getActivity().getPackageName());

        a.recycle();
    }

    public void startProgress(){
        progressThread = new Thread(progressIndicator);
        progressThread.start();
    }

    public void stopProgress(){

        dot1.setImageResource(defaultDot);
        dot2.setImageResource(defaultDot);
        dot3.setImageResource(defaultDot);

        progressThread.interrupt();
    }

    public void startSaccess(){
        successThread = new Thread(successIndicator);
        successThread.start();
    }

    public void stopSuccess(){
        successThread.interrupt();
    }

    public void startFailure(){
        faiureThread = new Thread(failureIndicator);
        faiureThread.start();
    }

    public void stopFailure(){
        faiureThread.interrupt();
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
                            dot1.setImageResource(progressDot);
                            dot2.setImageResource(defaultDot);
                            dot3.setImageResource(defaultDot);
                        }
                    });
                    Thread.sleep(500);
                    view.post(new Runnable() {
                        @Override
                        public void run() {
                            dot1.setImageResource(defaultDot);
                            dot2.setImageResource(progressDot);
                            dot3.setImageResource(defaultDot);
                        }
                    });
                    Thread.sleep(500);
                    view.post(new Runnable() {
                        @Override
                        public void run() {
                            dot1.setImageResource(defaultDot);
                            dot2.setImageResource(defaultDot);
                            dot3.setImageResource(progressDot);
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
                            dot1.setImageResource(defaultDot);
                            dot2.setImageResource(defaultDot);
                            dot3.setImageResource(defaultDot);
                        }
                    });
                    Thread.sleep(500);
                    view.post(new Runnable() {
                        @Override
                        public void run() {
                            dot1.setImageResource(successDot);
                            dot2.setImageResource(successDot);
                            dot3.setImageResource(successDot);
                        }
                    });
                }
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        }
    }

    private class FailureIndicator implements Runnable {

        View view;

        public FailureIndicator(View view) {
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
                            dot1.setImageResource(defaultDot);
                            dot2.setImageResource(defaultDot);
                            dot3.setImageResource(defaultDot);
                        }
                    });
                    Thread.sleep(500);
                    view.post(new Runnable() {
                        @Override
                        public void run() {
                            dot1.setImageResource(failureDot);
                            dot2.setImageResource(failureDot);
                            dot3.setImageResource(failureDot);
                        }
                    });
                }
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        }
    }
}

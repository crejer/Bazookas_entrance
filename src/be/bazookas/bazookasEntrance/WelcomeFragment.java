package be.bazookas.bazookasEntrance;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.ml.Ml;
import org.opencv.objdetect.CascadeClassifier;
import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.StringRes;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import be.bazookas.bazookasEntrance.R;

public class WelcomeFragment extends Fragment{

	private ArrayList<Person> persons;
	HandleXML obj;
	
	private View view;
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		view = inflater.inflate(R.layout.welcome_layout, container,false);
		getPerson(view);
		return view;
	}

	

	private void getPerson(View v) {
		 obj = new HandleXML(getString(R.string.ContentURL));
	      obj.fetchXML();
	      while(obj.parsingComplete);
	      persons = obj.getPersons();
	      TextView txtName = (TextView)v.findViewById(R.id.txtName);
	      if (persons.size()>0)
	      txtName.setText(persons.get(0).get_naam() + " from " + persons.get(0).get_company());
	}

}

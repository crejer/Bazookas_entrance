package be.bazookas.bazookasEntrance;

import java.util.ArrayList;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

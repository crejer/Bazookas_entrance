package be.bazookas.bazookasEntrance;

import java.util.ArrayList;


import android.app.Fragment;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.MediaController;
import android.widget.VideoView;
import be.bazookas.bazookasEntrance.R;

public class VideoFragment extends Fragment{
	private View v;
	private ArrayList<Video> videos = new ArrayList<Video>();
	public  void pauseVideo(){
		VideoView vw = (VideoView)v.findViewById(R.id.vidVideo);
		vw.pause();
	}
	public  void resumeVideo(){
		VideoView vw = (VideoView)v.findViewById(R.id.vidVideo);
		vw.start();
	}
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		 v = inflater.inflate(R.layout.video_layout, container,false);
		open(v);
		return v;
	}
	private HandleXML obj;
	private String URL="http://melbar.be/jeroencrevits/bazookas/demo/BazookasEntrance.xml";
	int videoNumber = 0;
	int totaVideos = videos.size();
	 public void open(View v){
	      obj = new HandleXML(URL);
	      obj.fetchXML();
	      while(obj.parsingComplete);
	      //persons = obj.getPersons();
	      videos = obj.getVideos();
	      totaVideos = videos.size();
	      playVideo(v);

	   }
	 private MediaController mc;
	private void playVideo(View v) {
		
		final VideoView vw =(VideoView) v.findViewById(R.id.vidVideo);
		
		
		vw.setVideoPath(videos.get(0).get_videoURL());
		mc = new MediaController(v.getContext());
		vw.setMediaController(mc);
		mc.setVisibility(View.GONE);
		vw.start();
		vw.setOnCompletionListener(new OnCompletionListener() {
			
			@Override
			public void onCompletion(MediaPlayer mp) {
				videoNumber ++;
				if (videoNumber>=totaVideos)
					videoNumber=0;
				vw.setVideoPath(videos.get(videoNumber).get_videoURL());				
				vw.start();
				mc.hide();
			}
		});
		
	}
}

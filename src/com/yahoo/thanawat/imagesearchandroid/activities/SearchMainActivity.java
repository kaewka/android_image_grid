package com.yahoo.thanawat.imagesearchandroid.activities;

import java.io.File;
import java.util.ArrayList;
import org.apache.commons.io.FileUtils;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.GridView;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.yahoo.thanawat.imagesearchandroid.EndlessScrollListener;
import com.yahoo.thanawat.imagesearchandroid.R;
import com.yahoo.thanawat.imagesearchandroid.SettingActivity;
import com.yahoo.thanawat.imagesearchandroid.adapters.ImageResultsAdapter;
import com.yahoo.thanawat.imagesearchandroid.models.ImageResult;


public class SearchMainActivity extends Activity {
	private EditText etQuery;
	private GridView gvResults;
	private ArrayList<ImageResult> imageResults;
	private ImageResultsAdapter aImageResults;
	private ArrayList<String> options;
	private AsyncHttpClient client = new AsyncHttpClient();

	
	
	public void onImageSearch(View v) {
	    String query = etQuery.getText().toString();
        String searchUrl = getUrl(0)+query;
	    imageResults.clear();  // clear existing image.
        makeRequest(searchUrl);
	}

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_main);
        setupViews();
		loadSettings();

        imageResults = new ArrayList<ImageResult>();
        aImageResults = new ImageResultsAdapter(this, imageResults);
        
        gvResults.setAdapter(aImageResults);

    }
    
    @Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.custom_setting, menu);
		return true;
	}
    
    private void loadSettings(){
		File fielsDir = getFilesDir();
		File optionsFile = new File(fielsDir, "searchOptions.txt");

		try {
			options = new ArrayList<String>(FileUtils.readLines(optionsFile));
		}catch(Exception ex){
			options = new ArrayList<String>();
			Log.d("ImageSearchMain", ex.getMessage());
		}

	}
    
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
		loadSettings();
	}
    
    public String getUrl(int start){
		if(options!=null && !options.isEmpty()){
			String imgSize = options.get(0);
			String imgType = options.get(1);
			String imgColor = options.get(2);
			String imgSite = options.get(3);

			return "https://ajax.googleapis.com/ajax/services/search/images?"
					+"v=1.0&rsz=8&start="+start+"&imgsz="+imgSize+"&imgtype="+imgType+
					"&imgcolor="+imgColor+"&as_sitesearch="+imgSite+"&q=";
		}else{
			return "https://ajax.googleapis.com/ajax/services/search/images?"
					+"v=1.0&rsz=8&start="+start+"&q=";
		}
	}
    
    
    private void makeRequest(String searchUrl){
        client.get(searchUrl, new JsonHttpResponseHandler(){
        	
        	@Override
        	public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
        		super.onSuccess(statusCode, headers, response);
        		Log.d("DEBUG", response.toString());
        		JSONArray imageResultsJson = null;
        		try {
        		    imageResultsJson = response.getJSONObject("responseData").getJSONArray("results");
        		    //imageResults.clear();  // clear existing image.
        		    //imageResults.addAll(ImageResult.fromJSONArray(imageResultsJson));
        		    //When you make to the adapter, it does modify the underlying data.
        		    aImageResults.addAll(ImageResult.fromJSONArray(imageResultsJson));
        		} catch (JSONException e) {
        			e.printStackTrace();
        		}
        		Log.i("INFO", imageResults.toString());
            }
        });

	}
    
    
    @Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			onSettingsClick();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
    
    public void onSettingsClick(){
    	
		Intent i = new Intent(this, SettingActivity.class);
		startActivityForResult(i, 10);
		
	}
    
    
    public void loadMore(int start){
		String query = Uri.encode(etQuery.getText().toString());		
		String url = getUrl(8*start)+query;		
		makeRequest(url);
	}
    
    
    private void setupViews(){
    	etQuery = (EditText) findViewById(R.id.etQuery);
    	gvResults = (GridView) findViewById(R.id.gvResults);
    	gvResults.setOnItemClickListener(new OnItemClickListener(){

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
                // Launch image display activity	
				// Create an intent
				Intent i = new Intent(SearchMainActivity.this, ImageDisplayActivity.class);
				
				//Get the image result to display
				ImageResult result = imageResults.get(position);
				
				// Pass image result to intent
				i.putExtra("result", result);
				
				// Launch the new activity
				startActivity(i);
			}
    		
    	});
    	
    	gvResults.setOnScrollListener(new EndlessScrollListener(){
    		@Override
    		public void onLoadMore(int page, int totalItemsCount) {
    			loadMore(page);
    		}
    		
    	});
    	
    }
}

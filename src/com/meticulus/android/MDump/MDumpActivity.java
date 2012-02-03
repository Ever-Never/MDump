package com.meticulus.android.MDump;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class MDumpActivity extends Activity {
	
	Spinner blockcombo;
	EditText edittext;
	ArrayAdapter<CharSequence> blockslist;
	ArrayList<Block> blocks;
	TextView filesizetext;
	EditText outlocation;
	String defaultoutputdir = "";
	int blockselection = 0;
	Button Startbtn;
	ProgressBar pbar;
	String curop = "";
	//EditText resulttext;
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        blockcombo = (Spinner)findViewById(R.id.blockscombo);
        filesizetext = (TextView)findViewById(R.id.filesizetext);
        blockcombo.setOnItemSelectedListener(new MyOnItemSelectedListener());
        blocks = new ArrayList<Block>();
        blockslist =new ArrayAdapter <CharSequence> (this, android.R.layout.simple_spinner_item );
        blockslist.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        outlocation = (EditText)findViewById(R.id.outlocation);
        Startbtn = (Button)findViewById(R.id.startbtn);
        //resulttext = (EditText)findViewById(R.id.result);
        
        OnClickListener click = new OnClickListener(){

			public void onClick(View v) {
				StartDump();
				
			}
        };
        Startbtn.setOnClickListener(click);
        pbar = (ProgressBar)findViewById(R.id.progressbar);
        
        SetDefaultOutputDir();
        LoadBlocks();
    }
    public void StartDump(){
		  
    	curop = "dumping";
    	File output = new File(outlocation.getText().toString());
    	File outputdir = new File(output.getParent());
    	if(outputdir.exists() == false){outputdir.mkdirs();}
    	new LongOperation().execute
    	(new String[]
    			{"su","-c","dd if=/dev/block/"+ blocks.get(blockselection).getBlockName() +
    			" of=" + outlocation.getText().toString()+
    			" bs=4096"});
	  }
    private void SetDefaultOutputDir(){
    	File outsdcard = new File("/sdcard/external_sd");
    	if(outsdcard.exists() && outsdcard.isDirectory())
    	{
    		defaultoutputdir = outsdcard.getPath();
    	}
    	else
    	{
    		defaultoutputdir = "/sdcard/";
    	}
    	outlocation.setText(defaultoutputdir +"/loading...");
    }
    private void SetOutputFile(){
    	
    }
    public class MyOnItemSelectedListener implements OnItemSelectedListener {

    	
        public void onItemSelected(AdapterView<?> parent,
            View view, int pos, long id) {
          //Toast.makeText(parent.getContext(), "Selected " +
          //   parent.getItemAtPosition(pos).toString(), Toast.LENGTH_LONG).show();
        	blockselection = pos;
          filesizetext.setText(blocks.get(pos).getFileSize());
          File output = new File(outlocation.getText().toString());
          defaultoutputdir = output.getParent();
          outlocation.setText(defaultoutputdir + "/" + blocks.get(pos).getBlockName() +".mdump");
        }

		public void onNothingSelected(AdapterView<?> arg0) {
			// TODO Auto-generated method stub
			
		}
    }
    final void LoadBlocks()
    {
    	curop = "load partitions";
    	new LongOperation().execute(new String[]{"cat", "/proc/partitions"});
    }
    final void DisableControls(){
    	blockcombo.setEnabled(false);
    	Startbtn.setEnabled(false);
    	outlocation.setEnabled(false);
    }
    final void EnableControls(){
    	blockcombo.setEnabled(true);
    	Startbtn.setEnabled(true);
    	outlocation.setEnabled(true);
    }
    private class LongOperation extends AsyncTask<String, Block, String> {
   	 
	@Override
  	  protected String doInBackground(String... params) {
  		  String result = "";
			try {
				Process p = Runtime.getRuntime().exec(params);
				BufferedReader reader = new BufferedReader(
				        new InputStreamReader(p.getInputStream()));
				int read;
				char[] buffer = new char[4096];
				StringBuffer output = new StringBuffer();
				while ((read = reader.read(buffer)) > 0) {
				    output.append(buffer, 0, read);
				}
				reader.close();
				try {
					p.waitFor();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				result = output.toString();
				String[] lines = result.split("\\n");
		  		  //edittext.setText(lines[2]);
		  		  //blocks = new Block[lines.length - 3];
		  		  for(int i =2;i<lines.length -1;i++)
		  		  {
		  			  String[] values = lines[i].split(" ");
		  			  Block block = new Block(values[values.length -1]);
		  			  block.setSize(Integer.parseInt(values[values.length -2]));
		  			  publishProgress(block);
		  		  }
				return output.toString();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return e.getMessage();
			}
			
			
  	  /* (non-Javadoc)
  	   * @see android.os.AsyncTask#onPostExecute(java.lang.Object)
  	   */
  	  }
  	  @Override
  	  protected void onPostExecute(String result) {
  	    // execution of result of Long time consuming operation
  		  
  		  if(curop.equals("load partitions")){
  			  blockcombo.setAdapter(blockslist);  
  		  }
  		  else if(curop.equals("dumping"))
  		  {
  			  File outfile = new File(outlocation.getText().toString());
  			  Toast.makeText(getBaseContext(), 
  	                result, 
  	                Toast.LENGTH_LONG).show();
  		  }
  		
  		pbar.setVisibility(ProgressBar.INVISIBLE);
  		EnableControls();
  	  }
  	 
  	  /* (non-Javadoc)
  	   * @see android.os.AsyncTask#onPreExecute()
  	   */
  	  @Override
  	  protected void onPreExecute() {
  	  // Things to be done before execution of long running operation. For example showing ProgessDialog
  		  DisableControls();
  		  pbar.setVisibility(ProgressBar.VISIBLE);
  		  //resulttext.setText("");
  	  }
  	 
  	  /* (non-Javadoc)
  	   * @see android.os.AsyncTask#onProgressUpdate(Progress[])
  	   */
  	  @Override
  	  protected void onProgressUpdate(Block... values) {
  		  blocks.add(values[0]);
  		  blockslist.add(values[0].toString());
  	   }
  	  
  	}
}
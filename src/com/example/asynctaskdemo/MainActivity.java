package com.example.asynctaskdemo;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.widget.ListView;

/**
 * 通过异步加载，避免阻塞UI线程
 * 通过LruCache,将下载的图片放到内存中
 * 通过ListView的滑动状态，判断何时加载图片
 * 
 * @author zhao
 *
 */
public class MainActivity extends ActionBarActivity {
	private static String URL="http://www.imooc.com/api/teacher?type=4&num=30";
	private ListView lv;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        lv=(ListView) findViewById(R.id.lv);
        new newsAsyncTask().execute(URL);
    }
    
    //异步加载数据
    class newsAsyncTask extends AsyncTask<String, Void, List<NewsBean>>{

		@Override
		protected List<NewsBean> doInBackground(String... params) {
			return getJsonData(params[0]);
		}

		@Override
		protected void onPostExecute(List<NewsBean> result) {
			super.onPostExecute(result);
			NewsAdapter adapter=new NewsAdapter(MainActivity.this, result,lv);
			lv.setAdapter(adapter);
		}
		

    }
    /**
     * 将JSON数据封装成NewsBean对象数据
     * @param url
     * @return
     */
    public List<NewsBean> getJsonData(String url){
    	List<NewsBean> newsBeans=new ArrayList<NewsBean>();
    	try {
			String data=readStram(new java.net.URL(url).openStream());
			JSONObject jsonObject;
			NewsBean newsBean ;
			jsonObject=new JSONObject(data);
			JSONArray jsonArray=jsonObject.getJSONArray("data");
			for(int i=0;i<jsonArray.length();i++){
			 jsonObject=(JSONObject) jsonArray.get(i);
			 newsBean=new NewsBean();
			 newsBean.newsIconURL=jsonObject.getString("picSmall");
			 newsBean.newsTitle=jsonObject.getString("name");
			 newsBean.newsContent=jsonObject.getString("description");
			 
			 //将每个NewsBean对象加入集合中
			 newsBeans.add(newsBean);
			}
			
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (JSONException e) {
			e.printStackTrace();
		}
    	return newsBeans;
    }
    /*
     * 读取json数据
     */
    public String readStram(InputStream is) {
    	String result="";
    	String line="";
    	//将字节流转换为字符流数据
    	InputStreamReader isReader=new InputStreamReader(is);
    	//将数据加入缓冲区
    	BufferedReader bf=new BufferedReader(isReader);
    	try {
			while((line=bf.readLine())!=null){
				result+=line;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
    	return result;
    }
}

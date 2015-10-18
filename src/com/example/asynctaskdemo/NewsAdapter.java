package com.example.asynctaskdemo;

import java.util.List;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

public class NewsAdapter extends BaseAdapter implements OnScrollListener{
	private LayoutInflater mInflater;
	private List<NewsBean> mList;
	private ImageLoader mImageLoader;
	private int mStart,mEnd;
	public static String[] URLS;
	private ListView mListView;
	private boolean mFirstIn;
	
	public NewsAdapter(Context context,List<NewsBean> list,ListView listView) {
		mInflater=LayoutInflater.from(context);
		mList=list;
		mListView=listView;
		mImageLoader=new ImageLoader(listView);
		URLS=new String[list.size()]; 
		
		for(int i=0;i<list.size();i++){
			URLS[i]=list.get(i).newsIconURL;
		}
		mListView.setOnScrollListener(this);
		mFirstIn=true;
	}
	@Override
	public int getCount() {
		return mList.size();
	}

	@Override
	public Object getItem(int arg0) {
		return mList.get(arg0);
	}

	@Override
	public long getItemId(int arg0) {
		return arg0;
	}

	@SuppressWarnings("unused")
	@Override
	public View getView(int position, View contenView, ViewGroup viewGroup) {
		
		ViewHolder viewHolder=null;
		if(viewHolder==null){
			viewHolder=new ViewHolder();
			contenView=mInflater.inflate(R.layout.item, null);
			viewHolder.imageView=(ImageView) contenView.findViewById(R.id.imageView);
			viewHolder.tv_title=(TextView) contenView.findViewById(R.id.tv_title);
			viewHolder.tv_content=(TextView) contenView.findViewById(R.id.tv_content);
			contenView.setTag(viewHolder);
		}else{
			viewHolder=(ViewHolder) contenView.getTag();
		}
//		viewHolder.imageView.setImageResource(R.drawable.ic_launcher);
		String url=mList.get(position).newsIconURL;
		
		//用url来给图片定位，防止错位
		viewHolder.imageView.setTag(url);
		//通过线程方式加载图片
//		new ImageLoader().showImageByThread(viewHolder.imageView,url);
		//通过AsyncTask加载图片
		mImageLoader.showImageByAsyncTask(viewHolder.imageView, url);
		viewHolder.tv_title.setText(mList.get(position).newsTitle);
		viewHolder.tv_content.setText(mList.get(position).newsContent);
		
		return contenView;
	}
	class ViewHolder{
		private  ImageView imageView;
		private TextView tv_title;
		private TextView tv_content;
		
		
	}
	//此方法会一直被调用
	@Override
	public void onScroll(AbsListView view, int firstVisibleItem,
			int visibleItemCount, int totalItemCount) {
		mStart=firstVisibleItem;
		mEnd=mStart+visibleItemCount;
		//当第一次启动时会加载第一次显示的item
		//首次加载时visibleCount为0
		System.out.println("visibleCount="+visibleItemCount);
		Log.i("onscroll", "aaaaa");
		if(mFirstIn==true&&visibleItemCount>0){
			mImageLoader.loadImages(mStart, mEnd);
			mFirstIn=false;
		}
	}
	/**
	 * 通过这种方式，加载图片原来由getView()控制改为onScrollStateChanged来控制
	 */
	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {
		
		if(scrollState==SCROLL_STATE_IDLE){
			//不滑动时加载数据
			mImageLoader.loadImages(mStart, mEnd);
		}else{
			//滑动时不加载任何数据
			mImageLoader.cancelAllTask();
			
		}
	}

}

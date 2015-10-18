package com.example.asynctaskdemo;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.support.v4.util.LruCache;
import android.util.Log;
import android.widget.ImageView;
import android.widget.ListView;

/**
 * 给imageview加载图片
 * 
 * @author zhao
 * 
 */
@SuppressLint("HandlerLeak")
public class ImageLoader {

	private LruCache<String, Bitmap> mCache;
	private ImageView mImageView;
	private String mUrl;
	private ListView mListView;
	private Set<NewsAsyncTask> mTask;

	public ImageLoader(ListView listView) {
		mListView = listView;
		mTask = new HashSet<ImageLoader.NewsAsyncTask>();
		int maxMemory = (int) Runtime.getRuntime().maxMemory();
		// 设置LRU缓存大的最大值
		int LRUcacheMaxMemory = maxMemory / 4;

		mCache = new LruCache<String, Bitmap>(LRUcacheMaxMemory) {

			@SuppressLint("NewApi")
			@Override
			protected int sizeOf(String key, Bitmap value) {
				// 在我们每次存入缓存的时候调用
				return value.getByteCount();
			}

		};

	}

	/**
	 * 停止滑动listView 时加载数据，此时将不再通过showImageByAsyncTask来加载图片，而是通过loadImages
	 * 
	 * @param start
	 * @param end
	 */
	public void loadImages(int start, int end) {
		for (int i = start; i < end; i++) {
			String url = NewsAdapter.URLS[i];
			Bitmap bitmap = getBitmapFromCache(url);
			if (bitmap == null) {
				NewsAsyncTask mAsyncTask = new NewsAsyncTask(url);
				mAsyncTask.execute(url);
				mTask.add(mAsyncTask);
			} else {
				// 通过listView获取当前ImageView
				ImageView mImageView = (ImageView) mListView
						.findViewWithTag(url);
				mImageView.setImageBitmap(bitmap);
			}
		}

	}

	// 取消加载
	public void cancelAllTask() {
		if (mTask != null) {
			for (NewsAsyncTask task : mTask) {
				task.cancel(false);
			}
		}
	}

	// 将bitmap存入缓存
	public void addBitmapToCache(String url, Bitmap bitmap) {
		mCache.put(url, bitmap);
	}

	// 将bitmap从缓存中取出来
	public Bitmap getBitmapFromCache(String url) {

		return mCache.get(url);
	}

	Handler mHandler = new Handler() {
		public void handleMessage(android.os.Message msg) {

			// 加载图片前先判断是否是当前图片的url
			if (mImageView.getTag().equals(mUrl)) {
				mImageView.setImageBitmap((Bitmap) msg.obj);
			}
		};

	};

	/**
	 * 用线程方式加载图片
	 * 
	 * @param imageView
	 * @param url
	 */
	public void showImageByThread(ImageView imageView, final String url) {
		mImageView = imageView;
		mUrl = url;

		new Thread() {

			@Override
			public void run() {
				super.run();

				Bitmap bitmap = getBitmapFromUrl(url);
				Message message = Message.obtain();
				message.obj = bitmap;
				mHandler.sendMessage(message);
				// 直接给图片bitmap会阻塞线程，需要用Handler将bitmap传递给主UI
				// mImageView.setImageBitmap(bitmap);
			}

		}.start();

	}

	/**
	 * 从url将资源图片资源转换成bitmap图像
	 * 
	 * @param url
	 * @return
	 */
	public Bitmap getBitmapFromUrl(String url) {
		Bitmap bitmap;
		try {
			URL url2 = new URL(url);
			HttpURLConnection connection = (HttpURLConnection) url2
					.openConnection();
			InputStream is = connection.getInputStream();
			BufferedInputStream bfInputStream = new BufferedInputStream(is);
			bitmap = BitmapFactory.decodeStream(bfInputStream);
			bfInputStream.close();
			is.close();
			connection.disconnect();
			return bitmap;

		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * 此方法是对单个ImageView加载图片，没有做滑动优化，后面的loadImages就是做的相关优化
	 * 
	 * @param imageView
	 * @param url
	 */
	public void showImageByAsyncTask(ImageView imageView, String url) {
		this.mImageView = imageView;
		mUrl = url;
		// 根据url从缓存中取出bitmap
		Bitmap bitmap = getBitmapFromCache(url);
		if (bitmap == null) {
			// new NewsAsyncTask(mImageView,mUrl).execute(mUrl);
			mImageView.setImageResource(R.drawable.ic_launcher);
		} else {
			mImageView.setImageBitmap(bitmap);
		}

	}

	/**
	 * 通过异步加载的方式加载图片
	 * 
	 * @author zhao
	 * 
	 */
	class NewsAsyncTask extends AsyncTask<String, Void, Bitmap> {
		ImageView mImageView;
		String mUrl2;

		public NewsAsyncTask(String url) {
			this.mUrl2 = url;
		}

		public NewsAsyncTask(ImageView mImageView, String url) {
			this.mImageView = mImageView;
			this.mUrl2 = url;
		}

		@Override
		protected Bitmap doInBackground(String... params) {

			String url = params[0];
			Bitmap bitmap = getBitmapFromUrl(url);
			// 将bitmap加入到缓存
			addBitmapToCache(mUrl2, bitmap);
			return bitmap;
		}

		// doinbackground返回值作为参数传给onpostExecute
		@Override
		protected void onPostExecute(Bitmap result) {
			super.onPostExecute(result);
			// 先判断图片的url是否是自己的，防止图片加载错位
			// if(mImageView.getTag().equals(mUrl2)){
			// mImageView.setImageBitmap(result);
			// }

			ImageView imageView = (ImageView) mListView.findViewWithTag(mUrl2);
			if (imageView != null && mUrl2 != null) {
				imageView.setImageBitmap(result);
			}
			mTask.remove(this);
		}

	}
}

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
 * ��imageview����ͼƬ
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
		// ����LRU���������ֵ
		int LRUcacheMaxMemory = maxMemory / 4;

		mCache = new LruCache<String, Bitmap>(LRUcacheMaxMemory) {

			@SuppressLint("NewApi")
			@Override
			protected int sizeOf(String key, Bitmap value) {
				// ������ÿ�δ��뻺���ʱ�����
				return value.getByteCount();
			}

		};

	}

	/**
	 * ֹͣ����listView ʱ�������ݣ���ʱ������ͨ��showImageByAsyncTask������ͼƬ������ͨ��loadImages
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
				// ͨ��listView��ȡ��ǰImageView
				ImageView mImageView = (ImageView) mListView
						.findViewWithTag(url);
				mImageView.setImageBitmap(bitmap);
			}
		}

	}

	// ȡ������
	public void cancelAllTask() {
		if (mTask != null) {
			for (NewsAsyncTask task : mTask) {
				task.cancel(false);
			}
		}
	}

	// ��bitmap���뻺��
	public void addBitmapToCache(String url, Bitmap bitmap) {
		mCache.put(url, bitmap);
	}

	// ��bitmap�ӻ�����ȡ����
	public Bitmap getBitmapFromCache(String url) {

		return mCache.get(url);
	}

	Handler mHandler = new Handler() {
		public void handleMessage(android.os.Message msg) {

			// ����ͼƬǰ���ж��Ƿ��ǵ�ǰͼƬ��url
			if (mImageView.getTag().equals(mUrl)) {
				mImageView.setImageBitmap((Bitmap) msg.obj);
			}
		};

	};

	/**
	 * ���̷߳�ʽ����ͼƬ
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
				// ֱ�Ӹ�ͼƬbitmap�������̣߳���Ҫ��Handler��bitmap���ݸ���UI
				// mImageView.setImageBitmap(bitmap);
			}

		}.start();

	}

	/**
	 * ��url����ԴͼƬ��Դת����bitmapͼ��
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
	 * �˷����ǶԵ���ImageView����ͼƬ��û���������Ż��������loadImages������������Ż�
	 * 
	 * @param imageView
	 * @param url
	 */
	public void showImageByAsyncTask(ImageView imageView, String url) {
		this.mImageView = imageView;
		mUrl = url;
		// ����url�ӻ�����ȡ��bitmap
		Bitmap bitmap = getBitmapFromCache(url);
		if (bitmap == null) {
			// new NewsAsyncTask(mImageView,mUrl).execute(mUrl);
			mImageView.setImageResource(R.drawable.ic_launcher);
		} else {
			mImageView.setImageBitmap(bitmap);
		}

	}

	/**
	 * ͨ���첽���صķ�ʽ����ͼƬ
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
			// ��bitmap���뵽����
			addBitmapToCache(mUrl2, bitmap);
			return bitmap;
		}

		// doinbackground����ֵ��Ϊ��������onpostExecute
		@Override
		protected void onPostExecute(Bitmap result) {
			super.onPostExecute(result);
			// ���ж�ͼƬ��url�Ƿ����Լ��ģ���ֹͼƬ���ش�λ
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

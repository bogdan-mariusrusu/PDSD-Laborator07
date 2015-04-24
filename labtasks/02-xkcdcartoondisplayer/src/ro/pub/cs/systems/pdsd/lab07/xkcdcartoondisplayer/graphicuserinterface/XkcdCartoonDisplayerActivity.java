package ro.pub.cs.systems.pdsd.lab07.xkcdcartoondisplayer.graphicuserinterface;

import java.io.IOException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import ro.pub.cs.systems.pdsd.lab07.xkcdcartoondisplayer.R;
import ro.pub.cs.systems.pdsd.lab07.xkcdcartoondisplayer.entities.XkcdCartoonInfo;
import ro.pub.cs.systems.pdsd.xkcdcartoondisplayer.general.Constants;
import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

public class XkcdCartoonDisplayerActivity extends Activity {
	
	private TextView xkcdCartoonTitleTextView;
	private ImageView xkcdCartoonImageView;
	private TextView xkcdCartoonUrlTextView;
	private Button previousButton, nextButton;
	
	private class XkcdCartoonUrlButtonClickListener implements Button.OnClickListener {
		
		String xkcdComicUrl;
		
		public XkcdCartoonUrlButtonClickListener(String xkcdComicUrl) {
			this.xkcdComicUrl = xkcdComicUrl;
		}
		
		@Override
		public void onClick(View view) {
			new XkcdCartoonDisplayerAsyncTask().execute(xkcdComicUrl);
		}
	}
	
	private class XkcdCartoonDisplayerAsyncTask extends AsyncTask<String, Void, XkcdCartoonInfo> {

		@Override
		protected XkcdCartoonInfo doInBackground(String... urls) {
			
			XkcdCartoonInfo xkcdCartoonInfo = new XkcdCartoonInfo();
			
			// TODO: exercise 5a)
			// 1. obtain the content of the web page (whose Internet address is stored in urls[0])
			// - create an instance of a HttpClient object
			HttpClient httpClient = new DefaultHttpClient();
			// - create an instance of a HttpGet object
			HttpGet httpXkcdGet = new HttpGet(urls[0]);
			// - create an instance of a ResponseHandler object
			ResponseHandler<String> responseHandlerGet = new BasicResponseHandler();
			// - execute the request, thus obtaining the web page source code
			String pageSourceCode = null;
			try {
				pageSourceCode = httpClient.execute(httpXkcdGet, responseHandlerGet);
			} catch (ClientProtocolException clientProtocolException) {
				Log.e(Constants.TAG, clientProtocolException.getMessage());
				if (Constants.DEBUG) {
					clientProtocolException.printStackTrace();
				}
			} catch (IOException ioException) {
				Log.e(Constants.TAG, ioException.getMessage());
				if (Constants.DEBUG) {
					ioException.printStackTrace();
				}
			}
			// 2. parse the web page source code
			if (pageSourceCode != null) {
				Document document = Jsoup.parse(pageSourceCode);
				Element htmlTag = document.child(0);
			// - cartoon title: get the tag whose id equals "ctitle"
				Element divTagIdCtitle = htmlTag.getElementsByAttributeValue(Constants.ID_ATTRIBUTE, Constants.CTITLE_VALUE).first();
				xkcdCartoonInfo.setCartoonTitle(divTagIdCtitle.ownText());
			// - cartoon url
			//   * get the first tag whose id equals "comic"
				Element divComicTag = htmlTag.getElementById("comic");
			//   * get the embedded <img> tag
				Element imgTag = divComicTag.child(0);
				// or 
			//  Element imgTag = divComicTag.getElementsByTag("img").first();
			//   * get the value of the attribute "src"
			//   * prepend the protocol: "http:"
				String url = Constants.HTTP_PROTOCOL + imgTag.attr("src");
				xkcdCartoonInfo.setCartoonUrl(url);
			// - cartoon content: get the input stream attached to the url and decode it into a Bitmap
				try {
					HttpGet httpCartoonGet = new HttpGet(url);
					HttpResponse httpGetResponse = httpClient.execute(httpCartoonGet);
					HttpEntity httpGetEntity = httpGetResponse.getEntity();
					xkcdCartoonInfo.setCartoonContent(BitmapFactory.decodeStream(httpGetEntity.getContent()));
				} catch (ClientProtocolException clientProtocolException) {
					Log.e(Constants.TAG, clientProtocolException.getMessage());
					if (Constants.DEBUG) {
						clientProtocolException.printStackTrace();
					}
				}
				catch (IOException ioException) {
					Log.e(Constants.TAG, ioException.getMessage());
					if (Constants.DEBUG) {
						ioException.printStackTrace();
					}
				}
			// - previous cartoon address
			//   * get the first tag whole rel attribute equals "prev"
			//   * get the href attribute of the tag
				Element aTagRelPrev = htmlTag.getElementsByAttributeValue(Constants.REL_ATTRIBUTE, Constants.PREVIOUS_VALUE).first();
			//   * prepend the value with the base url: http://www.xkcd.com
				String previousCartoonInternetAddress = Constants.XKCD_INTERNET_ADDRESS + aTagRelPrev.attr(Constants.HREF_ATTRIBUTE);
			//   * attach the previous button a click listener with the address attached
				previousButton.setOnClickListener(new XkcdCartoonUrlButtonClickListener(previousCartoonInternetAddress));
			// - next cartoon address
			//   * get the first tag whole rel attribute equals "next"
			//   * get the href attribute of the tag
				Element aTagRelNext = htmlTag.getElementsByAttributeValue(Constants.REL_ATTRIBUTE, Constants.NEXT_VALUE).first();
			//   * prepend the value with the base url: http://www.xkcd.com
				String nextCartoonInternetAddress = Constants.XKCD_INTERNET_ADDRESS + aTagRelNext.attr(Constants.HREF_ATTRIBUTE);
			//   * attach the next button a click listener with the address attached
				nextButton.setOnClickListener(new XkcdCartoonUrlButtonClickListener(nextCartoonInternetAddress));
			}
			
			return xkcdCartoonInfo;

		}
		
		@Override
		protected void onPostExecute(XkcdCartoonInfo xkcdCartoonInfo) {
			
			// TODO: exercise 5b)
			// map each member of xkcdCartoonInfo object to the corresponding widget
			if (xkcdCartoonInfo != null) {
				// cartoonTitle -> xkcdCartoonTitleTextView
				String cartoonTitle = xkcdCartoonInfo.getCartoonTitle();
				if (cartoonTitle != null) {
					xkcdCartoonTitleTextView.setText(cartoonTitle);
				}
				// cartoonContent -> xkcdCartoonImageView
				Bitmap cartoonContent = xkcdCartoonInfo.getCartoonContent();
				if (cartoonContent != null) {
					xkcdCartoonImageView.setImageBitmap(cartoonContent);
				}
				// cartoonUrl -> xkcdCartoonUrlTextView
				String cartoonUrl = xkcdCartoonInfo.getCartoonUrl();
				if (cartoonUrl != null) {
					xkcdCartoonUrlTextView.setText(cartoonUrl);
				}
			}
		}
		
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_xkcd_cartoon_displayer);
		
		xkcdCartoonTitleTextView = (TextView)findViewById(R.id.xkcd_cartoon_title_text_view);
		xkcdCartoonImageView = (ImageView)findViewById(R.id.xkcd_cartoon_image_view);
		xkcdCartoonUrlTextView = (TextView)findViewById(R.id.xkcd_cartoon_url_text_view);
		
		previousButton = (Button)findViewById(R.id.previous_button);
		nextButton = (Button)findViewById(R.id.next_button);
		
		new XkcdCartoonDisplayerAsyncTask().execute(Constants.XKCD_INTERNET_ADDRESS);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.xkcd_cartoon_displayer, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
}

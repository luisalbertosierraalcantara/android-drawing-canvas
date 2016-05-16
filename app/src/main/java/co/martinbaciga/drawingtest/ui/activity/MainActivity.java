package co.martinbaciga.drawingtest.ui.activity;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.firebase.client.DataSnapshot;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;

import org.xdty.preference.colorpicker.ColorPickerDialog;
import org.xdty.preference.colorpicker.ColorPickerSwatch;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import co.martinbaciga.drawingtest.R;
import co.martinbaciga.drawingtest.domain.application.DrawingCanvasApplication;
import co.martinbaciga.drawingtest.domain.manager.FileManager;
import co.martinbaciga.drawingtest.domain.manager.PermissionManager;
import co.martinbaciga.drawingtest.ui.component.DrawingView;
import co.martinbaciga.drawingtest.ui.component.StickerTextView;
import co.martinbaciga.drawingtest.ui.dialog.StrokeSelectorDialog;
import co.martinbaciga.drawingtest.ui.dialog.TextDialog;
import co.martinbaciga.drawingtest.ui.util.UiUtils;

public class MainActivity extends AppCompatActivity
{
	@Bind(R.id.container) FrameLayout mContainer;
	@Bind(R.id.main_drawing_view) DrawingView mDrawingView;
	@Bind(R.id.main_fill_iv) ImageView mFillBackgroundImageView;
	@Bind(R.id.main_text_iv) ImageView mTextImageView;
	@Bind(R.id.main_color_iv) ImageView mColorImageView;
	@Bind(R.id.main_stroke_iv) ImageView mStrokeImageView;
	@Bind(R.id.main_undo_iv) ImageView mUndoImageView;
	@Bind(R.id.main_redo_iv) ImageView mRedoImageView;

	private static final int MAX_STROKE_WIDTH = 50;

	//private ValueEventListener mConnectedListener;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		ButterKnife.bind(this);

		StickerTextView tv_sticker = new StickerTextView(MainActivity.this);
		tv_sticker.setText("Lorem ipsum dolor sit amet, consectetur adipisicing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua.");
		mContainer.addView(tv_sticker);

		initDrawingView();
	}

	@Override
	protected void onStart()
	{
		super.onStart();
		/*mConnectedListener = DrawingCanvasApplication.getInstance()
				.getFirebaseRef().getRoot().child(".info/connected").addValueEventListener(new ValueEventListener() {
			@Override
			public void onDataChange(DataSnapshot dataSnapshot) {
				boolean connected = (Boolean) dataSnapshot.getValue();
				if (connected) {
					Toast.makeText(MainActivity.this, "Connected to Firebase", Toast.LENGTH_SHORT).show();
					if (mDrawingView != null)
					{
						mDrawingView.syncBoard();
					}
				} else {
					Toast.makeText(MainActivity.this, "Disconnected from Firebase", Toast.LENGTH_SHORT).show();
				}
			}

			@Override
			public void onCancelled(FirebaseError firebaseError) {
				// No-op
			}
		});*/
	}

	@Override
	public void onStop() {
		super.onStop();
		/*DrawingCanvasApplication.getInstance()
				.getFirebaseRef().getRoot().child(".info/connected").removeEventListener(mConnectedListener);
		if (mDrawingView != null) {
			mDrawingView.clearListeners();
		}*/
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		getMenuInflater().inflate(R.menu.menu_main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch (item.getItemId())
		{
			case R.id.action_share:
				requestPermissionsAndSaveBitmap();
				break;
			case R.id.action_clear:
				mDrawingView.clearCanvas();
				break;
		}

		return super.onOptionsItemSelected(item);
	}

	private void initDrawingView()
	{
		mDrawingView.setEnabled(false);
	}

	private void startFillBackgroundDialog()
	{
		int[] colors = getResources().getIntArray(R.array.palette);

		ColorPickerDialog dialog = ColorPickerDialog.newInstance(R.string.color_picker_default_title,
				colors,
				mDrawingView.getBackgroundColor(),
				5,
				ColorPickerDialog.SIZE_SMALL);

		dialog.setOnColorSelectedListener(new ColorPickerSwatch.OnColorSelectedListener()
		{

			@Override
			public void onColorSelected(int color)
			{
				mDrawingView.setBackgroundColor(color);
			}

		});

		dialog.show(getFragmentManager(), "ColorPickerDialog");
	}

	private void startTextDialog()
	{
		TextDialog dialog = TextDialog.newInstance(null);

		dialog.setOnTextSettedListener(new TextDialog.OnTextSettedListener()
		{
			@Override
			public void onTextSetted(String text)
			{
				StickerTextView tv_sticker = new StickerTextView(MainActivity.this);
				tv_sticker.setText(text);
				mContainer.addView(tv_sticker);
			}
		});

		dialog.show(getSupportFragmentManager(), "TextDialog");
	}

	private void startColorPickerDialog()
	{
		int[] colors = getResources().getIntArray(R.array.palette);

		ColorPickerDialog dialog = ColorPickerDialog.newInstance(R.string.color_picker_default_title,
				colors,
				mDrawingView.getPaintColor(),
				5,
				ColorPickerDialog.SIZE_SMALL);

		dialog.setOnColorSelectedListener(new ColorPickerSwatch.OnColorSelectedListener()
		{

			@Override
			public void onColorSelected(int color)
			{
				mDrawingView.setPaintColor(color);
			}

		});

		dialog.show(getFragmentManager(), "ColorPickerDialog");
	}

	private void startStrokeSelectorDialog()
	{
		StrokeSelectorDialog dialog = StrokeSelectorDialog.newInstance(mDrawingView.getStrokeWidth(), MAX_STROKE_WIDTH);

		dialog.setOnStrokeSelectedListener(new StrokeSelectorDialog.OnStrokeSelectedListener()
		{
			@Override
			public void onStrokeSelected(int stroke)
			{
				mDrawingView.setStrokeWidth(stroke);
			}
		});

		dialog.show(getSupportFragmentManager(), "StrokeSelectorDialog");
	}

	private void startShareDialog(Uri uri)
	{
		Intent intent = new Intent();
		intent.setAction(Intent.ACTION_SEND);
		intent.setType("image/*");

		intent.putExtra(android.content.Intent.EXTRA_SUBJECT, "");
		intent.putExtra(android.content.Intent.EXTRA_TEXT, "");
		intent.putExtra(Intent.EXTRA_STREAM, uri);
		startActivity(Intent.createChooser(intent, "Share Image"));
	}

	private void requestPermissionsAndSaveBitmap()
	{
		if (PermissionManager.checkWriteStoragePermissions(this))
		{
			//Uri uri = FileManager.saveBitmap(mDrawingView.getBitmap());
			Uri uri = FileManager.saveBitmap(UiUtils.getBitmapFromView(mContainer));
			startShareDialog(uri);
		}
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults)
	{
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);
		switch (requestCode)
		{
			case PermissionManager.REQUEST_WRITE_STORAGE:
			{
				if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
				{
					Uri uri = FileManager.saveBitmap(mDrawingView.getBitmap());
					startShareDialog(uri);
				} else
				{
					Toast.makeText(this, "The app was not allowed to write to your storage. Hence, it cannot function properly. Please consider granting it this permission", Toast.LENGTH_LONG).show();
				}
			}
		}
	}

	@OnClick(R.id.main_fill_iv)
	public void onBackgroundFillOptionClick()
	{
		startFillBackgroundDialog();
	}

	@OnClick(R.id.main_text_iv)
	public void onTextOptionClick()
	{
		startTextDialog();
	}

	@OnClick(R.id.main_color_iv)
	public void onColorOptionClick()
	{
		startColorPickerDialog();
	}

	@OnClick(R.id.main_stroke_iv)
	public void onStrokeOptionClick()
	{
		startStrokeSelectorDialog();
	}

	@OnClick(R.id.main_undo_iv)
	public void onUndoOptionClick()
	{
		mDrawingView.undo();
	}

	@OnClick(R.id.main_redo_iv)
	public void onRedoOptionClick()
	{
		mDrawingView.redo();
	}
}

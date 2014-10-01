package com.cartalk;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle; 
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.AnticipateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.RelativeLayout;

import org.achartengine.GraphicalView;
import com.cartalk.view.DialChart;

import com.cartalk.view.InOutImageButton;
import com.cartalk.view.animation.ComposerButtonAnimation;
import com.cartalk.view.animation.ComposerButtonGrowAnimationIn;
import com.cartalk.view.animation.ComposerButtonGrowAnimationOut;
import com.cartalk.view.animation.ComposerButtonShrinkAnimationOut;
import com.cartalk.view.animation.InOutAnimation;

public class MainActivity extends Activity {

	private boolean		areButtonsShowing;
	private ViewGroup	composerButtonsWrapper;
	private View		composerButtonsShowHideButton;
	private Animation	rotateStoryAddButtonIn;
	private Animation	rotateStoryAddButtonOut;
	private ComposerLauncher composerlauncher=null;
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		composerButtonsWrapper = (ViewGroup) findViewById(R.id.composer_buttons_wrapper);
		composerButtonsShowHideButton = findViewById(R.id.composer_buttons_show_hide_button);
		rotateStoryAddButtonIn = AnimationUtils.loadAnimation(this,
				R.anim.rotate_story_add_button_in);
		rotateStoryAddButtonOut = AnimationUtils.loadAnimation(this,
				R.anim.rotate_story_add_button_out);
		//
		composerButtonsShowHideButton.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				toggleComposerButtons();
			}
		});
		
		for (int i = 0; i < composerButtonsWrapper.getChildCount(); i++) {
			View btv=composerButtonsWrapper.getChildAt(i);
			LauncherRunnable runnable=new LauncherRunnable();
			switch(btv.getId())
			{
				case R.id.menurealtime:
					composerlauncher=new ComposerLauncher(RealTimeInfoActivity.class,runnable);
					break;
				case R.id.menusetting:
					composerlauncher=new ComposerLauncher(ConfigActivity.class,runnable);
					break;
				case R.id.menunavigation:
					composerlauncher=new ComposerLauncher(NavigationActivity.class,runnable);
					break;
				case R.id.menuerrorcheck:
					composerlauncher=new ComposerLauncher(CheckActivity.class,runnable);
					break;
				case R.id.menubreakrules:
					composerlauncher=new ComposerLauncher(WatchListActivity.class,runnable);
					break;
				case R.id.menupeople:
					composerlauncher=new ComposerLauncher(LoginActivity.class,runnable);
					break;
			    default:
			    	composerlauncher=new ComposerLauncher(null,runnable);
					break;
			}
			btv.setOnClickListener(composerlauncher);
		}
		composerButtonsShowHideButton
				.startAnimation(new ComposerButtonGrowAnimationIn(200));
	}

	private void reshowComposer() {
		Animation growIn = new ComposerButtonGrowAnimationIn(300);
		growIn.setInterpolator(new OvershootInterpolator(2.0F));
		this.composerButtonsShowHideButton.startAnimation(growIn);
	}

	private void toggleComposerButtons() {
		if (!areButtonsShowing) {
			ComposerButtonAnimation.startAnimations(
					this.composerButtonsWrapper, InOutAnimation.Direction.IN);
		} else {
			ComposerButtonAnimation.startAnimations(
					this.composerButtonsWrapper, InOutAnimation.Direction.OUT);
		}
		areButtonsShowing = !areButtonsShowing;
	}

	public class ComposerLauncher implements View.OnClickListener {

		public final Runnable					DEFAULT_RUN	= new Runnable() {

																public void run() {
																	MainActivity.this
																			.startActivityForResult(
																					new Intent(
																							MainActivity.this,
																							MainActivity.ComposerLauncher.this.cls),
																					1);
																}
															};
		private final Class<? extends Activity>	cls;
		private final Runnable					runnable;

		private ComposerLauncher(Class<? extends Activity> c, LauncherRunnable runnable) {
			this.cls = c;
			runnable.setLaucher(this);
			this.runnable = runnable;
		}

		public void onClick(View paramView) {
			MainActivity.this.startComposerButtonClickedAnimations(
					paramView, runnable);
		}
	}

	private void startComposerButtonClickedAnimations(View view,
			final Runnable runnable) {
		this.areButtonsShowing = false;
		//Animation shrinkOut1 = new ComposerButtonShrinkAnimationOut(300);
		//Animation shrinkOut2 = new ComposerButtonShrinkAnimationOut(300);
		Animation growOut = new ComposerButtonGrowAnimationOut(300);
		//shrinkOut1.setInterpolator(new AnticipateInterpolator(2.0F));
		growOut.setAnimationListener(new Animation.AnimationListener() {

			public void onAnimationEnd(Animation animation) {
				if (runnable != null) {
					runnable.run();
				}
			}

			public void onAnimationRepeat(Animation animation) {}

			public void onAnimationStart(Animation animation) {}
		});
		//this.composerButtonsShowHideButton.startAnimation(shrinkOut1);
		for (int i = 0; i < this.composerButtonsWrapper.getChildCount(); i++) {
			final View button = this.composerButtonsWrapper.getChildAt(i);
			if (!(button instanceof InOutImageButton))
				continue;
			if (button.getId() != view.getId())
				//button.setAnimation(shrinkOut2);
				continue;
			else {
				button.startAnimation(growOut);
			}
		}
	}
	protected void onActivityResult(int requestCode, int resultCode, Intent data){
		super.onActivityResult(requestCode, resultCode, data);
		toggleComposerButtons();
	}
}

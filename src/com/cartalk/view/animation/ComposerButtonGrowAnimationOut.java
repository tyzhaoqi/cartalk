package com.cartalk.view.animation;


import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.ScaleAnimation;

public class ComposerButtonGrowAnimationOut extends InOutAnimation {

	public ComposerButtonGrowAnimationOut(int i) {
		super(InOutAnimation.Direction.OUT, i, new View[0]);
	}

	@Override
	protected void addInAnimation(View[] aview) {}

	@Override
	protected void addOutAnimation(View[] aview) {
		addAnimation(new ScaleAnimation(1F, 5F, 1F, 5F, 1, 0.5F, 1, 0.5F));
		addAnimation(new AlphaAnimation(1F, 0F));
	}

}


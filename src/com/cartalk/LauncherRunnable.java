package com.cartalk;

import com.cartalk.MainActivity.ComposerLauncher;

public class LauncherRunnable implements Runnable {
	private ComposerLauncher mComposerLauncher=null;
	public void setLaucher(ComposerLauncher auncher){
		mComposerLauncher=auncher;
	}
	public void run() {
		// TODO Auto-generated method stub
		if(mComposerLauncher!=null){
		    new Thread(new Runnable() {

			    public void run() {
				/*try {
					Thread.sleep(400);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}*/
				//reshowComposer();
				    new Thread(mComposerLauncher.DEFAULT_RUN).start();
			    }
		    }).start();
		}
	}

}

package com.cartalk;
import com.cartalk.io.ObdCommandJob;

public interface IPostListener {
	void stateUpdate(ObdCommandJob job);
	void stateUpdate(int command,float value);
}

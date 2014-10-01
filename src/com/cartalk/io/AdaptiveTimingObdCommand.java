package com.cartalk.io;
import eu.lighthouselabs.obd.commands.ObdCommand;


/**
 * This will set the value of time in milliseconds (ms) that the OBD interface
 * will wait for a response from the ECU. If exceeds, the response is "NO DATA".
 */
public class AdaptiveTimingObdCommand extends ObdCommand {

  /**
   * @param timeout
   *          value between 0 and 255 that multiplied by 4 results in the
   *          desired timeout in milliseconds (ms).
   */
  public AdaptiveTimingObdCommand(int model) {
    super("AT AT " + model);
  }

  /**
   * @param other
   */
  public AdaptiveTimingObdCommand(AdaptiveTimingObdCommand other) {
    super(other);
  }

  @Override
  public String getFormattedResult() {
    return getResult();
  }

  @Override
  public String getName() {
    return "Adaptive Timing";
  }

}
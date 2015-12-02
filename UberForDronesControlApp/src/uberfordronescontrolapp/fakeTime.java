/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package uberfordronescontrolapp;

/**
 *
 * @author Volts
 */
public class fakeTime {
   
   private int hours;
   private int minutes;
   private int seconds;
   
   public fakeTime() {
      hours = 0;
      minutes = 0;
      seconds = 0;
   }
   
   public fakeTime(int hours, int minutes, int seconds) {
      this.hours = hours;
      this.minutes = minutes;
      this.seconds = seconds;
      fixValues();
   }
   
   /**
    * Add time values to the fakeTime object. Will automatically adjust values
    * if they overflow.
    * @param hoursIn -
    * @param minutesIn -
    * @param secondsIn -
    */
   public void increment(int hoursIn, int minutesIn, int secondsIn) {
      seconds += secondsIn;
      minutes += minutesIn;
      hours += hoursIn;
      fixValues();
   }
   
   private void fixValues() {
      minutes += (seconds / 60);
      hours += (minutes / 60);
      seconds = seconds % 60;
      minutes = minutes % 60;
      hours = hours % 24;
   }
   
   @Override
   public String toString() {
      return hours + ":" + minutes + ":" + seconds;
   }
}
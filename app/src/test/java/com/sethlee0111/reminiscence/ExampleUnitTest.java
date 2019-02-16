package com.sethlee0111.reminiscence;

import android.util.Log;

import org.junit.Test;

import java.util.concurrent.ExecutionException;

import static org.junit.Assert.*;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    @Test
    public void addition_isCorrect() {
        assertEquals(4, 2 + 2);
    }
    @Test
    public void weatherWatch_Test() {
        WeatherWatchTask task = new WeatherWatchTask();
        String res = null;
        try {
            res = task.execute(30.0d, -97.0d).get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        if(res != null)
            Log.d("Result Weather", res);
    }
}
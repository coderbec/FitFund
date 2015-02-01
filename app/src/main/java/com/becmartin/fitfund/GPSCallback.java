package com.becmartin.fitfund;

/**
 * Created by sexybexy on 1/31/15.
 */
import android.location.Location;

public interface GPSCallback
{
    public abstract void onGPSUpdate(Location location);
}

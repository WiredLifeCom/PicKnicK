package wiredlife.com.picknick.utility;

import android.util.Log;

import com.wiredlife.jsonformatjava.model.unload.User;
import com.wiredlife.jsonformatjava.model.unload.Unload;
import com.wiredlife.jsonformatjava.model.unload.Zone;
import com.wiredlife.jsonformatjava.utility.Lock;

import org.joda.time.DateTime;

import java.util.HashMap;
import java.util.Map;

public class MiningModule implements Runnable {
    private Lock lock;

    private Map<String, Integer> cooldownMappings;

    private Zone currentZone;

    private Unload unload;
    private User user;

    public MiningModule(User user) {
        this.user = user;

        this.lock = new Lock();

        this.cooldownMappings = new HashMap<String, Integer>();
        this.cooldownMappings.put(null, 5000);
        this.cooldownMappings.put("Wood", 5000);
        this.cooldownMappings.put("Dirt", 5000);
        this.cooldownMappings.put("Stone", 5000);

        this.currentZone = new Zone();

        this.unload = new Unload();
        this.unload.setUser(user);
    }

    @Override
    public void run() {
        try {
            lock.lock();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        while (!Thread.currentThread().isInterrupted()) {
            Log.i("run", "Inside run...");

            if (this.currentZone.getArrival() != null && this.currentZone.getDeparture() == null) {
                if (this.currentZone.getNumberOfMaterialBlocks() <= 0) {
                    Log.i("run-EmptyBlocks", "No blocks left, stopping mining...");
                    leaveZone();
                } else {
                    this.unload.addMaterial(this.currentZone.getMaterial());
                    this.currentZone.setNumberOfMaterialBlocks(this.currentZone.getNumberOfMaterialBlocks() - 1);
                    try {
                        Log.i("run-NotNull", "Trying to sleep...");
                        lock.unlock();
                        Thread.sleep(this.cooldownMappings.get(this.currentZone.getMaterial()));
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            } else {
                try {
                    Log.i("run-Null", "Trying to sleep...");
                    lock.unlock();
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
        lock.unlock();
    }

    public void enterZone(Zone zone) {
        try {
            lock.lock();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        Log.i("enterZone", "Entering new zone...");
        this.currentZone = new Zone();
        this.currentZone.setArrival(DateTime.now());
        this.currentZone.setLatitude(zone.getLatitude());
        this.currentZone.setLongitude(zone.getLongitude());
        this.currentZone.setMaterial(zone.getMaterial());
        this.currentZone.setRadius(zone.getRadius());
        this.currentZone.setNumberOfMaterialBlocks(zone.getNumberOfMaterialBlocks());

        this.unload.addZone(this.currentZone);

        lock.unlock();
    }

    public void leaveZone() {
        try {
            lock.lock();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        this.currentZone.setDeparture(DateTime.now());

        lock.unlock();
    }

    public void stop() {
        try {
            lock.lock();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        leaveZone();

        this.unload.setUser(user);
        this.unload.setUnload(DateTime.now());

        Thread.currentThread().interrupt();

        lock.unlock();
    }

    public Zone getCurrentZone() {
        try {
            lock.lock();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        Zone tempZone = this.currentZone;

        lock.unlock();

        return tempZone;
    }

    public void setCurrentZone(Zone currentZone) {
        this.currentZone = currentZone;
    }

    public Unload getUnload() {
        return unload;
    }

    public void setUnload(Unload unload) {
        this.unload = unload;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}


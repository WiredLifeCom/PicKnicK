package wiredlife.com.picknick.async;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.wiredlife.jsonformatjava.model.unload.Zone;

import wiredlife.com.picknick.utility.MiningModule;

public class DoMiningProgressAsync extends AsyncTask<Void, Double, Void> {

    private Context context;
    private Zone currentZone;
    private MiningModule miningModule;

    public DoMiningProgressAsync(Context context, MiningModule miningModule) {
        this.context = context;
        this.miningModule = miningModule;
    }

    @Override
    protected Void doInBackground(Void... voids) {
        double oldPercent = -1;

        while (!Thread.currentThread().isInterrupted()) {
            Log.i("Zone", miningModule.getCurrentZone().toString());

            Zone miningModuleZone = miningModule.getCurrentZone();

            if (currentZone != null && miningModuleZone.getArrival() != null) {
                double newPercent = (miningModule.getCurrentZone().getNumberOfMaterialBlocks() / Double.valueOf(currentZone.getNumberOfMaterialBlocks())) * 100;

                if (newPercent <= 0) {
                    Thread.currentThread().interrupt();
                }

                if (newPercent != oldPercent) {
                    publishProgress(newPercent);
                }

                oldPercent = newPercent;
            }

            if (!Thread.currentThread().isInterrupted()) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    @Override
    protected void onProgressUpdate(Double... progress) {
        int blocksLeft = miningModule.getCurrentZone().getNumberOfMaterialBlocks();
        if (blocksLeft > 0) {
            Toast.makeText(context, "Resources left : " + blocksLeft + " " + miningModule.getCurrentZone().getMaterial() + " blocks (" + progress[0] + "%)", Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(context, "No resources left! Move to another zone", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onPostExecute(Void result) {
        //showDialog("Downloaded " + result + " bytes");
    }

    public void setCurrentZone(Zone currentZone) {
        this.currentZone = currentZone;
    }

}

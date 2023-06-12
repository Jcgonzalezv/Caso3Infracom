package gload;

import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;

public class LoadGenerator
{
    public static int SYNC_GAP;
    private String name;
    private int executorsNumber;
    private ExecutorService executors;
    private int loadUnits;
    private Task unit;
    private long timeGap;
    
    static {
        LoadGenerator.SYNC_GAP = 5000;
    }
    
    public LoadGenerator(final String nameP, final int executorsNumberP, final int loadUnitsP, final Task unitP, final long timeGapP) {
        this.name = nameP;
        this.executorsNumber = executorsNumberP;
        this.executors = Executors.newFixedThreadPool(executorsNumberP);
        this.loadUnits = loadUnitsP;
        this.unit = unitP;
        this.timeGap = timeGapP;
    }
    
    public LoadGenerator(final String nameP, final int loadUnitsP, final Task unitP, final long timeGapP) {
        this.name = nameP;
        this.executorsNumber = loadUnitsP;
        this.executors = Executors.newFixedThreadPool(this.executorsNumber);
        this.loadUnits = loadUnitsP;
        this.unit = unitP;
        this.timeGap = timeGapP;
    }
    
    public void generate() {
        for (int i = 0; i < this.loadUnits; ++i) {
            boolean sync = false;
            if (this.timeGap == 0L) {
                sync = true;
            }
            final LoadUnit unidad = new LoadUnit(this.unit, i, this.timeGap * i, sync);
            this.executors.execute(unidad);
            try {
                Thread.sleep(this.timeGap);
            }
            catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
    
    public static int getSYNC_GAP() {
        return LoadGenerator.SYNC_GAP;
    }
    
    public static void setSYNC_GAP(final int SYNC_GAP_P) {
        LoadGenerator.SYNC_GAP = SYNC_GAP_P;
    }
}

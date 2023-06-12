
package gload;

import java.util.Date;

public class LoadUnit implements Runnable
{
    private Task command;
    private int id;
    private long extraTimeGap;
    private boolean sync;
    
    public LoadUnit(final Task commandP, final int idP, final long extraTimeGapP, final boolean syncP) {
        this.command = commandP;
        this.id = idP;
        this.extraTimeGap = extraTimeGapP;
        this.sync = syncP;
    }
    
    @Override
    public void run() {
        if (this.sync) {
            this.waitUntil();
        }
        this.command.execute();
        System.out.println("[LoadUnit " + this.id + "] [Executed at: " + new Date(System.currentTimeMillis()) + "]");
    }
    
    public void waitUntil() {
        final long born = System.currentTimeMillis();
        final long waitMl = born + LoadGenerator.SYNC_GAP + this.extraTimeGap;
        final Date wait = new Date(waitMl);
        System.out.println("[LoadUnit" + this.id + "] [Waiting Until Sync: " + wait.toString() + "**]");
        for (boolean isTheTime = false; !isTheTime; isTheTime = true) {
            if (new Date(System.currentTimeMillis()).toString().equals(wait.toString())) {}
        }
    }
}

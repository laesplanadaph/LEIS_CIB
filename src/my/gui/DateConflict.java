/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package my.gui;

/**
 *
 * @author gene chester
 */
public class DateConflict {
    private boolean resultWithId;
    private boolean resultWithoutId;
    
    public DateConflict(boolean withId, boolean withoutId) {
        this.resultWithId = withId;
        this.resultWithoutId = withoutId;
    }

    /**
     * @return the withId
     */
    public boolean getResultWithId() {
        return resultWithId;
    }

    /**
     * @param withId the withId to set
     */
    public void setResultWithId(boolean withId) {
        this.resultWithId = withId;
    }

    /**
     * @return the withoutId
     */
    public boolean getResultWithoutId() {
        return resultWithoutId;
    }

    /**
     * @param withoutId the withoutId to set
     */
    public void setResultWithoutId(boolean withoutId) {
        this.resultWithoutId = withoutId;
    }
    
}

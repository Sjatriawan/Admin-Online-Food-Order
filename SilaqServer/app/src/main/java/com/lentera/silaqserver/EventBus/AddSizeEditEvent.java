package com.lentera.silaqserver.EventBus;

public class AddSizeEditEvent {
    private boolean addon;
    private int pos;

    public AddSizeEditEvent(boolean addon, int pos) {
        this.addon = addon;
        this.pos = pos;
    }

    public boolean isAddon() {
        return addon;
    }

    public void setAddon(boolean addon) {
        this.addon = addon;
    }

    public int getPos() {
        return pos;
    }

    public void setPos(int pos) {
        this.pos = pos;
    }
}

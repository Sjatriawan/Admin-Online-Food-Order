package com.lentera.silaqserver.EventBus;

public class ChangeMenuCLick {
    private boolean isFromFoodList;

    public ChangeMenuCLick(boolean isFromFoodList) {
        this.isFromFoodList = isFromFoodList;
    }

    public boolean isFromFoodList() {
        return isFromFoodList;
    }

    public void setFromFoodList(boolean fromFoodList) {
        isFromFoodList = fromFoodList;
    }
}

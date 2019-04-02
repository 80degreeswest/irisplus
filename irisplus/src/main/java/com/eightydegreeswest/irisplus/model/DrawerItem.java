package com.eightydegreeswest.irisplus.model;

import java.io.Serializable;

public class DrawerItem implements Serializable {
	 
    private static final long serialVersionUID = 6064639544714872170L;
    
	String itemName;
    int imgResID;
    int position;

    public DrawerItem(String itemName, int imgResID, int position) {
          super();
          this.itemName = itemName;
          this.imgResID = imgResID;
          this.position = position;
    }

    public String getItemName() {
        return itemName;
    }
    public void setItemName(String itemName) {
        this.itemName = itemName;
    }
    public int getImgResID() {
          return imgResID;
    }
    public void setImgResID(int imgResID) {
          this.imgResID = imgResID;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }
}
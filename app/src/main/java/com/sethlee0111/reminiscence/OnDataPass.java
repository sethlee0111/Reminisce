package com.sethlee0111.reminiscence;

public interface OnDataPass {
    void onDataPass(String... data);
    void onTimePass(int hourOfDay, int minute);
    void onDatePass(int year, int month, int dayOfMonth);
}
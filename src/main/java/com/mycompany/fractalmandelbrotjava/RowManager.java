/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.fractalmandelbrotjava;



 //Aceasta reprezintă resursa partajată la care toate firele de execuție au acces.
 
public class RowManager {
    private int currentRow = 0;
    private final int maxHeight;

    public RowManager(int maxHeight) {
        this.maxHeight = maxHeight;
    }

    
    //Metoda sincronizată pt obtinerea urmatorului rand 
    public synchronized int getNextRow() {
        if (currentRow < maxHeight) {
            int rowToReturn = currentRow;
            currentRow++;
            return rowToReturn;
        }
        return -1; // Toate randurile au fost procesate
    }
}

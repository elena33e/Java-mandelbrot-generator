/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.fractalmandelbrotjava;

import java.util.concurrent.atomic.AtomicLong;

public class Worker implements Runnable {
    private final RowManager rowManager; // Resursa partajată
    private final int width;
    private final int height;
    private final FractalConfig conf;
    private final int[] allData;
    private final AtomicLong totalIters;

    public Worker(RowManager rowManager, int width, int height, FractalConfig conf, int[] allData, AtomicLong totalIters) {
        this.rowManager = rowManager;
        this.width = width;
        this.height = height;
        this.conf = conf;
        this.allData = allData;
        this.totalIters = totalIters;
    }

    @Override
    public void run() {
        long localIters = 0;
        int y;
        
   
        while ((y = rowManager.getNextRow()) != -1) {
            double py = conf.yStart() + (double) y * conf.height() / height;
            
            for (int x = 0; x < width; x++) {
                double px = conf.xStart() + (double) x * conf.width() / width;
                
                int iter;
                if (conf.isJulia()) {
                    iter = compute(px, py, conf.cRe(), conf.cIm());
                } else {
                    iter = compute(0.0, 0.0, px, py);
                }
                
                // Scriere direct în vectorul partajat
                allData[y * width + x] = iter;
                localIters += iter;
            }
        }
        
        // Adunăm rezultatul local la cel global 
        totalIters.addAndGet(localIters);
    }

    private int compute(double zr, double zi, double cr, double ci) {
        int i = 0;
        while (zr * zr + zi * zi <= 4.0 && i < conf.maxIter()) {
            double temp_zr = zr * zr - zi * zi + cr;
            zi = 2.0 * zr * zi + ci;
            zr = temp_zr;
            i++;
        }
        return i;
    }
}

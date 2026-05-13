/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.fractalmandelbrotjava;
import java.util.concurrent.atomic.AtomicLong;


public class Worker implements Runnable {
    private final int id;
    private final int totalThreads;
    private final int width;
    private final int height;
    private final FractalConfig conf;
    private final int[] allData;
    private final AtomicLong totalIters;

    public Worker(int id, int totalThreads, int width, int height, FractalConfig conf, int[] allData, AtomicLong totalIters) {
        this.id = id;
        this.totalThreads = totalThreads;
        this.width = width;
        this.height = height;
        this.conf = conf;
        this.allData = allData;
        this.totalIters = totalIters;
    }

    @Override
    public void run() {
        long localIters = 0;
        
        // Distribuție intercalată 
        for (int y = id; y < height; y += totalThreads) {
            double py = conf.yStart() + (double) y * conf.height() / height;
            
            for (int x = 0; x < width; x++) {
                double px = conf.xStart() + (double) x * conf.width() / width;
                
                int iter;
                if (conf.isJulia()) {
                    // Pentru Julia: z0 = (px, py), c = constant
                    iter = compute(px, py, conf.cRe(), conf.cIm());
                } else {
                    // Pentru Mandelbrot: z0 = 0, c = (px, py)
                    iter = compute(0.0, 0.0, px, py);
                }
                
                allData[y * width + x] = iter;
                localIters += iter;
            }
        }
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
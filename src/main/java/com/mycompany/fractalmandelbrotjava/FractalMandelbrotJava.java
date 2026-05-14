package com.mycompany.fractalmandelbrotjava;

import java.util.concurrent.atomic.AtomicLong;
import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;

// Record configurare fractali
record FractalConfig(
    double xStart, 
    double yStart, 
    double width, 
    double height,
    int maxIter,
    boolean isJulia,
    double cRe,
    double cIm
) {}

public class FractalMandelbrotJava {
    static final int WIDTH = 1024;
    static final int HEIGHT = 768;
    static final int MAX_ITER = 1000;
    static final int NUM_THREADS = 100; 

    public static void main(String[] args) {
        
        executaCalcul("mandelbrot_dynamic.ppm", 
            new FractalConfig(-2.0, -1.2, 3.0, 2.4, MAX_ITER, false, 0, 0));
        
        executaCalcul("julia_dynamic_0.ppm", 
            new FractalConfig(-1.5, -1.5, 3.0, 3.0, MAX_ITER, true, -0.8, 0.156));
        
        executaCalcul("julia_dynamic_1.ppm", 
            new FractalConfig(-1.5, -1.5, 3.0, 3.0, MAX_ITER, true, -0.7269, 0.1889));
        
        executaCalcul("julia_dynamic_2.ppm", 
            new FractalConfig(-1.5, -1.5, 3.0, 3.0, MAX_ITER, true, 0.285, 0.01));
    }

    private static void executaCalcul(String numeFisier, FractalConfig config) {
        AtomicLong totalIters = new AtomicLong(0);
        int[] allData = new int[WIDTH * HEIGHT];
        
        // Instanța resursei partajate
        RowManager rowManager = new RowManager(HEIGHT);
        
        System.out.println("Incepere calcul pentru: " + numeFisier);
        long startTime = System.currentTimeMillis();
        
        Thread[] threads = new Thread[NUM_THREADS];

        for (int i = 0; i < NUM_THREADS; i++) {
            
            threads[i] = new Thread(new Worker(rowManager, WIDTH, HEIGHT, config, allData, totalIters));
            threads[i].start();
        }

        try {
            for (Thread t : threads) {
                t.join();
            }
        } catch (InterruptedException e) {
            System.err.println("Eroare la sincronizarea firelor: " + e.getMessage());
        }

        long duration = System.currentTimeMillis() - startTime;
        System.out.printf("[%s] Finalizat in %.4f secunde. Total iteratii: %d\n\n", 
            numeFisier, duration / 1000.0, totalIters.get());
        
        saveToPPM(numeFisier, allData);
    }

    private static void saveToPPM(String filename, int[] data) {
        try (BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(filename))) {
            out.write(("P6\n" + WIDTH + " " + HEIGHT + "\n255\n").getBytes());
            for (int iter : data) {
                byte r = 0, g = 0, b = 0;
                if (iter != MAX_ITER) {
                    r = (byte) ((iter * 9) % 256);
                    g = (byte) ((iter * 2) % 256);
                    b = (byte) ((iter * 15) % 256);
                }
                out.write(new byte[]{r, g, b});
            }
        } catch (IOException e) {
            System.err.println("Eroare la salvare: " + e.getMessage());
        }
    }

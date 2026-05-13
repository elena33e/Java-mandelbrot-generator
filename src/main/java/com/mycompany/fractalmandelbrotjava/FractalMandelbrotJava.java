package com.mycompany.fractalmandelbrotjava;

import java.util.concurrent.atomic.AtomicLong;
import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;

// Păstrăm FractalConfig doar pentru a grupa parametrii matematici
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
        // Apelăm direct funcția de execuție pentru fiecare fișier dorit
        executaCalcul("mandelbrot_java.ppm", new FractalConfig(-2.0, -1.2, 3.0, 2.4, MAX_ITER, false, 0, 0));
        
        executaCalcul("julia_java_0.ppm", new FractalConfig(-1.5, -1.5, 3.0, 3.0, MAX_ITER, true, -0.8, 0.156));
        
        executaCalcul("julia_java_1.ppm", new FractalConfig(-1.5, -1.5, 3.0, 3.0, MAX_ITER, true, -0.7269, 0.1889));
        
        executaCalcul("julia_java_2.ppm", new FractalConfig(-1.5, -1.5, 3.0, 3.0, MAX_ITER, true, 0.285, 0.01));
    }

    private static void executaCalcul(String numeFisier, FractalConfig config) {
        AtomicLong totalIters = new AtomicLong(0);
        int[] allData = new int[WIDTH * HEIGHT];
        
        long startTime = System.currentTimeMillis();
        Thread[] threads = new Thread[NUM_THREADS];

        // Crearea și pornirea firelor de execuție
        for (int i = 0; i < NUM_THREADS; i++) {
            threads[i] = new Thread(new Worker(i, NUM_THREADS, WIDTH, HEIGHT, config, allData, totalIters));
            threads[i].start();
        }

        // Așteptarea finalizării tuturor firelor (Barieră)
        try {
            for (Thread t : threads) {
                t.join();
            }
        } catch (InterruptedException e) {
            System.err.println("Eroare la sincronizare: " + e.getMessage());
        }

        long endTime = System.currentTimeMillis();
        double duration = (endTime - startTime) / 1000.0;

        System.out.printf("[%s] Finalizat in %.4f secunde. Total iteratii: %d\n", numeFisier, duration, totalIters.get());
        
        saveToPPM(numeFisier, allData);
    }

    private static void saveToPPM(String filename, int[] data) {
        try (BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(filename))) {
            String header = "P6\n" + WIDTH + " " + HEIGHT + "\n255\n";
            out.write(header.getBytes());

            for (int iter : data) {
                byte r = 0, g = 0, b = 0;
                if (iter != MAX_ITER) {
                    r = (byte) ((iter * 9) % 256);
                    g = (byte) ((iter * 2) % 256);
                    b = (byte) ((iter * 15) % 256);
                }
                out.write(r);
                out.write(g);
                out.write(b);
            }
        } catch (IOException e) {
            System.err.println("Eroare la salvarea fisierului: " + e.getMessage());
        }
    }
}
package com.example.sushitrain.scanner;

import java.io.File;
import java.io.FileNotFoundException;

public interface TrayScanner {

    int scanTrays(File input) throws FileNotFoundException;
}

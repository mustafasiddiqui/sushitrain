package com.example.sushitrain.scanner;

import java.io.FileNotFoundException;

public interface TrayScanner {

    int scanTrays(String pathString) throws FileNotFoundException;

}

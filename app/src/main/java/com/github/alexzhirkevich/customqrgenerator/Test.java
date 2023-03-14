package com.github.alexzhirkevich.customqrgenerator;

import com.github.alexzhirkevich.customqrgenerator.vector.QrVectorOptions;

public class Test {
    public static void main(String[] args) {
        new QrVectorOptions.Builder()
                .setFourthEyeEnabled(true);
    }
}

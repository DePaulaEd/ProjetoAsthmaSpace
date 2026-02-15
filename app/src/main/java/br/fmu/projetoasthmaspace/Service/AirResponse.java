package br.fmu.projetoasthmaspace.Service;

import java.util.List;

public class AirResponse {
    public Coord coord;
    public List<Item> list;

    public static class Coord {
        public double lat;
        public double lon;
    }

    public static class Item {
        public Main main;
        public Components components;
    }

    public static class Main {
        public int aqi;
    }

    public static class Components {
        public double co;
        public double no2;
        public double o3;
        public double so2;
        public double pm2_5;
        public double pm10;
    }
}

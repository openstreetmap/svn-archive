package org.openstreetmap.labelling.annealing;

public class DefaultReversionProbability implements ReversionProbability
{
    private AnnealingOven oven = null;

    public double getReversionProbability()
    {
        double temperature = oven.getAnnealingSchedule()
                .getCurrentTemperature();
        double deltaE = oven.getAnnealingMaterial().getEnergy().getDelta();
        //System.out.println("delta: " + deltaE);
        //System.out.println("temp: " + temperature);
        return 1 - Math.exp(-deltaE / temperature);
    }

    public void setAnnealingOven(AnnealingOven oven)
    {
        this.oven = oven;
    }
}

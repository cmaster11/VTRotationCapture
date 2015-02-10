package tests.viewtwoo.com.vtrotationcapture;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by cmaster11 on 1/13/2015.
 */
public class StatisticList implements Serializable {
    public StatisticList() {
    }

    private int currentValuesSize = 0;

    private Boolean bCalculateMaximumDifference = false;

    public Boolean getbCalculateMaximumDifference() {
        return bCalculateMaximumDifference;
    }

    public void setbCalculateMaximumDifference(Boolean bCalculateMaximumDifference) {

        this.bCalculateMaximumDifference = bCalculateMaximumDifference;
    }

    public StatisticList(Integer limit) {
        this.limit = limit;
    }

    private int precision = 2;

    public int getPrecision() {
        return precision;
    }

    public void setPrecision(int precision) {
        this.precision = precision;
    }

    private Boolean calculateAtAdd = false;

    public Boolean getCalculateAtAdd() {
        return calculateAtAdd;
    }

    public void setCalculateAtAdd(Boolean calculateAtAdd) {
        this.calculateAtAdd = calculateAtAdd;
    }

    private static final String TAG = StatisticList.class.toString();

    private LinkedList<Double> values = new LinkedList<>();
    private Integer limit = null;

    //double currentValue;
    double currentQuadraticAverage = 0;

    public double getQuadraticAverage() {
        if (calculateAtAdd)
            return currentQuadraticAverage;
        else
            return calculateQuadraticAverage();
    }

    public double getAverage() {
        if (calculateAtAdd)
            return currentAverage;
        else
            return calculateAverage();
    }

    public double getMaximumDifference() {
        if (calculateAtAdd)
            return currentMaximumDifference;
        else
            return calculateMaximumDifference();
    }

    public double getVariance() {
        if (calculateAtAdd)
            return currentVariance;
        else
            return calculateVariance();
    }

    private double calculateQuadraticAverage() {
        currentQuadraticAverage = 0;

        if (currentValuesSize == 0) return 0;

        for (double currentValue : values) {
            currentQuadraticAverage += Math.pow(currentValue, 2);
        }

        currentQuadraticAverage = Math.sqrt(currentQuadraticAverage / (double) currentValuesSize);

        return currentQuadraticAverage;
    }

    private void calculateQuadraticAverageAtAdd(Double oldValue) {
        if(Double.isNaN(currentQuadraticAverage)) currentQuadraticAverage = 0;

        if (oldValue != null) {
            currentQuadraticAverage = Math.pow(currentQuadraticAverage, 2) * currentValuesSize;
            currentQuadraticAverage -= Math.pow(oldValue, 2);
        } else {
            currentQuadraticAverage = Math.pow(currentQuadraticAverage, 2) * (currentValuesSize - 1);
        }

        currentQuadraticAverage += Math.pow(values.getLast(), 2);

        currentQuadraticAverage = Math.sqrt(currentQuadraticAverage / currentValuesSize);
    }

    double lastAverage = 0;
    double currentAverage = 0;

    private double calculateAverage() {
        lastAverage = currentAverage;
        currentAverage = 0;

        if (currentValuesSize == 0) return 0;

        for (double currentValue : values) {
            currentAverage += currentValue;
        }

        currentAverage = currentAverage / (double) currentValuesSize;

        return currentAverage;
    }

    double varianceAverage = 0;

    double currentVariance = 0;

    private double calculateVariance() {
        if (currentValuesSize == 0) return 0;

        varianceAverage = calculateAverage();
        currentVariance = 0;

        for (double currentValue : values) {
            currentVariance += Math.pow(currentValue - varianceAverage, 2);
        }

        return currentVariance;
    }

    private void calculateAverageAtAdd(Double oldValue) {
        if(Double.isNaN(currentAverage)) currentAverage = 0;

        lastAverage = currentAverage;

        if (oldValue != null) {
            currentAverage *= currentValuesSize;
            currentAverage -= oldValue;
        } else {
            currentAverage *= (currentValuesSize - 1);
        }

        currentAverage += values.getLast();

        currentAverage = currentAverage / (double) currentValuesSize;
    }

    private void calculateVarianceAtAdd(Double oldValue) {
        if(Double.isNaN(currentVariance)) currentVariance = 0;

        if (oldValue != null) {
            currentVariance -= Math.pow(oldValue - lastAverage, 2);
        }

        currentVariance += Math.pow(values.getLast() - currentAverage, 2);
    }

    private void calculateMaximumDifferenceAtAdd(Double oldValue) {
        if(Double.isNaN(currentMaximum)) currentMaximum = 0;
        if(Double.isNaN(currentMinimum)) currentMinimum = 0;

        double newValue = values.getLast();

        if (currentValuesSize == 1) {
            currentMaximum = newValue;
            currentMinimum = newValue;

            return;
        }

        if (oldValue != null) {
            if (currentMinimum == oldValue || currentMaximum == oldValue) {
                calculateMaximumDifference();
            }

            return;
        }

        if (newValue > currentMaximum) currentMaximum = newValue;
        if (newValue < currentMinimum) currentMinimum = newValue;
    }


    @Override
    public String toString() {

        String doubleDescriptor = "%." + precision + "f";

        String log = "";

        log += String.format("Avg: " + doubleDescriptor, calculateAtAdd ? currentAverage : calculateAverage());
        log += String.format(" SQAvg: " + doubleDescriptor, calculateAtAdd ? currentQuadraticAverage : calculateQuadraticAverage());
        log += String.format(" Var: " + doubleDescriptor, calculateAtAdd ? currentVariance : calculateVariance());

        if (bCalculateMaximumDifference)
            log += String.format(" Diff: " + doubleDescriptor, calculateAtAdd ? currentMaximumDifference : calculateMaximumDifference());

        return log;
    }

    double currentMinimum = 0, currentMaximum = 0, currentMaximumDifference = 0;
    Boolean firstCycle = true;

    private double calculateMaximumDifference() {

        if (currentValuesSize == 0) return 0;
        firstCycle = true;

        for (double currentValue : values) {
            if (firstCycle) {
                currentMinimum = currentValue;
                currentMaximum = currentValue;
                firstCycle = false;
                continue;
            }

            if (currentValue > currentMaximum)
                currentMaximum = currentValue;

            if (currentValue < currentMinimum)
                currentMinimum = currentValue;
        }

        currentMaximumDifference = currentMaximum - currentMinimum;
        return currentMaximumDifference;
    }

    public List<Double> getValues() {
        return values;
    }

    public void clearValues() {
        values.clear();
    }

    public Integer getLimit() {
        return limit;
    }

    public void setLimit(Integer limit) {
        this.limit = limit;

        checkSizeLimit();
    }

    public void addValue(double value) {
        values.addLast(value);
        Double oldValue = checkSizeLimit();
        currentValuesSize = values.size();

        if (calculateAtAdd) {
            calculateAverageAtAdd(oldValue);
            calculateVarianceAtAdd(oldValue);
            if (bCalculateMaximumDifference)
                calculateMaximumDifferenceAtAdd(oldValue);
            calculateQuadraticAverageAtAdd(oldValue);
        }
    }

    private Double checkSizeLimit() {
        if (limit == null) return null;

        Double lastDouble = null;

        while (values.size() > limit) {
            lastDouble = values.pollFirst();
        }

        return lastDouble;
    }

}

/*

    private static HashMap<String, ArrayList<Double>> statisticListsValues = new HashMap<>();
    private static HashMap<String, Integer> statisticListsLimits = new HashMap<>();

    private static Integer listDefaultLimit = 100;

    public static void addList(String label) throws Exception {
        if(statisticListsValues.containsKey(label))
        {
            Log.e(TAG, String.format("Label %s already exists!", label));
            throw new Exception(String.format("Label %s already exists!", label));
        }

        statisticListsValues.put(label, new ArrayList<Double>());
        statisticListsLimits.put(label, null);


    }

    public static void setListLimit(Integer limit)
    {

    }*/
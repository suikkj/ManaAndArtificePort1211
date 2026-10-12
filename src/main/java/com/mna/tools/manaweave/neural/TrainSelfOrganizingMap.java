/*
 * Decompiled with CFR 0.152.
 */
package com.mna.tools.manaweave.neural;

import com.mna.tools.manaweave.neural.LearningMethod;
import com.mna.tools.manaweave.neural.Matrix;
import com.mna.tools.manaweave.neural.MatrixMath;
import com.mna.tools.manaweave.neural.NormalizeInput;
import com.mna.tools.manaweave.neural.SelfOrganizingMap;

public class TrainSelfOrganizingMap {
    private SelfOrganizingMap som;
    private LearningMethod learnMethod;
    private double learnRate;
    private double reduction = 0.99;
    private double globalError;
    private double totalError;
    private double bestError;
    private final int inputNeuronCount;
    private final int outputNeuronCount;
    private final SelfOrganizingMap bestNet;
    private double[][] train;
    private int[] won;
    private Matrix work;
    private Matrix correc;

    public TrainSelfOrganizingMap(SelfOrganizingMap som, double[][] trainingData, LearningMethod learnMethod, double learnRate) {
        this.som = som;
        this.train = trainingData;
        this.totalError = 1.0;
        this.learnMethod = learnMethod;
        this.learnRate = learnRate;
        this.outputNeuronCount = som.countOutputNeurons();
        this.inputNeuronCount = som.countInputNeurons();
        this.totalError = 1.0;
        for (int tset = 0; tset < this.train.length; ++tset) {
            Matrix dptr = Matrix.createColumnMatrix(this.train[tset]);
            if (!(MatrixMath.vectorLength(dptr) < 1.0E-30)) continue;
            throw new RuntimeException("Multiplicative normalization has null training case");
        }
        this.bestNet = new SelfOrganizingMap(this.inputNeuronCount, this.outputNeuronCount, this.som.getNormalizationType());
        this.won = new int[this.outputNeuronCount];
        this.correc = new Matrix(this.outputNeuronCount, this.inputNeuronCount + 1);
        this.work = this.learnMethod == LearningMethod.ADDITIVE ? new Matrix(1, this.inputNeuronCount + 1) : null;
        this.initialize();
        this.bestError = Double.MAX_VALUE;
    }

    public void initialize() {
        this.som.getOutputWeights().randomize(-1.0, 1.0);
        for (int i = 0; i < this.outputNeuronCount; ++i) {
            this.normalizeWeight(this.som.getOutputWeights(), i);
        }
    }

    public double getBestError() {
        return this.bestError;
    }

    public double getTotalError() {
        return this.totalError;
    }

    protected void adjustWeights() {
        for (int i = 0; i < this.outputNeuronCount; ++i) {
            if (this.won[i] == 0) continue;
            double f = 1.0 / (double)this.won[i];
            if (this.learnMethod == LearningMethod.SUBTRACTIVE) {
                f *= this.learnRate;
            }
            for (int j = 0; j <= this.inputNeuronCount; ++j) {
                double corr = f * this.correc.get(i, j);
                this.som.getOutputWeights().add(i, j, corr);
            }
        }
    }

    private void copyWeights(SelfOrganizingMap source, SelfOrganizingMap target) {
        MatrixMath.copy(source.getOutputWeights(), target.getOutputWeights());
    }

    void evaluateErrors() throws RuntimeException {
        this.correc.clear();
        for (int i = 0; i < this.won.length; ++i) {
            this.won[i] = 0;
        }
        this.globalError = 0.0;
        for (int tset = 0; tset < this.train.length; ++tset) {
            double diff;
            int i;
            int best;
            NormalizeInput input = new NormalizeInput(this.train[tset], this.som.getNormalizationType());
            int n = best = this.som.winner(input);
            this.won[n] = this.won[n] + 1;
            Matrix wptr = this.som.getOutputWeights().getRow(best);
            double length = 0.0;
            for (i = 0; i < this.inputNeuronCount; ++i) {
                diff = this.train[tset][i] * input.getNormFac() - wptr.get(0, i);
                length += diff * diff;
                if (this.learnMethod == LearningMethod.SUBTRACTIVE) {
                    this.correc.add(best, i, diff);
                    continue;
                }
                this.work.set(0, i, this.learnRate * this.train[tset][i] * input.getNormFac() + wptr.get(0, i));
            }
            diff = input.getSynth() - wptr.get(0, this.inputNeuronCount);
            length += diff * diff;
            if (this.learnMethod == LearningMethod.SUBTRACTIVE) {
                this.correc.add(best, this.inputNeuronCount, diff);
            } else {
                this.work.set(0, this.inputNeuronCount, this.learnRate * input.getSynth() + wptr.get(0, this.inputNeuronCount));
            }
            if (length > this.globalError) {
                this.globalError = length;
            }
            if (this.learnMethod != LearningMethod.ADDITIVE) continue;
            this.normalizeWeight(this.work, 0);
            for (i = 0; i <= this.inputNeuronCount; ++i) {
                this.correc.add(best, i, this.work.get(0, i) - wptr.get(0, i));
            }
        }
        this.globalError = Math.sqrt(this.globalError);
    }

    void forceWin() throws RuntimeException {
        double[] output;
        int best;
        int which = 0;
        Matrix outputWeights = this.som.getOutputWeights();
        double dist = Double.MAX_VALUE;
        for (int tset = 0; tset < this.train.length; ++tset) {
            best = this.som.winner(this.train[tset]);
            output = this.som.getOutput();
            if (!(output[best] < dist)) continue;
            dist = output[best];
            which = tset;
        }
        NormalizeInput input = new NormalizeInput(this.train[which], this.som.getNormalizationType());
        best = this.som.winner(input);
        output = this.som.getOutput();
        dist = Double.MIN_VALUE;
        int i = this.outputNeuronCount;
        while (i-- > 0) {
            if (this.won[i] != 0 || !(output[i] > dist)) continue;
            dist = output[i];
            which = i;
        }
        for (int j = 0; j < input.getInputMatrix().getCols(); ++j) {
            outputWeights.set(which, j, input.getInputMatrix().get(0, j));
        }
        this.normalizeWeight(outputWeights, which);
    }

    public void iteration() throws RuntimeException {
        this.evaluateErrors();
        this.totalError = this.globalError;
        if (this.totalError < this.bestError) {
            this.bestError = this.totalError;
            this.copyWeights(this.som, this.bestNet);
        }
        int winners = 0;
        for (int i = 0; i < this.won.length; ++i) {
            if (this.won[i] == 0) continue;
            ++winners;
        }
        if (winners < this.outputNeuronCount && winners < this.train.length) {
            this.forceWin();
            return;
        }
        this.adjustWeights();
        if (this.learnRate > 0.01) {
            this.learnRate *= this.reduction;
        }
    }

    protected void normalizeWeight(Matrix matrix, int row) {
        double len = MatrixMath.vectorLength(matrix.getRow(row));
        len = Math.max(len, 1.0E-30);
        len = 1.0 / len;
        for (int i = 0; i < this.inputNeuronCount; ++i) {
            matrix.set(row, i, matrix.get(row, i) * len);
        }
        matrix.set(row, this.inputNeuronCount, 0.0);
    }
}

